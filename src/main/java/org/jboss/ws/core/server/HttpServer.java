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
package org.jboss.ws.core.server;

// $Id$

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import org.jboss.wsf.spi.utils.ServiceLoader;

/**
 * An abstract HTTP Server
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Jul-2006
 */
public abstract class HttpServer 
{
   public static String HTTP_SERVER_PROPERTY = HttpServer.class.getName();
   
   private Map<String, Object> properties = new HashMap<String, Object>();
   
   // Hide constructor
   protected HttpServer ()
   {
   }
   
   /** 
    * Create an instance of an HTTP server. 
    * The discovery algorithm is described in {@link FactoryFinder.find(String,String)}
    */
   public static HttpServer create()
   {
      HttpServer server = (HttpServer)ServiceLoader.loadService(HTTP_SERVER_PROPERTY, null);
      return server;
   }
   
   /** Start an instance of this HTTP server */
   public abstract void start();
   
   /** Create an HTTP context */
   public abstract HttpContext createContext(String string);
   
   /** Publish an JAXWS endpoint to the HTTP server */
   public abstract void publish(HttpContext context, Endpoint endpoint);
   
   /** Destroys an JAXWS endpoint on the HTTP server */
   public abstract void destroy(HttpContext context, Endpoint endpoint);
   
   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public void setProperties(Map<String, Object> map)
   {
      properties = map;
   }
   
   public void setProperty(String key, Object value)
   {
      properties.put(key, value);
   }
   
   public Object getProperty(String key)
   {
      return properties.get(key);
   }

}
