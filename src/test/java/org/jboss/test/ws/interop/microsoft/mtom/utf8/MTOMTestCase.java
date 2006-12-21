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
package org.jboss.test.ws.interop.microsoft.mtom.utf8;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.ClientScenario;
import org.jboss.test.ws.interop.microsoft.InteropConfigFactory;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

/**
 *
 * MTOM test scenarios cover essential combinations of MTOM encoding applied to
 * different data structures, character encodings and WS-Security.
 * Scenarios 3.1 – 3.5 cover optimizing binary data in various parts of a message.
 * Scenario 3.6 exercises UTF-16 encoding together with MTOM.
 * Scenario 3.7 and 3.8 exercise composition of MTOM with Security.
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since Aug 24, 2006
 */
public class MTOMTestCase extends JBossWSTest {

   IMtomTest port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(MTOMTestCase.class, "jbossws-interop-mtomUTF8-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null )
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/interop/MTOMUTF8Service");
         port = (IMtomTest)service.getPort(IMtomTest.class);
         configureClient();
      }
   }

   private void configureClient() {

      InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         log.info("Using scenario: " + scenario);
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
   }

   /**
    * Scenario #3.1. Echo Binary As String.
    *
    * Request has a XOPed binary that contains utf-8 encoded text,
    * response contains the passed string.
    */
   public void testScenario_3_1() throws Exception
   {
      String s = "testScenario_3_1";
      EchoBinaryAsStringResponse response = port.echoBinaryAsString( new EchoBinaryAsString(s.getBytes()));
      assertNotNull(response);
      assertEquals(response.getEchoBinaryAsStringResult(), s);
   }

   /**
    * Scenario #3.2. Echo String As Binary.
    *
    * Request has a string that returned back as XOPed binary blob.
    */
   public void testScenario_3_2() throws Exception
   {
      String s = "testScenario_3_2";
      EchoStringAsBinaryResponse response = port.echoStringAsBinary( new EchoStringAsBinary(s));
      assertNotNull(response);
      assertEquals( new String(response.getEchoStringAsBinaryResult(), "UTF-8"), s);
   }

   /**
    * Scenario #3.3. Echo Array of Binaries As Array Of Strings
    *
    * Request contains array, each array element has type base64Binary, XOPed,
    * contains unique utf-8 encoded strings.
    * Response has array of strings matching those in the request
    */
   public void testScenario_3_3() throws Exception
   {
      System.out.println("FIXME: testScenario_3_3");
   }

   /**
    * Scenario #3.4. Echo Binary Field As String
    *
    * Echo complex type with a binary field Request contains a structure,
    * one of the fields is binary, contains a string UTF-8 encoded.
    * Response contains a string from the binary field from the request.
    */
   public void testScenario_3_4() throws Exception
   {
      String s = "testScenario_3_4";
      EchoBinaryFieldAsStringResponse response = port.echoBinaryFieldAsString(
          new EchoBinaryFieldAsString(
              new MtomTestStruct(s.getBytes(), "Hello World")
          )
      );
      assertNotNull(response);
      assertEquals( response.getEchoBinaryFieldAsStringResult(), s);
   }

   /**
    * Scenario #3.5. Echo Binary Passed In a Header As String
    *
    * Request message contains a Header element of the type base64Binary,
    * content is XOPed. Binary contains utf-8 encoded text.
    * Response message contains the passed string in the body
    */
   public void testScenario_3_5() throws Exception
   {
      System.out.println("FIXME: testScenario_3_5");
   }

   /**
    * Scenario #3.6. Composition with UTF-16 encoding
    *
    */
   public void testScenario_3_6() throws Exception
   {
      System.out.println("FIXME: testScenario_3_6");
   }
}
