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
package org.jboss.test.ws.tools.jbws_206.tests.UserExceptionInheritance;

import org.jboss.test.ws.tools.jbws_206.JBWS206Test;

/**
 *  JBWS-206: WSDL 1.1 to Java Comprehensive Test Collection
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Sep 26, 2005
 */
public class UserExceptionInheritanceWSDL2JavaTestCase extends JBWS206Test
{  
   public String getBase() 
   {
      return "UserExceptionInheritance";
   } 
   
   public String getFixMe()
   {
      return null;
   }
   
   public String getSEIName()
   {
      return "UserExceptionSEI";
   }
   
   public String getServiceName()
   {
      return "UserExceptionService";
   } 
   
   public  void checkGeneratedUserTypes() throws Exception
   {  
      checkUserType( "CustomException.java");
      checkUserType( "InheritedException.java"); 
   } 
}
