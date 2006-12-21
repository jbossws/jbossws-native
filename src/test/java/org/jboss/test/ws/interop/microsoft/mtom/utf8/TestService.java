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

import java.rmi.RemoteException;
import java.io.UnsupportedEncodingException;

/**
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
public class TestService implements IMtomTest{
   public EchoBinaryAsStringResponse echoBinaryAsString(EchoBinaryAsString parameters) throws RemoteException {
      try
      {
         return new EchoBinaryAsStringResponse(new String(parameters.getArray(), "UTF-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RemoteException(e.getMessage());
      }
   }

   public EchoStringAsBinaryResponse echoStringAsBinary(EchoStringAsBinary parameters) throws RemoteException {
      return new EchoStringAsBinaryResponse(parameters.getS().getBytes());
   }

   public EchoBinaryArrayAsStringArrayResponse echoBinaryArrayAsStringArray(EchoBinaryArrayAsStringArray parameters)
       throws RemoteException {
      throw new RemoteException("Not implemented");
   }

   public EchoBinaryFieldAsStringResponse echoBinaryFieldAsString(EchoBinaryFieldAsString parameters) throws RemoteException {
      return new EchoBinaryFieldAsStringResponse(
          new String(parameters.getS().getArray())
      );
   }

   public OutputMessageContract echoBinaryHeaderAsString(InputMessageContract parameters) throws RemoteException {
      throw new RemoteException("Not implemented");
   }
}
