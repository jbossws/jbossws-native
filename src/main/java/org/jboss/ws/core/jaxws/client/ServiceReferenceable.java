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
package org.jboss.ws.core.jaxws.client;

// $Id$

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;



/**
 * A JNDI reference to a javax.xml.ws.Service
 * 
 * It holds the information to reconstrut the javax.xml.ws.Service
 * when the client does a JNDI lookup.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Oct-2006
 */
public class ServiceReferenceable implements Referenceable
{
   public static final String SERVICE_REF = "SERVICE_REF";
   public static final String SERVICE_TYPE = "SERVICE_TYPE";
   public static final String PORT_TYPE = "PORT_TYPE";

   private String serviceType;
   private String portType;
   private UnifiedServiceRef sref;
   
   public ServiceReferenceable(String serviceType, String portType, UnifiedServiceRef sref)
   {
      this.serviceType = serviceType;
      this.portType = portType;
      this.sref = sref;
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

      myRef.add(new StringRefAddr(SERVICE_TYPE, serviceType));
      myRef.add(new StringRefAddr(PORT_TYPE, portType));
      myRef.add(new BinaryRefAddr(SERVICE_REF, marshallServiceRef()));

      return myRef;
   }
   
   private byte[] marshallServiceRef() throws NamingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(sref);
         oos.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot marshall service ref, cause: " + e.toString());
      }
      return baos.toByteArray();
   }
}