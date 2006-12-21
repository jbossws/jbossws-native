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
package org.jboss.ws.integration.jboss42;

// $Id$

import java.net.URL;
import java.util.Iterator;

import javax.naming.Context;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.rpc.JAXRPCException;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.naming.Util;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.webservice.metadata.serviceref.ServiceRefMetaData;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.ServiceReferenceable;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCClientDeployment;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.tools.wsdl.WSDL11DefinitionFactory;

/**
 * Binds a JAXRPC Service object in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public class WebServiceClientDeployer extends ServiceMBeanSupport implements WebServiceClientDeployerMBean
{
   /**
    * This binds a jaxrpc Service into the callers ENC for every service-ref element
    *
    * @param envCtx      ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param di          The client's deployment info
    * @throws org.jboss.deployment.DeploymentException if it goes wrong
    */
   public void setupServiceRefEnvironment(Context envCtx, Iterator serviceRefs, DeploymentInfo di) throws DeploymentException
   {
      try
      {
         while (serviceRefs.hasNext())
         {
            ServiceRefMetaData serviceRef = (ServiceRefMetaData)serviceRefs.next();
            String serviceRefName = serviceRef.getServiceRefName();

            UnifiedServiceRefMetaData wsServiceRef = ServiceRefMetaDataAdaptor.buildUnifiedServiceRefMetaData(serviceRef);

            JavaWsdlMapping javaWsdlMapping = getJavaWsdlMapping(wsServiceRef);
            wsServiceRef.setJavaWsdlMapping(javaWsdlMapping);

            Definition wsdlDefinition = getWsdlDefinition(wsServiceRef);
            wsServiceRef.setWsdlDefinition(wsdlDefinition);

            // build the container independent deployment info
            UnifiedDeploymentInfo udi = new JAXRPCClientDeployment(UnifiedDeploymentInfo.DeploymentType.JAXRPC_Client);
            DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);

            ServiceReferenceable ref = new ServiceReferenceable(wsServiceRef, udi);
            Util.bind(envCtx, serviceRefName, ref);

            log.debug("Webservice binding: java:comp/env/" + serviceRefName);
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Cannot bind webservice to client environment", e);
      }
   }

   private JavaWsdlMapping getJavaWsdlMapping(UnifiedServiceRefMetaData serviceRef)
   {
      JavaWsdlMapping javaWsdlMapping = null;
      URL mappingURL = serviceRef.getMappingLocation();
      if (mappingURL != null)
      {
         try
         {
            // setup the XML binding Unmarshaller
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(mappingURL);
         }
         catch (Exception e)
         {
            throw new JAXRPCException("Cannot unmarshal jaxrpc-mapping-file: " + mappingURL, e);
         }
      }
      return javaWsdlMapping;
   }

   private Definition getWsdlDefinition(UnifiedServiceRefMetaData serviceRef)
   {
      Definition wsdlDefinition = null;
      {
         URL wsdlOverride = serviceRef.getWsdlOverride();
         URL wsdlURL = serviceRef.getWsdlLocation();
         if (wsdlOverride == null && wsdlURL != null)
         {
            try
            {
               WSDL11DefinitionFactory factory = WSDL11DefinitionFactory.newInstance();
               wsdlDefinition = factory.parse(wsdlURL);
            }
            catch (WSDLException e)
            {
               throw new WSException("Cannot unmarshall wsdl, cause: " + e.toString());
            }
         }
      }
      return wsdlDefinition;
   }
}
