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
// $Id$
package org.jboss.ws.core.jaxrpc.client;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.ServiceEndpoint;
import org.jboss.ws.core.server.ServiceEndpointManager;
import org.jboss.ws.core.server.ServiceEndpointManagerFactory;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedCallPropertyMetaData;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.tools.wsdl.WSDL11DefinitionFactory;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.Service;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.Remote;
import java.util.*;

/**
 * This ServiceObjectFactory reconstructs a javax.xml.rpc.Service
 * for a given WSDL when the webservice client does a JNDI lookup
 * <p/>
 * It uses the information provided by the service-ref element in application-client.xml
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class ServiceObjectFactory implements ObjectFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceObjectFactory.class);

   /**
    * Creates an object using the location or reference information specified.
    * <p/>
    *
    * @param obj         The possibly null object containing location or reference
    *                    information that can be used in creating an object.
    * @param name        The name of this object relative to <code>nameCtx</code>,
    *                    or null if no name is specified.
    * @param nameCtx     The context relative to which the <code>name</code>
    *                    parameter is specified, or null if <code>name</code> is
    *                    relative to the default initial context.
    * @param environment The possibly null environment that is used in
    *                    creating the object.
    * @return The object created; null if an object cannot be created.
    * @throws Exception if this object factory encountered an exception
    *                   while attempting to create an object, and no other object factories are
    *                   to be tried.
    * @see javax.naming.spi.NamingManager#getObjectInstance
    * @see javax.naming.spi.NamingManager#getURLContext
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      try
      {
         Reference ref = (Reference)obj;
         
         // Unmarshall the ServiceRefMetaData
         UnifiedServiceRefMetaData serviceRef = null;
         RefAddr metaRefAddr = ref.get(ServiceReferenceable.SERVICE_REF_META_DATA);
         ByteArrayInputStream bais = new ByteArrayInputStream((byte[])metaRefAddr.getContent());
         try
         {
            ObjectInputStream ois = new ObjectInputStream(bais);
            serviceRef = (UnifiedServiceRefMetaData)ois.readObject();
            ois.close();
         }
         catch (IOException ex)
         {
            NamingException ne = new NamingException("Cannot unmarshall service ref meta data");
            ne.setRootCause(ex);
            throw ne;
         }

         // Unmarshall the WSSecurityConfiguration
         WSSecurityConfiguration securityConfig = null;
         RefAddr wsseRefAddr = ref.get(ServiceReferenceable.SECURITY_CONFIG);
         if (wsseRefAddr != null)
         {
            bais = new ByteArrayInputStream((byte[])wsseRefAddr.getContent());
            try
            {
               ObjectInputStream ois = new ObjectInputStream(bais);
               securityConfig = (WSSecurityConfiguration)ois.readObject();
               ois.close();
            }
            catch (IOException e)
            {
               throw new NamingException("Cannot unmarshall security config, cause: " + e.toString());
            }
         }

         ServiceImpl jaxrpcService = null;
         URL wsdlLocation = serviceRef.getWsdlLocation();
         if (wsdlLocation != null)
         {
            if(log.isDebugEnabled()) log.debug("Create jaxrpc service from wsdl");

            // Create the actual service object
            QName serviceName = serviceRef.getServiceQName();
            JavaWsdlMapping javaWsdlMapping = getJavaWsdlMapping(serviceRef);
            jaxrpcService = new ServiceImpl(serviceName, wsdlLocation, javaWsdlMapping, securityConfig, serviceRef);
         }
         else
         {
            if(log.isDebugEnabled()) log.debug("Create jaxrpc service with no wsdl");
            jaxrpcService = new ServiceImpl(new QName(Constants.NS_JBOSSWS_URI, "AnonymousService"));
         }

         ServiceMetaData serviceMetaData = jaxrpcService.getServiceMetaData();
         
         // Set any service level properties
         if (serviceRef.getCallProperties().size() > 0)
         {
            Properties callProps = new Properties();
            serviceMetaData.setProperties(callProps);
            for (UnifiedCallPropertyMetaData prop : serviceRef.getCallProperties())
               callProps.setProperty(prop.getPropName(), prop.getPropValue());
         }

         // The web service client using a port-component-link, the contet is the URL to
         // the PortComponentLinkServlet that will return the actual endpoint address
         RefAddr pcLinkRef = ref.get(ServiceReferenceable.PORT_COMPONENT_LINK);
         if (pcLinkRef != null)
         {
            String pcLink = (String)pcLinkRef.getContent();
            if(log.isDebugEnabled()) log.debug("Resolving port-component-link: " + pcLink);

            // First try to obtain the endpoint address loacally
            String endpointAddress = null;
            try
            {
               ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
               ServiceEndpointManager epManager = factory.getServiceEndpointManager();
               ServiceEndpoint serviceEndpoint = epManager.resolvePortComponentLink(pcLink);
               if (serviceEndpoint == null)
                  throw new WSException("Cannot resolve port-component-link: " + pcLink);

               endpointAddress = serviceEndpoint.getServiceEndpointInfo().getServerEndpointMetaData().getEndpointAddress();
            }
            catch (Throwable ex)
            {
               // ignore, we are probably a remote client
            }

            // We may be remote in the esoteric case where an appclient uses the port-comonent-link feature
            if (endpointAddress == null)
            {
               String servletPath = (String)ref.get(ServiceReferenceable.PORT_COMPONENT_LINK_SERVLET).getContent();
               servletPath += "?pcLink=" + URLEncoder.encode(pcLink, "UTF-8");
               InputStream is = new URL(servletPath).openStream();
               BufferedReader br = new BufferedReader(new InputStreamReader(is));
               endpointAddress = br.readLine();
               is.close();
            }

            if(log.isDebugEnabled()) log.debug("Resolved to: " + endpointAddress);
            if (serviceMetaData.getEndpoints().size() == 1)
            {
               EndpointMetaData epMetaData = serviceMetaData.getEndpoints().get(0);
               epMetaData.setEndpointAddress(endpointAddress);
            }
            else
            {
               log.warn("Cannot set endpoint address for port-component-link, unsuported number of endpoints");
            }
         }

         //narrowPortSelection(serviceRef, serviceMetaData);

         /********************************************************
          * Setup the Proxy that implements the service-interface
          ********************************************************/

         // load the service interface class
         ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
         Class siClass = contextCL.loadClass(serviceRef.getServiceInterface());
         if (Service.class.isAssignableFrom(siClass) == false)
            throw new JAXRPCException("The service interface does not implement javax.xml.rpc.Service: " + siClass.getName());

         // load all service endpoint interface classes
         for (UnifiedPortComponentRefMetaData pcr : serviceRef.getPortComponentRefs())
         {
            Class seiClass = contextCL.loadClass(pcr.getServiceEndpointInterface());
            if (Remote.class.isAssignableFrom(seiClass) == false)
               throw new IllegalArgumentException("The SEI does not implement java.rmi.Remote: " + seiClass.getName());
         }

         // Setup the handler chain
         setupHandlerChain(jaxrpcService);

         InvocationHandler handler = new ServiceProxy(jaxrpcService, siClass);
         return Proxy.newProxyInstance(contextCL, new Class[] { siClass, ServiceExt.class }, handler);
      }
      catch (Exception ex)
      {
         log.error("Cannot create service", ex);         
         throw ex;
      }
   }

   /**
    * Narrow port selection tries to nail down available endpoints
    * by <port-component-ref> declarations. A selection base upon
    * Service.getPort(SEI) later on needs to be distinct to an endpoint, or in this case EndpointMetaData.
    * 
    * @param serviceRef
    * @param serviceMetaData
    */
   private void narrowPortSelection(UnifiedServiceRefMetaData serviceRef, ServiceMetaData serviceMetaData)
   {
      Map<String, List<UnifiedPortComponentRefMetaData>> seiGroups = new HashMap<String, List<UnifiedPortComponentRefMetaData>>();
      for(UnifiedPortComponentRefMetaData pcref : serviceRef.getPortComponentRefs())
      {
         String sei = pcref.getServiceEndpointInterface();
         if(null==seiGroups.get(sei))
            seiGroups.put(sei, new ArrayList<UnifiedPortComponentRefMetaData>());
         List<UnifiedPortComponentRefMetaData> group = seiGroups.get(sei);
         group.add(pcref);

         // Constraint#1: within a service-ref it's not allowed to use a SEI across different pcref's
         if(group.size()>1)
            throw new WSException("Within a <service-ref> it's not allowed to use a SEI across different <port-component-ref>'s: "+group);
      }

      // Constraint#2: A pcref may only match one EndpointMetaData
      for( String sei : seiGroups.keySet())
      {
         // Narrow available endpoints by port-component-ref declaration
         List<QName> narrowedEndpoints = new ArrayList<QName>();

         List<UnifiedPortComponentRefMetaData> group = seiGroups.get(sei);
         UnifiedPortComponentRefMetaData pcref = group.get(0); // distinct, see above Constraint#1

         // Constraint#3: Port selection only applies when both SEI and QName are given
         if(pcref.getServiceEndpointInterface()!=null && pcref.getPortQName()!=null)
         {
            List<QName> pcRef2EndpointMapping = new ArrayList<QName>();
            for(EndpointMetaData epMeta : serviceMetaData.getEndpoints())
            {
               if(pcref.getServiceEndpointInterface().equals(epMeta.getServiceEndpointInterfaceName()))
               {
                  pcRef2EndpointMapping.add(epMeta.getPortName());
               }

            }

            for(QName q : pcRef2EndpointMapping)
            {
               EndpointMetaData mappedEndpoint = serviceMetaData.getEndpoint(q);
               if(! pcref.getPortQName().equals( mappedEndpoint.getPortName()) )
                  narrowedEndpoints.add(q);
            }

            // Constraint: Dont exclude all of them ;)
            if(pcRef2EndpointMapping.size()>0 && (pcRef2EndpointMapping.size() == narrowedEndpoints.size()))
               throw new WSException("Failed to narrow available endpoints by <port-component-ref> declaration");

            for(QName q : narrowedEndpoints)
            {
               EndpointMetaData removed = serviceMetaData.removeEndpoint(q);
               log.debug("Narrowed endpoint " + q + "("+removed+")");
            }
         }
         else
         {
            // TODO: In case there is more then one EMPD this should cause an exception
            log.warn("Unable to narrow port selection for "+ pcref);
         }

      }
   }

   /**
    * Setup the handler chain(s) for this service
    */
   private void setupHandlerChain(ServiceImpl jaxrpcService) throws Exception
   {
      List<EndpointMetaData> endpoints = jaxrpcService.getServiceMetaData().getEndpoints();
      for (EndpointMetaData epMetaData : endpoints)
      {
         jaxrpcService.setupHandlerChain(epMetaData);
      }
   }

   private JavaWsdlMapping getJavaWsdlMapping(UnifiedServiceRefMetaData serviceRef)
   {
      JavaWsdlMapping javaWsdlMapping = null;
      if (serviceRef.getMappingFile() != null)
      {
         String mappingFile = serviceRef.getMappingFile();
         try
         {
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            URL mappingURL = serviceRef.getVfsRoot().findChild(mappingFile).toURL();
            javaWsdlMapping = mappingFactory.parse(mappingURL);
         }
         catch (Exception e)
         {
            throw new WSException("Cannot unmarshal jaxrpc-mapping-file: " + mappingFile, e);
         }
      }
      return javaWsdlMapping;
   }

   private Definition getWsdlDefinition(UnifiedServiceRefMetaData serviceRef)
   {
      Definition wsdlDefinition = null;
      {
         URL wsdlLocation = serviceRef.getWsdlLocation();
         if (wsdlLocation != null)
         {
            try
            {
               WSDL11DefinitionFactory factory = WSDL11DefinitionFactory.newInstance();
               wsdlDefinition = factory.parse(wsdlLocation);
            }
            catch (Exception e)
            {
               throw new WSException("Cannot unmarshall wsdl-file: " + wsdlLocation, e);
            }
         }
      }
      return wsdlDefinition;
   }

}
