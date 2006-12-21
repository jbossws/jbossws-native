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
package org.jboss.ws.metadata.wsdl;

// $Id$

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;

/**
 * An Interface component describes sequences of messages that a service sends and/or receives. It does this
 * by grouping related messages into operations. An operation is a sequence of input and output messages,
 * and an interface is a set of operations. Thus, an interface defines the design of the application.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class WSDLInterface extends Extendable
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLInterface.class);
   
   private static final long serialVersionUID = 2453454924501233964L;

   // The parent WSDL definitions element.
   private WSDLDefinitions wsdlDefinitions;

   /** The REQUIRED name attribute information item together with the targetNamespace attribute information item
    * of the [parent] definitions element information item forms the QName of the interface.
    */
   private NCName name;
   
   /** Derived QName identifier. 
    */
   private QName qname;

   /** The OPTIONAL extends attribute information item lists the interfaces that this interface derives from.
    */
   private QName[] extendList;

   /** The OPTIONAL styleDefault attribute information item indicates the default style used to construct the
    * {element} properties of {message references} of all operations contained within the [owner] interface
    */
   private String styleDefault;

   /** Zero or more operation element information items */
   private Map operations = new LinkedHashMap();
   /** Zero or more fault element information items */
   private Map faults = new LinkedHashMap();

   /** Construct a WSDL interface for a given WSDL definition */
   public WSDLInterface(WSDLDefinitions wsdlDefinitions)
   {
      log.trace("new WSDLInterface");
      this.wsdlDefinitions = wsdlDefinitions;
   }

   /** Get a WSDLOperation for a given name
    *
    * @param name The name of the operation
    * @return A WSDLOperation or null
    */
   public WSDLInterfaceOperation getWSDLInterfaceOperation(NCName name)
   {
      return (WSDLInterfaceOperation)operations.get(name);
   }

   public WSDLDefinitions getWsdlDefinitions()
   {
      return wsdlDefinitions;
   }

   public NCName getName()
   {
      return name;
   }

   public void setName(NCName name)
   {
      log.trace("setName: " + name);
      this.name = name;
   }

   public QName getQName()
   {
      if (qname == null)
      {
         String tnsURI = wsdlDefinitions.getTargetNamespace();
         qname = new QName(tnsURI, name.toString());
      }
      return qname;
   }

   
   public void setQName(QName qname)
   {
      log.trace("setQName: " + qname);
      this.qname = qname;
   }

   public QName[] getExtendList()
   {
      return extendList;
   }

   public void setExtendList(QName[] extendList)
   {
      this.extendList = extendList;
   }

   public String getStyleDefault()
   {
      return styleDefault;
   }

   public void setStyleDefault(String styleDefault)
   {
      this.styleDefault = styleDefault;
   }

   public WSDLInterfaceOperation[] getOperations()
   {
      WSDLInterfaceOperation[] arr = new WSDLInterfaceOperation[operations.size()];
      new ArrayList(operations.values()).toArray(arr);
      return arr;
   }

   public WSDLInterfaceOperation[] getSortedOperations()
   {
      WSDLInterfaceOperation[] arr = new WSDLInterfaceOperation[operations.size()];
      new ArrayList(operations.values()).toArray(arr);
      Arrays.sort(arr);
      return arr;
   }

   public WSDLInterfaceOperation getOperation(NCName opName)
   {
      WSDLInterfaceOperation operation = (WSDLInterfaceOperation)operations.get(opName);
      return operation;
   }

   public void addOperation(WSDLInterfaceOperation operation)
   {
      operations.put(operation.getName(), operation);
   }

   public WSDLInterfaceFault[] getFaults()
   {
      WSDLInterfaceFault[] arr = new WSDLInterfaceFault[faults.size()];
      new ArrayList(faults.values()).toArray(arr);
      return arr;
   }

   public WSDLInterfaceFault getFault(NCName fName)
   {
      WSDLInterfaceFault fault = (WSDLInterfaceFault)faults.get(fName);
      return fault;
   }

   public void addFault(WSDLInterfaceFault fault)
   {
      faults.put(fault.getName(), fault);
   }

   public boolean containsInterfaceOperation(NCName name)
   {
      return operations.containsKey(name);
   }
}
