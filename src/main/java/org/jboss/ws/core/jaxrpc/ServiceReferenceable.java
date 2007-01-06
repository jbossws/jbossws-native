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
package org.jboss.ws.core.jaxrpc;

// $Id$

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import org.jboss.logging.Logger;
import org.jboss.ws.core.server.ServiceEndpointManager;
import org.jboss.ws.core.server.ServiceEndpointManagerFactory;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.j2ee.UnifiedPortComponentRefMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityOMFactory;

/**
 * A JNDI reference to a javax.xml.rpc.Service
 * <p/>
 * It holds the information to reconstrut the javax.xml.rpc.Service
 * when the client does a JNDI lookup.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class ServiceReferenceable implements Referenceable
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceReferenceable.class);
   
   public static final String SERVICE_REF_META_DATA = "SERVICE_REF_META_DATA";
   public static final String DEPLOYMENT_URL = "DEPLOYMENT_URL";
   public static final String SECURITY_CONFIG = "SECURITY_CONFIG";
   public static final String DESCRIPTOR_LOC = "DESCRIPTOR_LOC";
   public static final String PORT_COMPONENT_LINK = "PORT_COMPONENT_LINK";
   public static final String PORT_COMPONENT_LINK_SERVLET = "PORT_COMPONENT_LINK_SERVLET";

   private UnifiedServiceRefMetaData refMetaData;
   private UnifiedDeploymentInfo udi;

   /**
    * A service referenceable for a WSDL document that is part of the deployment
    *
    * @param refMetaData The service-ref meta data
    * @param udi          The client DeploymentInfo
    */
   public ServiceReferenceable(UnifiedServiceRefMetaData refMetaData, UnifiedDeploymentInfo udi)
   {
      this.refMetaData = refMetaData;
      this.udi = udi;
   }

   /**
    * Retrieves the Reference of this object.
    *
    * @return The non-null Reference of this object.
    * @throws javax.naming.NamingException If a naming exception was encountered while retrieving the reference.
    */
   public Reference getReference() throws NamingException
   {
      Reference myRef = new Reference(ServiceReferenceable.class.getName(), ServiceObjectFactory.class.getName(), null);

      // The deployment URL of the web service client deployment
      // Add a reference to the client deployment URL
      String deploymentID = udi.name;
      myRef.add(new StringRefAddr(DEPLOYMENT_URL, deploymentID));

      // Add a reference to the ServiceRefMetaData and WSDLDefinitions
      myRef.add(new BinaryRefAddr(SERVICE_REF_META_DATA, marshallServiceRef()));

      // FIXME: JBWS-1431 Merge ws-security config with jaxrpc/jaxws config
      if (getSecurityConfig() != null)
         myRef.add(new BinaryRefAddr(SECURITY_CONFIG, marshallSecurityConfig()));

      // Add references to port component links
      UnifiedPortComponentRefMetaData[] pcrArr = refMetaData.getPortComponentRefs();
      for (int i = 0; i < pcrArr.length; i++)
      {
         UnifiedPortComponentRefMetaData pcr = pcrArr[i];
         String pcLink = pcr.getPortComponentLink();
         if (pcLink != null)
         {
            myRef.add(new StringRefAddr(PORT_COMPONENT_LINK, pcLink));
            try
            {
               ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
               ServiceEndpointManager epManager = factory.getServiceEndpointManager();
               String host = epManager.getWebServiceHost();
               int port = epManager.getWebServicePort();

               String servletURL = "http://" + host + ":" + port + "/jbossws/pclink";
               myRef.add(new StringRefAddr(PORT_COMPONENT_LINK_SERVLET, servletURL));
            }
            catch (Exception ex)
            {
               throw new NamingException("Cannot obtain path to PortComponentLinkServlet: " + ex);
            }
         }
      }

      return myRef;
   }

   /** Marshall the ServiceRefMetaData to an byte array
    */
   private byte[] marshallServiceRef() throws NamingException
   {
      refMetaData.getJavaWsdlMapping();

      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(refMetaData);
         oos.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot marshall service ref meta data, cause: " + e.toString());
      }
      return baos.toByteArray();
   }

   /** Marshall the WSSecurityConfiguration to an byte array
    */
   private byte[] marshallSecurityConfig() throws NamingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         URL vfConfig = getSecurityConfig();
         WSSecurityConfiguration securityConfig = WSSecurityOMFactory.newInstance().parse(vfConfig);
         oos.writeObject(securityConfig);
         oos.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot marshall security config, cause: " + e.toString());
      }
      return baos.toByteArray();
   }

   private URL getSecurityConfig()
   {
      String descriptorPath = udi.metaData instanceof UnifiedWebMetaData ? "WEB-INF" : "META-INF";
      descriptorPath = descriptorPath + "/" + WSSecurityOMFactory.CLIENT_RESOURCE_NAME;
      try
      {
         URL vfConfig = udi.getMetaDataFileURL(descriptorPath);
         InputStream inputStream = vfConfig.openStream();
         inputStream.close();
         return vfConfig;
      }
      catch (IOException ex)
      {
         log.debug("Cannot find security config: " + descriptorPath);
         return null;
      }
   }
}