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
package org.jboss.test.ws.jaxws.wsrm.emulator.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.test.ws.jaxws.wsrm.emulator.utils.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 7, 2007
 */
final class ResponseImpl implements Response
{
   
   private final String resource;
   private final String statusCode;
   private final String contentType;
   private final Map<String, String> properties;
   
   ResponseImpl(Element e, Map<String, String> namespaces)
   {
      this.resource = e.getAttribute("resource");
      this.statusCode = e.getAttribute("statusCode");
      this.contentType = e.getAttribute("contentType");
      NodeList setNodes = e.getElementsByTagName("set");
      if ((setNodes != null) && (setNodes.getLength() > 0))
      {
         Map<String, String> toFill = new HashMap<String, String>();
         this.properties = Collections.unmodifiableMap(toFill);
         for (int i = 0; i < setNodes.getLength(); i++)
         {
            String key = ((Element)setNodes.item(i)).getAttribute("property");
            String val = ((Element)setNodes.item(i)).getAttribute("value");
            toFill.put(key, replace(val, namespaces));
         }
      }
      else
      {
         this.properties = Collections.emptyMap();
      }
   }

   private static String replace(String s, Map<String, String> namespaces)
   {
      for (Iterator<String> i = namespaces.keySet().iterator(); i.hasNext(); )
      {
         String key = i.next();
         String val = namespaces.get(key);
         s = StringUtil.replace("${" + key + "}", val, s);
      }
      return s;
   }
   
   public String getContentType()
   {
      return this.contentType;
   }

   public Map<String, String> getProperties()
   {
      return this.properties;
   }

   public String getResource()
   {
      return this.resource;
   }

   public String getStatusCode()
   {
      return this.statusCode;
   }
   
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("RESPONSE {");
      sb.append("resource=" + this.resource + ", ");
      sb.append("statusCode=" + this.statusCode + ", ");
      sb.append("contentType=" + this.contentType + ", ");
      sb.append("properties=" + this.properties + "}");
      return sb.toString();
   }
   
}
