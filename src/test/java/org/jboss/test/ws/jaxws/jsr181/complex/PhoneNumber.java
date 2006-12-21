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
package org.jboss.test.ws.jaxws.jsr181.complex;

/**
 * A typical phone number object. Part of the JSR-181 Complex Test Case.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class PhoneNumber
{
   private String exchange;
   private String areaCode;
   private String line;

   public String getAreaCode()
   {
      return areaCode;
   }

   public void setAreaCode(String areaCode)
   {
      this.areaCode = areaCode;
   }

   public String getExchange()
   {
      return exchange;
   }

   public void setExchange(String exchange)
   {
      this.exchange = exchange;
   }

   public String getLine()
   {
      return line;
   }

   public void setLine(String line)
   {
      this.line = line;
   }
}
