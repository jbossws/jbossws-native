/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ws.metadata.builder.jaxws;

import java.io.IOException;

import javax.xml.ws.BindingType;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.PortComponent;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.server.UnifiedDeploymentInfo.DeploymentType;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * Builds ServiceEndpointMetaData for a JAX-WS endpoint.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public abstract class JAXWSEndpointMetaDataBuilder extends JAXWSMetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JAXWSEndpointMetaDataBuilder.class);

   public abstract ServerEndpointMetaData buildEndpointMetaData(UnifiedMetaData wsMetaData, UnifiedDeploymentInfo udi, Class<?> sepClass, String linkName) throws IOException;

   protected void processBindingType(EndpointMetaData epMetaData, Class<?> wsClass)
   {
      if (! wsClass.isAnnotationPresent(BindingType.class))
         return;

      BindingType anBindingType = (BindingType)wsClass.getAnnotation(BindingType.class);
      String uri = anBindingType.value();
      if (uri.length() > 0)
      {
         epMetaData.setBindingId(uri);
      }
   }

   protected void processPortComponent(UnifiedDeploymentInfo udi, Class<?> wsClass, String linkName, ServerEndpointMetaData sepMetaData)
   {
      PortComponent anPortComponent = wsClass.getAnnotation(PortComponent.class);
      if (anPortComponent == null)
         return;

      // setup config name
      if (anPortComponent.configName().length() > 0)
      {
         String configName = anPortComponent.configName();
         sepMetaData.setConfigName(configName);
      }

      // setup config file
      if (anPortComponent.configFile().length() > 0)
      {
         String configFile = anPortComponent.configFile();
         sepMetaData.setConfigFile(configFile);
      }

      boolean isJSEEndpoint = (udi.type == DeploymentType.JAXWS_JSE);

      // context-root
      if (anPortComponent.contextRoot().length() > 0)
      {
         if (isJSEEndpoint)
            log.warn("@PortComponent.contextRoot is only valid on EJB endpoints");

         if (isJSEEndpoint == false)
         {
            String contextRoot = anPortComponent.contextRoot();
            if (contextRoot.startsWith("/") == false)
               contextRoot = "/" + contextRoot;

            sepMetaData.setContextRoot(contextRoot);
         }
      }

      // url-pattern
      if (anPortComponent.urlPattern().length() > 0)
      {
         if (isJSEEndpoint)
            log.warn("@PortComponent.urlPattern is only valid on EJB endpoints");

         if (isJSEEndpoint == false)
         {
            String urlPattern = anPortComponent.urlPattern();
            sepMetaData.setURLPattern(urlPattern);
         }
      }

      // auth-method
      if (anPortComponent.authMethod().length() > 0)
      {
         if (isJSEEndpoint)
            log.warn("@PortComponent.authMethod is only valid on EJB endpoints");

         if (isJSEEndpoint == false)
         {
            String authMethod = anPortComponent.authMethod();
            sepMetaData.setAuthMethod(authMethod);
         }
      }

      // transport-guarantee
      if (anPortComponent.transportGuarantee().length() > 0)
      {
         if (isJSEEndpoint)
            log.warn("@PortComponent.transportGuarantee is only valid on EJB endpoints");

         if (isJSEEndpoint == false)
         {
            String transportGuarantee = anPortComponent.transportGuarantee();
            sepMetaData.setTransportGuarantee(transportGuarantee);
         }
      }
      
      // secure wsdl access
      sepMetaData.setSecureWSDLAccess(anPortComponent.secureWSDLAccess());

      // virtual hosts
      String[] virtualHosts = anPortComponent.virtualHosts();
      if (virtualHosts != null & virtualHosts.length > 0)
      {
         sepMetaData.setVirtualHosts(virtualHosts);
      }

   }
}
