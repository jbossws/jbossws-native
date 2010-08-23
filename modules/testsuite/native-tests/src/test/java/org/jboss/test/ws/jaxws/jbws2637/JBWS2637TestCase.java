/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2637;

import java.io.File;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2637] Incorrect namespace for fault 
 * messages.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 23rd August 2010
 * @see https://jira.jboss.org/jira/browse/JBWS-2637
 */
public class JBWS2637TestCase extends JBossWSTest
{
   
   public final String TARGET_WSDL_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2637?wsdl";

   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final String EXT = ":".equals(PS) ? ".sh" : ".bat";

   private String JBOSS_HOME = System.getProperty("jboss.home");
   
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2637TestCase.class, "jaxws-jbws2637.war");
   }

   public void testWSConsume() throws Exception
   {
      // use absolute path for the output to be re-usable
      String absOutput = "target/wsconsume/jbws2637";

      String command = JBOSS_HOME + FS + "bin" + FS + "wsconsume" + EXT + " -k -o " + absOutput + " " + TARGET_WSDL_ADDRESS;
      executeCommand(command);

      String packageDir = new File(absOutput + "/org/jboss/ws/jbws2637").getAbsolutePath();

      checkFileExists(packageDir, "Echo.java");
      checkFileExists(packageDir, "EchoResponse.java");
      checkFileExists(packageDir, "Endpoint.java");
      checkFileExists(packageDir, "EndpointFault.java");
      checkFileExists(packageDir, "EndpointFault_Exception.java");
      checkFileExists(packageDir, "EndpointService.java");
   }

   private static void checkFileExists(String packageDir, String filename)
   {
      File expectedFile = new File(packageDir + FS + filename);
      assertTrue("File '" + filename + "' missing from folder '" + packageDir + "'", expectedFile.exists());
   }
   
}
