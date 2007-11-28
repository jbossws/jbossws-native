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
package org.jboss.ws.extensions.wsrm.config;

/**
 * Message store metadata
 * 
 * @author richard.opalka@jboss.com
 */
public final class RMMessageStoreConfig
{
   
   private String id;
   private String className;
   private String configFile;
   
   public final void setId(String id)
   {
      if (id == null)
         throw new IllegalArgumentException();
      
      this.id = id;
   }
   
   public final String getId()
   {
      return this.id;
   }
   
   public final void setClassName(String className)
   {
      if (className == null)
         throw new IllegalArgumentException();
      
      this.className = className;
   }
   
   public final String getClassName()
   {
      return this.className;
   }
   
   public final void setConfigFile(String configFile)
   {
      if (configFile == null)
         throw new IllegalArgumentException();
      
      this.configFile = configFile;
   }
   
   public final String getConfigFile()
   {
      return this.configFile;
   }

}
