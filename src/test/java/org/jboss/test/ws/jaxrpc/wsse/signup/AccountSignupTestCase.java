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
package org.jboss.test.ws.jaxrpc.wsse.signup;

import java.util.Calendar;
import java.util.Date;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

public class AccountSignupTestCase extends JBossWSTest
{

   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(AccountSignupTestCase.class, "jaxrpc-wsse-account-signup.war, jaxrpc-wsse-account-signup-client.jar");
   }

   public void testEndpoint() throws Exception
   {
      if (isTargetJBoss50())
      {
         System.out.println("FIXME: [JBWS-1330] Fix jaxrpc wsse tests for jbossws-5.0");
         return;
      }
      
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/AccountSignupService");
      AccountSignup signup = (AccountSignup)service.getPort(AccountSignup.class);

      AccountInfo account = new AccountInfo();
      account.setFirstName("Jason");
      account.setLastName("Greene");
      Address address = new Address();
      address.setCity("Madison");
      address.setStreet("Some street");
      address.setZip("53717");
      account.setAddress(address);

      CreditCardInfo credit = new CreditCardInfo();
      credit.setCreditCardNumber("1234-1234-1234-1234");
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2005, 11, 1, 0, 0);

      credit.setExpiration(cal.getTime());
      credit.setSecurityCode("123");
      account.setCreditCardInfo(credit);

      int result = signup.signup(account, 0.0f, new Date());
      assertTrue(result == 345);
   }
}