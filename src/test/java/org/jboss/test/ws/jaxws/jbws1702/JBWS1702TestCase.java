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
package org.jboss.test.ws.jaxws.jbws1702;

import junit.framework.Test;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassB;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassC;
import org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperB;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class JBWS1702TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1702TestCase.class, "jaxws-jbws1702.war");
   }

   public void testInheritanceBare() throws Exception
   {
      System.out.println("FIXME: [JBWS-1702]: Type inheritance on Doclit/Bare");
      /*Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/jbws1702/SampleWSWithDocument_Bare?wsdl"),
        new QName("http://jbws1702.jaxws.ws.test.jboss.org/", "SampleWSWithDocument_BareService")
      );

      SampleWSBareSEI port = service.getPort(SampleWSBareSEI.class);
      ResponseWrapperB wrapper = port.getClassCAsClassB();
      ClassB b = wrapper.getData();
      assertTrue("Should be an instance of ClassC, but was " + b, (b instanceof ClassC));
      */
   }

   public void testInheritanceWrapped() throws Exception
   {
      Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/jbws1702/SampleWSWithDocument_Wrapped?wsdl"),
        new QName("http://jbws1702.jaxws.ws.test.jboss.org/", "SampleWSWithDocument_WrappedService")
      );

      SampleWSWrappedSEI port = service.getPort(SampleWSWrappedSEI.class);
      ClassB b = port.getClassCAsClassB();      
      assertTrue("Should be an instance of ClassC, but was " + b, (b instanceof ClassC));
   }


}
