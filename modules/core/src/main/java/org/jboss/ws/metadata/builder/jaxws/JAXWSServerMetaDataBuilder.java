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

import javax.jws.WebService;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceProvider;

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
