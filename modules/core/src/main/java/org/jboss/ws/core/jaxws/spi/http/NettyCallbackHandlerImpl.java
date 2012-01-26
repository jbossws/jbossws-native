/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ws.core.jaxws.spi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.ws.common.injection.PreDestroyHolder;
import org.jboss.ws.core.server.netty.NettyCallbackHandler;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.EndpointResolver;
import org.jboss.wsf.stack.jbws.WebAppResolver;

/**
 * Netty callback handler operating in JSE environment (replacement for Servlet on J2EE side).
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyCallbackHandlerImpl implements NettyCallbackHandler
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(NettyCallbackHandlerImpl.class);

   /** Logger. */
   private static final Logger LOGGER = Logger.getLogger(NettyCallbackHandlerImpl.class);

   /** SPI provider instance. */
   private static final SPIProvider SPI_PROVIDER = SPIProviderResolver.getInstance().getProvider();

   /** Endpoints registry. */
   private static final EndpointRegistry ENDPOINTS_REGISTRY = NettyCallbackHandlerImpl.SPI_PROVIDER.getSPI(
         EndpointRegistryFactory.class, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader()).getEndpointRegistry();

   /** @PreDestroy registry. */
   private final List<PreDestroyHolder> preDestroyRegistry = new LinkedList<PreDestroyHolder>();

   /** Path this Netty callback operates on. */
   private final String handledPath;

   /** Endpoint associated with this callback. */
   private Endpoint endpoint;

   /**
    * Constructor.
    *
    * @param path this handler operates on
    * @param context this handler operates on
    * @param endpointRegistryPath registry id
    */
   public NettyCallbackHandlerImpl(final String path, final String context, final String endpointRegistryPath)
   {
      super();
      this.initEndpoint(context, endpointRegistryPath);
      this.handledPath = path;
   }

   /**
    * Initialize the service endpoint and associate it with this callback.
    *
    * @param context context path
    * @param endpointRegistryPath registry id
    */
   private void initEndpoint(final String context, final String endpointRegistryPath)
   {
      final EndpointResolver resolver = new WebAppResolver(context, endpointRegistryPath);
      this.endpoint = NettyCallbackHandlerImpl.ENDPOINTS_REGISTRY.resolve(resolver);

      if (this.endpoint == null)
      {
         throw new WebServiceException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ENDPOINT",  endpointRegistryPath));
      }
   }

   /**
    * Handles either WSDL GET request or endpoint POST invocation.
    * 
    * @param method only HTTP GET and POST methods are supported
    * @param is input stream
    * @param os output stream
    * @param invCtx invocation context
    * @throws IOException if some I/O error occurs
    */
   public void handle(final String method, final InputStream is, final OutputStream os, final InvocationContext invCtx)
         throws IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(this.endpoint);
         final RequestHandler requestHandler = this.endpoint.getRequestHandler();

         if (method.equals("POST"))
         {
            requestHandler.handleRequest(this.endpoint, is, os, invCtx);
         }
         else if (method.equals("GET"))
         {
            requestHandler.handleWSDLRequest(this.endpoint, os, invCtx);
         }
         else
         {
            throw new WSException(BundleUtils.getMessage(bundle, "UNSUPPORTED_HTTP_METHOD",  method));
         }
      }
      catch (final Exception e)
      {
         NettyCallbackHandlerImpl.LOGGER.error(e.getMessage(),  e);
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException e)
         {
            NettyCallbackHandlerImpl.LOGGER.error(e.getMessage(),  e);
         }
         try
         {
            os.close();
         }
         catch (IOException e)
         {
            NettyCallbackHandlerImpl.LOGGER.error(e.getMessage(),  e);
         }
         this.registerForPreDestroy(this.endpoint);
         EndpointAssociation.removeEndpoint();
      }
   }

   /**
    * Returns request path this callback operates on.
    * 
    * @return callback path
    */
   public String getPath()
   {
      return this.handledPath;
   }

   /**
    * Registers endpoint for with @PreDestroy registry.
    * 
    * @param endpoint webservice endpoint
    */
   private void registerForPreDestroy(final Endpoint endpoint)
   {
      final PreDestroyHolder holder = (PreDestroyHolder) endpoint.getAttachment(PreDestroyHolder.class);
      if (holder != null)
      {
         synchronized (this.preDestroyRegistry)
         {
            if (!this.preDestroyRegistry.contains(holder))
            {
               this.preDestroyRegistry.add(holder);
            }
         }
         endpoint.removeAttachment(PreDestroyHolder.class);
      }
   }

   /**
    * Template lifecycle method that does nothing in this implementation.
    */
   public void init()
   {
      // Does nothing
   }

   /**
    * Calls @PreDestroy annotated methods on endpoint bean.
    */
   public void destroy()
   {
      synchronized (this.preDestroyRegistry)
      {
         for (final PreDestroyHolder holder : this.preDestroyRegistry)
         {
            try
            {
               final Object targetBean = holder.getObject();
               InjectionHelper.callPreDestroyMethod(targetBean);
            }
            catch (Exception exception)
            {
               NettyCallbackHandlerImpl.LOGGER.error(exception.getMessage(),  exception);
            }
         }
         this.preDestroyRegistry.clear();
      }
   }

}
