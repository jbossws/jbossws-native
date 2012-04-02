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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServicePermission;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.wsf.spi.deployment.Deployment;
import org.w3c.dom.Element;

/**
 * A Web service endpoint implementation.
 *  
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class EndpointImpl extends Endpoint
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointImpl.class);

   private static final Logger log = Logger.getLogger(EndpointImpl.class);
   private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION = new WebServicePermission("publishEndpoint");

   private Object implementor;
   private Executor executor;
   private List<Source> metadata;
   private BindingProviderImpl bindingProvider;
   private Map<String, Object> properties = new HashMap<String, Object>();
   private boolean isPublished;
   private URI address;
   private Deployment dep;

   public EndpointImpl(String bindingId, Object implementor, WebServiceFeature[] features)
   {
      if (implementor == null)
      {
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "IMPLEMENTOR_CANNOT_BE_NULL"));
      }

      this.implementor = implementor;
      this.bindingProvider = new BindingProviderImpl(bindingId);
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
	  throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void stop()
   {
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
      throw new UnsupportedOperationException();
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
   
   public void publish(final javax.xml.ws.spi.http.HttpContext serverContext)
   {
      // JAX-WS Endpoint API is broken by design and reveals many implementation details 
      // of JAX-WS RI that are not portable cross different application servers :(
      log.warn(BundleUtils.getMessage(bundle, "PUBLISH_NOT_IMPLEMENT"));
   }

   public void setEndpointContext(final javax.xml.ws.EndpointContext endpointContext)
   {
      // JAX-WS Endpoint API is broken by design and reveals many implementation details 
      // of JAX-WS RI that are not portable cross different application servers :(
      log.warn(BundleUtils.getMessage(bundle, "SETENDPOINTCONTEXT_NOT_IMPLEMENT"));
   }

}
