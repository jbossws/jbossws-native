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

// $Id$

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.ws.annotation.WebContext;
import org.jboss.ws.integration.deployment.UnifiedDeploymentInfo;
import org.jboss.ws.integration.deployment.Deployment.DeploymentType;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * Builds ServiceEndpointMetaData for a JAX-WS endpoint.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Thomas.Diesler@jboss.com
 */
public abstract class JAXWSServerMetaDataBuilder extends JAXWSMetaDataBuilder
{
   static void setupProviderOrWebService(UnifiedMetaData umd, UnifiedDeploymentInfo udi, Class<?> beanClass, String beanName) throws Exception
   {
      if (beanClass.isAnnotationPresent(WebService.class))
      {
         JAXWSWebServiceMetaDataBuilder builder = new JAXWSWebServiceMetaDataBuilder();
         builder.buildWebServiceMetaData(umd, udi, beanClass, beanName);
      }
      else if (beanClass.isAnnotationPresent(WebServiceProvider.class))
      {
         JAXWSProviderMetaDataBuilder builder = new JAXWSProviderMetaDataBuilder();
         builder.buildProviderMetaData(umd, udi, beanClass, beanName);
      }
   }

   protected void processEndpointConfig(UnifiedDeploymentInfo udi, Class<?> wsClass, String linkName, ServerEndpointMetaData sepMetaData)
   {
      EndpointConfig anEndpointConfig = wsClass.getAnnotation(EndpointConfig.class);

      if (anEndpointConfig== null)
         return;

      String configName = null;
      String configFile = null;
      
      if (anEndpointConfig.configName().length() > 0)
         configName = anEndpointConfig.configName();

      if (anEndpointConfig.configFile().length() > 0)
         configFile = anEndpointConfig.configFile();

      if (configName != null || configFile != null)
         sepMetaData.setConfigName(configName, configFile);
   }

   protected void processWebContext(UnifiedDeploymentInfo udi, Class<?> wsClass, String linkName, ServerEndpointMetaData sepMetaData)
   {
      WebContext anWebContext = wsClass.getAnnotation(WebContext.class);

      if (anWebContext == null)
         return;
      
      boolean isJSEEndpoint = (udi.type == DeploymentType.JAXWS_JSE);

      // context-root
      if (anWebContext.contextRoot().length() > 0)
      {
         if (isJSEEndpoint)
         {
            log.warn("@WebContext.contextRoot is only valid on EJB endpoints");
         }
         else
         {
            String contextRoot = anWebContext.contextRoot();
            if (contextRoot.startsWith("/") == false)
               contextRoot = "/" + contextRoot;

            sepMetaData.setContextRoot(contextRoot);
         }
      }

      // url-pattern
      if (anWebContext.urlPattern().length() > 0)
      {
         if (isJSEEndpoint)
         {
            log.warn("@WebContext.urlPattern is only valid on EJB endpoints");
         }
         else
         {
            String urlPattern = anWebContext.urlPattern();
            sepMetaData.setURLPattern(urlPattern);
         }
      }

      // auth-method
      if (anWebContext.authMethod().length() > 0)
      {
         if (isJSEEndpoint)
         {
            log.warn("@WebContext.authMethod is only valid on EJB endpoints");
         }
         else
         {
            String authMethod = anWebContext.authMethod();
            sepMetaData.setAuthMethod(authMethod);
         }
      }

      // transport-guarantee
      if (anWebContext.transportGuarantee().length() > 0)
      {
         if (isJSEEndpoint)
         {
            log.warn("@WebContext.transportGuarantee is only valid on EJB endpoints");
         }
         else
         {
            String transportGuarantee = anWebContext.transportGuarantee();
            sepMetaData.setTransportGuarantee(transportGuarantee);
         }
      }
      
      // secure wsdl access
      sepMetaData.setSecureWSDLAccess(anWebContext.secureWSDLAccess());

      // virtual hosts
      String[] virtualHosts = anWebContext.virtualHosts();
      if (virtualHosts != null & virtualHosts.length > 0)
      {
         sepMetaData.setVirtualHosts(virtualHosts);
      }
   }
}
