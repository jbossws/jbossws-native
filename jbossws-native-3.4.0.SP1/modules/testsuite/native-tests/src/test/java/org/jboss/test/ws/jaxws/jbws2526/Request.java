/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbws2526;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Test request parameter.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Request", namespace = "urn:JBWS-2526-Schema")
@XmlType(name = "", propOrder = { "value1", "value2" })
public class Request
{

   @XmlElement(name = "Value1", required = true, namespace = "urn:JBWS-2526-Schema")
   protected int value1;

   @XmlElement(name = "Value2", required = true, namespace = "urn:JBWS-2526-Schema")
   protected int value2;

   public int getValue1()
   {
      return value1;
   }

   public void setValue1(int value1)
   {
      this.value1 = value1;
   }

   public int getValue2()
   {
      return value2;
   }

   public void setValue2(int value2)
   {
      this.value2 = value2;
   }

}
