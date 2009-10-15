/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxws.spi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServicePermission;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.jaxws.wsaddressing.EndpointReferenceUtil;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpServer;
import org.jboss.wsf.spi.http.HttpServerFactory;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.w3c.dom.Element;

/**
 * A Web service endpoint implementation.
 *  
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class EndpointImpl extends Endpoint
{

   private static final Logger log = Logger.getLogger(EndpointImpl.class);
   private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION = new WebServicePermission("publishEndpoint");

   private Object implementor;
   private Executor executor;
   private WebServiceFeature[] features; // TODO: use features
   private List<Source> metadata;
   private BindingProviderImpl bindingProvider;
   private Map<String, Object> properties = new HashMap<String, Object>();
   private HttpContext serverContext;
   private boolean isPublished;
   private boolean isDestroyed;
   private URI address;
   private Deployment dep;

   public EndpointImpl(String bindingId, Object implementor, WebServiceFeature[] features)
   {
      if (implementor == null)
      {
         throw new IllegalArgumentException("Implementor cannot be null");
      }

      this.implementor = implementor;
      this.bindingProvider = new BindingProviderImpl(bindingId);
      this.features = features;
   }

   @Override
   public Binding getBinding()
   {
      return this.bindingProvider.getBinding();
   }

   @Override
   public Object getImplementor()
   {
      return this.implementor;
   }

   /**
    * Publishes this endpoint at the given address. The necessary server infrastructure will be created and configured by the JAX-WS
    * implementation using some default configuration. In order to get more control over the server configuration,
    * please use the javax.xml.ws.Endpoint#publish(Object) method instead.
    *
    * @param address specifying the address to use. The address must be compatible with the binding specified at the time the endpoint was created.
    */
   @Override
   public void publish(final String addr)
   {
      log.debug("publish: " + addr);

      try
      {
         this.address = new URI(addr);
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException("Invalid address: " + addr);
      }

      // Check with the security manger
      this.checkPublishEndpointPermission();

      // Get HTTP server
      final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      final HttpServer httpServer = spiProvider.getSPI(HttpServerFactory.class).getHttpServer();

      final String contextRoot = this.getContextRoot();
      final HttpContext context = httpServer.createContext(contextRoot);

      this.publish(context);
   }

   /**
    * Publishes this endpoint at the provided server context.
    * A server context encapsulates the server infrastructure and addressing information for a particular transport.
    * For a call to this method to succeed, the server context passed as an argument to it must be compatible with the endpoint's binding.
    *
    * @param context An object representing a server context to be used for publishing the endpoint.
    */
   @Override
   public void publish(Object context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      
      log.debug("publishing endpoint " + this + " to " + context);

      if (isDestroyed)
         throw new IllegalStateException("Endpoint already destroyed");

      // Check with the security manger
      checkPublishEndpointPermission();

      if (context instanceof HttpContext)
      {
         this.serverContext = (HttpContext)context;
         if (this.address == null)
         {
            this.address = getAddressFromConfigAndContext(serverContext); // TODO: is it necessary?
         }
         HttpServer httpServer = this.serverContext.getHttpServer();
         httpServer.publish(this.serverContext, this);
         this.isPublished = true;
      }
      else
      {
         throw new UnsupportedOperationException("Cannot handle contexts of type: " + context);
      }
   }
   
   private static URI getAddressFromConfigAndContext(HttpContext context)
   {
      try
      {
         SPIProvider provider = SPIProviderResolver.getInstance().getProvider();
         ServerConfigFactory spi = provider.getSPI(ServerConfigFactory.class);
         ServerConfig serverConfig = spi.getServerConfig();
         String host = serverConfig.getWebServiceHost();
         int port = serverConfig.getWebServicePort();
         String hostAndPort = host + (port > 0 ? ":" + port : ""); 
         return new URI("http://" + hostAndPort + context.getContextRoot());
      }
      catch (URISyntaxException e)
      {
         throw new WebServiceException("Error while getting endpoint address from context!", e);
      }
   }

   @Override
   public void stop()
   {
      log.debug("stop");

      if (serverContext == null || isPublished == false)
         log.error("Endpoint not published");

      try
      {
         if (serverContext != null)
         {
            HttpServer httpServer = serverContext.getHttpServer();
            httpServer.destroy(serverContext, this);
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot stop endpoint", ex);
      }

      isPublished = false;
      isDestroyed = true;
   }

   @Override
   public boolean isPublished()
   {
      return this.isPublished;
   }

   @Override
   public List<Source> getMetadata()
   {
      return this.metadata;
   }

   @Override
   public void setMetadata(final List<Source> list)
   {
      log.info("Ignore metadata, not implemented"); // TODO:
      this.metadata = list;
   }

   @Override
   public Executor getExecutor()
   {
      return this.executor;
   }

   @Override
   public void setExecutor(Executor executor)
   {
      log.info("Ignore executor, not implemented"); // TODO
      this.executor = executor;
   }

   @Override
   public Map<String, Object> getProperties()
   {
      return this.properties;
   }

   @Override
   public void setProperties(Map<String, Object> map)
   {
      this.properties = map;
   }

   private void checkPublishEndpointPermission()
   {
      // 5.10 Conformance (Checking publishEndpoint Permission): When any of the publish methods defined
      // by the Endpoint class are invoked, an implementation MUST check whether a SecurityManager is
      // installed with the application. If it is, implementations MUST verify that the application has the 
      // WebServicePermission identified by the target name publishEndpoint before proceeding. If the permission
      // is not granted, implementations MUST NOT publish the endpoint and they MUST throw a 
      // java.lang.SecurityException.
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         sm.checkPermission(ENDPOINT_PUBLISH_PERMISSION);
      }
   }

   @Override
   public EndpointReference getEndpointReference(Element... referenceParameters)
   {
      return getEndpointReference(W3CEndpointReference.class, referenceParameters);
   }

   @Override
   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters)
   {
      if (isDestroyed || !isPublished)
         throw new WebServiceException("Cannot get EPR for an unpubblished or already destroyed endpoint!");

      if (getBinding() instanceof HTTPBinding)
      {
         throw new UnsupportedOperationException("Cannot get epr when using the XML/HTTP binding");
      }
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
      builder.address(address.toString());
      builder.wsdlDocumentLocation(address.toString() +  "?wsdl");
      //TODO set other parameters in the builder
      if (referenceParameters != null && W3CEndpointReference.class.getName().equals(clazz.getName()))
      {
         for (Element el : referenceParameters)
            builder.referenceParameter(el);
      }

      return EndpointReferenceUtil.transform(clazz, builder.build());
   }
   
   public String getPath()
   {
      String path = this.address.getPath();
      while (path.endsWith("/"))
      {
         path = path.substring(0, path.length() - 1);
      }
      return path;
   }
   
   public int getPort()
   {
      return this.address.getPort();
   }
   
   public String getContextRoot()
   {
      final StringTokenizer st = new StringTokenizer(this.getPath(), "/");
      
      String contextRoot = "/";
      
      if (st.hasMoreTokens())
      {
         contextRoot += st.nextToken();
      }
      
      return contextRoot;
   }
   
   public String getPathWithoutContext()
   {
      // TODO: optimize this method
      StringTokenizer st = new StringTokenizer(this.getPath(), "/");
      if (st.hasMoreTokens())
      {
         st.nextToken();
      }
      StringBuilder sb = new StringBuilder();
      while (st.hasMoreTokens())
      {
         sb.append('/');
         sb.append(st.nextToken());
      }
      sb.append('/');
      
      return sb.toString();
   }
   
   public void setDeployment(final Deployment dep)
   {
      if (this.dep == null)
      {
         this.dep = dep;
      }
   }
   
   public Deployment getDeployment()
   {
      return this.dep;
   }
   
}
