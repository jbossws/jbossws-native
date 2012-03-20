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
package org.jboss.ws.core.jaxrpc.client.serviceref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ResourceBundle;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * A JNDI reference to a javax.xml.rpc.Service
 * <p/>
 * It holds the information to reconstrut the javax.xml.rpc.Service
 * when the client does a JNDI lookup.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public final class NativeServiceReferenceableJAXRPC implements Referenceable
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(NativeServiceReferenceableJAXRPC.class);
   public static final String SERVICE_REF_META_DATA = "SERVICE_REF_META_DATA";
   public static final String PORT_COMPONENT_LINK = "PORT_COMPONENT_LINK";
   public static final String PORT_COMPONENT_LINK_SERVLET = "PORT_COMPONENT_LINK_SERVLET";

   private UnifiedServiceRefMetaData refMetaData;

   /**
    * A service referenceable for a WSDL document that is part of the deployment
    */
   public NativeServiceReferenceableJAXRPC(UnifiedServiceRefMetaData refMetaData)
   {
      this.refMetaData = refMetaData;
   }

   /**
    * Retrieves the Reference of this object.
    *
    * @return The non-null Reference of this object.
    * @throws javax.naming.NamingException If a naming exception was encountered while retrieving the reference.
    */
   public Reference getReference() throws NamingException
   {
      Reference myRef = new Reference(NativeServiceReferenceableJAXRPC.class.getName(), NativeServiceObjectFactoryJAXRPC.class.getName(), null);

      // Add a reference to the ServiceRefMetaData and WSDLDefinitions
      myRef.add(new BinaryRefAddr(SERVICE_REF_META_DATA, marshallServiceRef()));

      // Add references to port component links
      for (UnifiedPortComponentRefMetaData pcr : refMetaData.getPortComponentRefs())
      {
         String pcLink = pcr.getPortComponentLink();
         if (pcLink != null)
         {
            myRef.add(new StringRefAddr(PORT_COMPONENT_LINK, pcLink));
            try
            {
               SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
               ServerConfig config = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
               
               String host = config.getWebServiceHost();
               int port = config.getWebServicePort();

               String servletURL = "http://" + host + ":" + port + "/jbossws/pclink";
               myRef.add(new StringRefAddr(PORT_COMPONENT_LINK_SERVLET, servletURL));
            }
            catch (Exception ex)
            {
               throw new NamingException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_PATH",  ex));
            }
         }
      }

      return myRef;
   }

   /** Marshall the ServiceRefMetaData to an byte array
    */
   private byte[] marshallServiceRef() throws NamingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(refMetaData);
         oos.close();
      }
      catch (IOException e)
      {
         throw new NamingException(BundleUtils.getMessage(bundle, "CANNOT_MARSHALL_SERVICE_REF_META_DATA",  e.toString()));
      }
      return baos.toByteArray();
   }

}
