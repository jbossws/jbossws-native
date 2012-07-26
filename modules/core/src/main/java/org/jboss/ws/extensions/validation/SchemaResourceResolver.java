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
package org.jboss.ws.extensions.validation;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.ws.core.utils.JBossWSEntityResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
/**
 * SchemaResourceResolver
 * 
 * @author ema@redhat.com
 */

public class SchemaResourceResolver implements LSResourceResolver
{
   private static Logger log = Logger.getLogger(SchemaResourceResolver.class);
   private Map<String, byte[]> streamMap;

   public SchemaResourceResolver(Map<String, byte[]> map)
   {
      streamMap = map;
   }

   public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
   {
      LSInput lsInput = null;
      if (streamMap.get(namespaceURI) != null)
      {
         byte[] value = streamMap.get(namespaceURI);
         lsInput = new LSInputImpl();
         lsInput.setByteStream(new ByteArrayInputStream(value));
      }
      try
      {
         JBossWSEntityResolver entityResolver = new JBossWSEntityResolver();
         InputSource ins = entityResolver.resolveEntity(publicId, systemId);
         if (ins != null)
         {
            lsInput = new LSInputImpl();
            lsInput.setByteStream(ins.getByteStream());
         }
      }
      catch (Exception e)
      {
         log.warn("Failed to resolve the schema", e);
         
      }
      return lsInput;
   }

}
