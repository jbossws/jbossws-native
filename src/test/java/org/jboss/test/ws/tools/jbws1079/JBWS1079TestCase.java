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
package org.jboss.test.ws.tools.jbws1079;

import java.io.File;
import java.io.FileInputStream;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.tools.fixture.JBossSourceComparator;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.tools.WSTools;
import org.w3c.dom.Element;

/**
 * 
 * @author darran.lofthouse@jboss.com
 * @since Nov 5, 2006
 */
public class JBWS1079TestCase extends JBossWSTest
{

   public void testAnonymousType() throws Exception
   {
      String resourceDir = "resources/tools/jbws1079";
      String toolsDir = "tools/jbws1079";
      String[] args = new String[] { "-dest", toolsDir, "-config", resourceDir + "/wstools-config.xml" };
      new WSTools().generate(args);

      Element exp = DOMUtils.parse(new FileInputStream(resourceDir + "/anonymous-mapping.xml"));
      Element was = DOMUtils.parse(new FileInputStream(toolsDir + "/anonymous-mapping.xml"));
      assertEquals(exp, was);

      compareSource(resourceDir + "/TelephoneNumber.java", toolsDir + "/org/jboss/test/ws/jbws1079/TelephoneNumber.java");
      compareSource(resourceDir + "/TelephoneNumberNumber.java", toolsDir + "/org/jboss/test/ws/jbws1079/TelephoneNumberNumber.java");
   }

   private static void compareSource(final String expectedName, final String generatedName) throws Exception
   {
      File expected = new File(expectedName);
      File generated = new File(generatedName);

      JBossSourceComparator sc = new JBossSourceComparator(expected, generated);
      sc.validate();
      sc.validateImports();
   }
}
