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
package org.jboss.ws.core.utils;

// $Id$

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * Dynamically register the JBossWS entities.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Aug-2006
 */
public class JBossWSEntityResolver extends JBossEntityResolver
{
   // provide logging
   private static final Logger log = Logger.getLogger(JBossWSEntityResolver.class);

   public JBossWSEntityResolver()
   {
      registerEntity("urn:jboss:jaxrpc-config:2.0", "schema/jaxrpc-config_2_0.xsd");
      registerEntity("urn:jboss:jaxws-config:2.0", "schema/jaxws-config_2_0.xsd");
      registerEntity("http://java.sun.com/xml/ns/javaee", "schema/javaee_web_services_1_2.xsd");
      registerEntity("http://www.w3.org/2005/08/addressing", "schema/ws-addr.xsd");
      registerEntity("http://schemas.xmlsoap.org/ws/2004/08/eventing", "eventing.xsd");
   }

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      log.debug("resolveEntity: [pub=" + publicId + ",sysid=" + systemId + "]");
      InputSource inputSource = super.resolveEntity(publicId, systemId);

      if (inputSource == null)
      {
         inputSource = resolveSystemIDasURL(systemId, log.isTraceEnabled());
      }

      return inputSource;
   }

   /** This method should be protected in the super class. */
   protected InputSource resolveSystemIDasURL(String systemId, boolean trace)
   {
      if (systemId == null)
         return null;

      if (trace)
         log.trace("resolveSystemIDasURL, systemId=" + systemId);

      InputSource inputSource = null;

      // Try to use the systemId as a URL to the schema
      try
      {
         if (trace)
            log.trace("Trying to resolve systemId as a URL");

         URL url = new URL(systemId);
         if (url.getProtocol().equalsIgnoreCase("file") == false)
         {
            log.warn("Trying to resolve systemId as a non-file URL: " + systemId);
         }

         InputStream ins = new ResourceURL(url).openStream();
         if (ins != null)
         {
            inputSource = new InputSource(ins);
            inputSource.setSystemId(systemId);
         }
         else
         {
            log.warn("Cannot load systemId as URL: " + systemId);
         }

         if (trace)
            log.trace("Resolved systemId as a URL");
      }
      catch (MalformedURLException ignored)
      {
         if (trace)
            log.trace("SystemId is not a url: " + systemId, ignored);
      }
      catch (IOException e)
      {
         if (trace)
            log.trace("Failed to obtain URL.InputStream from systemId: " + systemId, e);
      }
      return inputSource;
   }
}
