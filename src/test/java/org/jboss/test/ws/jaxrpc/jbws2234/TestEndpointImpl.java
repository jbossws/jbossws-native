/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.ws.jaxrpc.jbws2234;

/**
 * @author darran.lofthouse@jboss.com
 * @since June 21, 2008
 */
public class TestEndpointImpl implements TestEndpoint
{

   public static final String TEST_EXCEPTION = "TestException";

   public static final String RUNTIME_EXCEPTION = "RuntimeException";

   public String echo(String message) throws TestException
   {
      if (TEST_EXCEPTION.equals(message))
      {
         throw new TestException();
      }
      else if (RUNTIME_EXCEPTION.equals(message))
      {
         throw new RuntimeException("Simulated failure");
      }
      return message;
   }

}
