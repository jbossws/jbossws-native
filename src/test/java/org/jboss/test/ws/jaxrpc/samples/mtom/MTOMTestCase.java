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
package org.jboss.test.ws.jaxrpc.samples.mtom;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import javax.activation.DataHandler;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;

/**
 * Test SOAP with XOP through the JAXRPC dynamic proxy layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 * @since 18-Jan-2006
 */
public class MTOMTestCase extends JBossWSTest
{
   private static EmployeeRecords port;

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(MTOMTestCase.class, "jaxrpc-samples-mtom.war, jaxrpc-samples-mtom-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         if (isTargetJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/XOPTestService");
            port = (EmployeeRecords)service.getPort(EmployeeRecords.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/jaxrpc/samples/mtom/WEB-INF/wsdl/EmployeeService.wsdl").toURL();
            URL mappingURL = new File("resources/jaxrpc/samples/mtom/WEB-INF/jaxrpc-mapping.xml").toURL();
            QName qname = new QName("http://org.jboss.ws/samples/mtom", "EmployeeService");
            Service service = factory.createService(wsdlURL, qname, mappingURL);
            port = (EmployeeRecords)service.getPort(EmployeeRecords.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-mtom");
         }

      }
   }

   public void testUpdate() throws Exception
   {
      Employee employee = new Employee();
      employee.setFirstname("Peter");
      employee.setLastname("Pan");
      employee.setLegacyData(getLegacyData());

      Status status = port.updateEmployee(employee);
      assertEquals("OK", status.getCode());
   }

   public void testUpdateMTOMDisabled() throws Exception
   {
      setMTOMEnabled(Boolean.FALSE);
      testUpdate();
   }

   public void testQuery() throws Exception
   {
      Query query = new Query("Peter", "Pan");
      Employee employee = port.queryEmployee(query);
      assertNotNull(employee);
      assertNotNull(employee.getLegacyData());

      StreamSource xmlStream = (StreamSource)employee.getLegacyData().getContent();
      String content = new BufferedReader(new InputStreamReader(xmlStream.getInputStream())).readLine();
      assertEquals("<Payroll><Data/></Payroll>", content);
   }

   private DataHandler getLegacyData()
   {
      return new DataHandler(new StreamSource(new ByteArrayInputStream("<Payroll><Data/></Payroll>".getBytes())), "application/xml");
   }

   private void setMTOMEnabled(Boolean b)
   {
      ((Stub)port)._setProperty(StubExt.PROPERTY_MTOM_ENABLED, b);
   }
}
