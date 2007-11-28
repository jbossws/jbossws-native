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
package org.jboss.test.ws.jaxws.wsrm.oneway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;

import org.jboss.ws.extensions.wsrm.api.RMAddressingType;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Addressable version of one way WS-RM message exchange
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 28, 2007
 */
public final class RMAddressableOneWayTestCase extends RMAbstractOneWayTest
{

   private static final Properties props = new Properties();
   private final boolean emulatorOn = Boolean.parseBoolean((String)props.get("emulator"));
   private final String serviceURL = "http://" + getServerHost() + ":" + props.getProperty("port") + props.getProperty("path");

   static
   {
      // load test properties
      File propertiesFile = new File("resources/jaxws/wsrm/properties/RMAddressableOneWayTestCase.properties");
      try 
      {
         props.load(new FileInputStream(propertiesFile));
      }
      catch (IOException ignore)
      {
         ignore.printStackTrace();
      }
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(RMAddressableOneWayTestCase.class, props.getProperty("archives"));
   }

   public final RMAddressingType getAddressingType()
   {
      return RMAddressingType.ADDRESSABLE;
   }

   public final boolean isEmulatorOn()
   {
      return this.emulatorOn;
   }
   
   public final String getServiceURL()
   {
      return this.serviceURL;
   }
   
}
