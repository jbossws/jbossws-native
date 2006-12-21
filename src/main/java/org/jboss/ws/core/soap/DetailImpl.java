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
package org.jboss.ws.core.soap;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;

/**
 * A container for DetailEntry objects. DetailEntry objects give detailed error information that is application-specific
 * and related to the SOAPBody object that contains it.
 *
 * A Detail object, which is part of a SOAPFault object, can be retrieved using the method SOAPFault.getDetail.
 *
 * The Detail interface provides two methods. One creates a new DetailEntry object and also automatically adds
 * it to the Detail object. The second method gets a list of the DetailEntry objects contained in a Detail object.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class DetailImpl extends SOAPFaultElementImpl implements Detail
{
   public DetailImpl()
   {
      super("detail");
   }

   public DetailEntry addDetailEntry(Name name) throws SOAPException
   {
      DetailEntryImpl detailEntry = new DetailEntryImpl(name);
      addChildElement(detailEntry);
      return detailEntry;
   }

   public DetailEntry addDetailEntry(QName qname) throws SOAPException
   {
      DetailEntryImpl detailEntry = new DetailEntryImpl(qname);
      addChildElement(detailEntry);
      return detailEntry;
   }

   public Iterator getDetailEntries()
   {
      return getChildElements();
   }
}
