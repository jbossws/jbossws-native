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
import java.util.LinkedList;
import java.util.List;
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
final class RequestImpl implements Request
{
   
   private final String httpMethod;
   private final String pathInfo;
   private final Map<String, String> properties;
   private final List<String> matches;

   RequestImpl(Element e, Map<String, String> namespaces)
   {
      this.httpMethod = e.getAttribute("httpMethod");
      this.pathInfo = e.getAttribute("pathInfo");
      NodeList contains = e.getElementsByTagName("contains");
      if ((contains != null) && (contains.getLength() == 1))
      {
         NodeList nodes = ((Element)contains.item(0)).getElementsByTagName("node");
         List<String> toFill = new LinkedList<String>();
         this.matches = Collections.unmodifiableList(toFill);
         for (int i = 0; i < nodes.getLength(); i++)
         {
            toFill.add(replace(((Element)nodes.item(i)).getAttribute("name"), namespaces));
         }
      }
      else
      {
         this.matches = Collections.emptyList();
      }
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
   
   public String getHttpMethod()
   {
      return this.httpMethod;
   }

   public List<String> getMatches()
   {
      return this.matches;
   }

   public String getPathInfo()
   {
      return this.pathInfo;
   }

   public Map<String, String> getProperties()
   {
      return this.properties;
   }
   
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("REQUEST {");
      sb.append("httpMethod=" + this.httpMethod + ", ");
      sb.append("pathInfo=" + this.pathInfo + ", ");
      sb.append("properties=" + this.properties + ", ");
      sb.append("matches=" + this.matches + "}");
      return sb.toString();
   }

}
