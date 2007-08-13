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
package org.jboss.ws.core;

// $Id$

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.Constants;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPFaultImpl;
import org.jboss.ws.metadata.umdm.OperationMetaData;

/**
 * The SOAP11Binding  
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 04-Jul-2006
 */
public abstract class CommonSOAP12Binding extends CommonSOAPBinding
{
   
   public CommonSOAP12Binding()
   {
   }

   /** Create the SOAP-1.1 message */
   protected MessageAbstraction createMessage(OperationMetaData opMetaData) throws SOAPException
   {
      MessageFactoryImpl factory = new MessageFactoryImpl();
      factory.setEnvNamespace(Constants.NS_SOAP12_ENV);
      return (MessageAbstraction)factory.createMessage();
   }

   protected abstract Set<String> getRoles();
   
   @Override
   protected void verifyUnderstoodHeader(SOAPHeaderElement element) throws Exception
   {
      QName name = new QName(element.getNamespaceURI(), element.getLocalName());
      String actor = element.getActor();
      Set<String> roles = getRoles();
      
      boolean isActor = actor == null || actor.length() == 0 || Constants.URI_SOAP11_NEXT_ACTOR.equals(actor) || roles.contains(actor);
      if (isActor && !headerSource.getHeaders().contains(name))
      {
         // How do we pass NotUnderstood blocks? They are not in the fault element
         QName faultCode = SOAPConstants.SOAP_MUSTUNDERSTAND_FAULT;
         SOAPFaultImpl fault = new SOAPFaultImpl();
         fault.setFaultCode(faultCode);
         fault.setFaultString("SOAP header blocks not understood");
         throwFaultException(fault);
      }
   }
}
