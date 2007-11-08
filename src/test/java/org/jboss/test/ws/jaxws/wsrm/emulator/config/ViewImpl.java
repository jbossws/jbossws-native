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

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 7, 2007
 */
final class ViewImpl implements View
{

   private final String id;
   private final Request req;
   private final Response res;
   
   ViewImpl(Element e, Map<String, String> namespaces)
   {
      this.id = e.getAttribute("id");
      NodeList response = e.getElementsByTagName("response");
      this.res = (response == null) ? null : ObjectFactory.getResponse((Element)response.item(0), namespaces); 
      NodeList request = e.getElementsByTagName("request");
      this.req = (request == null) ? null : ObjectFactory.getRequest((Element)request.item(0), namespaces); 
   }
   
   public String getId()
   {
      return this.id;
   }

   public Request getRequest()
   {
      return this.req;
   }

   public Response getResponse()
   {
      return this.res;
   }
   
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("VIEW {");
      sb.append("id=" + id + ", ");
      sb.append("request=" + req + ", ");
      sb.append("response=" + res + "}");
      return sb.toString();
   }

}
