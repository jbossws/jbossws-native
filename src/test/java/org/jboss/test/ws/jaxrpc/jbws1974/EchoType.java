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
package org.jboss.test.ws.jaxrpc.jbws1974;

/**
 * @author darran.lofthouse@jboss.com
 * @since June 20, 2008
 */
public class EchoType
{

   private String message_1;

   private String message_2;

   private String message_3;

   public EchoType(final String message_1, final String message_2, final String message_3)
   {
      super();
      this.message_1 = message_1;
      this.message_2 = message_2;
      this.message_3 = message_3;
   }

   public String getMessage_1()
   {
      return message_1;
   }

   public void setMessage_1(String message_1)
   {
      this.message_1 = message_1;
   }

   public String getMessage_2()
   {
      return message_2;
   }

   public void setMessage_2(String message_2)
   {
      this.message_2 = message_2;
   }

   public String getMessage_3()
   {
      return message_3;
   }

   public void setMessage_3(String message_3)
   {
      this.message_3 = message_3;
   }

}
