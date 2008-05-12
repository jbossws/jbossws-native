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
package org.jboss.test.ws.tools.scripts;

import java.io.File;
import java.io.IOException;

import org.jboss.wsf.test.JBossWSTest;

/**
 * JBWS-1793: Provide a test case for the tools scripts that reside under JBOSS_HOME/bin
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ScriptTestCase extends JBossWSTest
{
   private String TOOLS_CONFIG = getResourceFile("tools/scripts/wstools-config.xml").getAbsolutePath();

   private String JBOSS_HOME;
   private String JDK_HOME;
   private String TEST_EXEC_DIR;
   private String OS;

   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      TEST_EXEC_DIR = createResourceFile(".").getPath();
      JDK_HOME = System.getProperty("java.home");
      OS = System.getProperty("os.name").toLowerCase();
   }

   public void testWSToolsFromCommandLine() throws Exception
   {
      if (isWindowsOS())
      {
         fail("This test has not been verified on windows");
      }

      // use absolute path for the output to be re-usable      
      File dest = createResourceFile("wstools/java");
      dest.mkdirs();

      String command = JBOSS_HOME + "/bin/wstools.sh -config " + TOOLS_CONFIG + " -dest "+ dest.getAbsolutePath();
      Process p = executeCommand(command);

      // check status code
      assertStatusCode(p, "wstools");

      File javaSource = getResourceFile("wstools/java/org/jboss/test/ws/jbws810/PhoneBookService.java");

      assertTrue("Service endpoint interface not generated", javaSource.exists());
   }

   private Process executeCommand(String command) throws IOException
   {
      // be verbose
      System.out.println("cmd: " + command);
      System.out.println("test execution dir: " + TEST_EXEC_DIR);

      Process p = Runtime.getRuntime().exec(command, new String[] { "JBOSS_HOME=" + JBOSS_HOME, "JAVA_HOME=" + JDK_HOME });
      return p;
   }

   private void assertStatusCode(Process p, String s) throws InterruptedException
   {
      // check status code
      int status = p.waitFor();
      assertTrue(s + " did exit with status " + status, status == 0);
   }

   private boolean isWindowsOS()
   {
      return ((OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1));
   }
}
