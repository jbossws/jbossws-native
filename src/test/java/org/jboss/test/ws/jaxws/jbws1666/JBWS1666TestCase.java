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
package org.jboss.test.ws.jaxws.jbws1666;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.wsf.common.IOUtils;

/**
 * [JBWS-1666] Simplify jbosws jar dependencies
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1666
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Jun-2007
 */
public class JBWS1666TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1666TestCase.class, "jaxws-jbws1666.war");
   }

   public void testPortAccess() throws Exception
   {
      String resStr = TestClient.testPortAccess(getServerHost());
      assertEquals(TestClient.REQ_STR, resStr);
   }

   public void testClientAccess() throws Exception
   {
      File javaFile = new File (System.getProperty("java.home") + "/bin/java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      
      String jbh = System.getProperty("jboss.home");
      String jbc = jbh + "/client";
      String jbl = jbh + "/lib";
      
      // Setup the classpath - do not modify this lightheartedly. 
      // Maybe you should extend the Class-Path in the MANIFEST instead.
      StringBuffer cp = new StringBuffer("./classes");
      cp.append(":" + jbc + "/jbossws-client.jar");
      if (isTargetJBoss50())
      {
         cp.append(":" + jbc + "/jboss-common-core.jar");
         cp.append(":" + jbc + "/jboss-logging-spi.jar");
      }
      else
      {
         cp.append(":" + jbc + "/jboss-common-client.jar");
      }

      Runtime rt = Runtime.getRuntime();

      String command = javaCmd + " -Djava.endorsed.dirs=" + jbl + "/endorsed -cp " + cp + " " + TestClient.class.getName() + " " + getServerHost();
      Process proc = rt.exec(command);
      int status = proc.waitFor();
      if (status == 0)
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
         String resStr = br.readLine();
         assertEquals(TestClient.REQ_STR, resStr);
      }
      else
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         IOUtils.copyStream(baos, proc.getErrorStream());
         String errStr = new String(baos.toByteArray());
         fail(errStr);
      }
   }
}
