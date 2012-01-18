/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.metadata.builder.jaxws;

import static org.jboss.ws.common.integration.WSHelper.isJaxwsJseDeployment;

import java.util.ResourceBundle;

import javax.jws.WebService;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesFactory;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;

/**
 * Builds ServiceEndpointMetaData for a JAX-WS endpoint.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 */
public abstract class JAXWSServerMetaDataBuilder extends JAXWSMetaDataBuilder
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JAXWSServerMetaDataBuilder.class);
   static void setupProviderOrWebService(ArchiveDeployment dep, UnifiedMetaData umd, Class<?> beanClass, String beanName) throws Exception
   {
      if (beanClass.isAnnotationPresent(WebService.class))
      {
         JAXWSWebServiceMetaDataBuilder builder = new JAXWSWebServiceMetaDataBuilder();
         builder.buildWebServiceMetaData(dep, umd, beanClass, beanName);
      }
      else if (beanClass.isAnnotationPresent(WebServiceProvider.class))
      {
         JAXWSProviderMetaDataBuilder builder = new JAXWSProviderMetaDataBuilder();
         builder.buildProviderMetaData(dep, umd, beanClass, beanName);
      }
   }

   protected void processEndpointConfig(Deployment dep, ServerEndpointMetaData sepMetaData, Class<?> wsClass, String linkName)
   {
      String configName = null;
      String configFile = null;
      
      EndpointConfig anEndpointConfig = wsClass.getAnnotation(EndpointConfig.class);
      if (anEndpointConfig != null)
      {
         if (anEndpointConfig.configName().length() > 0)
            configName = anEndpointConfig.configName();

         if (anEndpointConfig.configFile().length() > 0)
            configFile = anEndpointConfig.configFile();
      }
      
      JSEArchiveMetaData jseMetaData = dep.getAttachment(JSEArchiveMetaData.class);
      if (jseMetaData != null)
      {
         if (jseMetaData.getConfigName() != null)
            configName = jseMetaData.getConfigName();
         if (jseMetaData.getConfigFile() != null)
            configFile = jseMetaData.getConfigFile();
      }
      
      EJBArchiveMetaData ejbMetaData = dep.getAttachment(EJBArchiveMetaData.class);
      if (ejbMetaData != null)
      {
         if (ejbMetaData.getConfigName() != null)
            configName = ejbMetaData.getConfigName();
         if (ejbMetaData.getConfigFile() != null)
            configFile = ejbMetaData.getConfigFile();
      }
      
      if (configName != null || configFile != null)
         sepMetaData.setConfigName(configName, configFile);
   }

   protected void processWebContext(Deployment dep, Class<?> wsClass, String linkName, ServerEndpointMetaData sepMetaData)
   {
      WebContext anWebContext = wsClass.getAnnotation(WebContext.class);

      if (anWebContext == null)
         return;

      boolean isJSEEndpoint = isJaxwsJseDeployment(dep);

      // context-root
      if (anWebContext.contextRoot().length() > 0)
      {
         if (isJSEEndpoint)
         {
            log.warn(BundleUtils.getMessage(bundle, "CONTEXTROOT_IS_ONLY_VALID_ON_EJB_ENDPOINTS"));
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
            log.warn(BundleUtils.getMessage(bundle, "URLPATTERN_IS_ONLY_VALID_ON_EJB_ENDPOINTS"));
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
            log.warn(BundleUtils.getMessage(bundle, "AUTHMETHOD_IS_ONLY_VALID_ON_EJB_ENDPOINTS"));
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
            log.warn(BundleUtils.getMessage(bundle, "TRANSPORTGUARANTEE_IS_ONLY_VALID_ON_EJB_ENDPOINTS"));
         }
         else
         {
            String transportGuarantee = anWebContext.transportGuarantee();
            sepMetaData.setTransportGuarantee(transportGuarantee);
         }
      }

      // secure wsdl access
      sepMetaData.setSecureWSDLAccess(anWebContext.secureWSDLAccess());
   }
   
   /**
    * With JAX-WS the use of webservices.xml is optional since the annotations can be used
    * to specify most of the information specified in this deployment descriptor file.
    * The deployment descriptors are only used to override or augment the annotation member attributes.
    * @param sepMetaData
    */
   protected void processWSDDContribution(Deployment dep, ServerEndpointMetaData sepMetaData)
   {
      WebservicesMetaData webservices = dep.getAttachment(WebservicesMetaData.class);
      if (webservices != null)
      {
         for (WebserviceDescriptionMetaData wsDesc : webservices.getWebserviceDescriptions())
         {
            for (PortComponentMetaData portComp : wsDesc.getPortComponents())
            {
               // We match portComp's by SEI first and portQName second
               // In the first case the portComp may override the portQName that derives from the annotation
               String portCompSEI = portComp.getServiceEndpointInterface();
               boolean doesMatch = portCompSEI != null ? portCompSEI.equals(sepMetaData.getServiceEndpointInterfaceName()) : false;
               if (!doesMatch)
               {
                  doesMatch = portComp.getWsdlPort().equals(sepMetaData.getPortName());
               }

               if (doesMatch)
               {

                  log.debug("Processing 'webservices.xml' contributions on EndpointMetaData");

                  // PortQName overrides
                  if (portComp.getWsdlPort() != null)
                  {
                     if (log.isDebugEnabled())
                        log.debug("Override EndpointMetaData portName " + sepMetaData.getPortName() + " with " + portComp.getWsdlPort());
                     sepMetaData.setPortName(portComp.getWsdlPort());
                  }

                  // HandlerChain contributions
                  UnifiedHandlerChainsMetaData chainWrapper = portComp.getHandlerChains();
                  if (chainWrapper != null)
                  {
                     for (UnifiedHandlerChainMetaData handlerChain : chainWrapper.getHandlerChains())
                     {
                        for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
                        {
                           if (log.isDebugEnabled())
                              log.debug("Contribute handler from webservices.xml: " + uhmd.getHandlerName());
                           HandlerMetaDataJAXWS hmd = HandlerMetaDataJAXWS.newInstance(uhmd, HandlerType.ENDPOINT);
                           sepMetaData.addHandler(hmd);
                        }
                     }
                  }

                  // MTOM settings
                  if (portComp.isMtomEnabled())
                  {
                     log.debug("Enabling MTOM");
                     MTOMFeature feature = new MTOMFeature(true, portComp.getMtomThreshold());
                     sepMetaData.getFeatures().addFeature(feature);
                  }
                  
                  if (portComp.isAddressingEnabled()) 
                  {  log.debug("Enabling Addressing");
                     AddressingFeature.Responses responses = getAddressFeatureResponses(portComp.getAddressingResponses());              
                     AddressingFeature feature = new AddressingFeature(true, portComp.isAddressingRequired(), responses);
                     sepMetaData.getFeatures().addFeature(feature);
                  }
                  
                  if (portComp.isRespectBindingEnabled()) 
                  {
                     log.debug("Enabling RespectBinding Feature");
                     RespectBindingFeature feature = new RespectBindingFeature(true);
                     sepMetaData.getFeatures().addFeature(feature);
                  }

                  //wsdlLocation override
                  String wsdlFile = portComp.getWebserviceDescription().getWsdlFile();
                  if (wsdlFile != null)
                  {
                     if (log.isDebugEnabled())
                        log.debug("Override wsdlFile location with " + wsdlFile);
                     sepMetaData.getServiceMetaData().setWsdlFile(wsdlFile);
                  }
               }
            }
         }

      }
   }
}
