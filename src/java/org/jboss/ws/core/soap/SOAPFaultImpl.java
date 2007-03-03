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

import java.io.Writer;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.xb.QNameBuilder;
import org.w3c.dom.Element;

/**
 * An element in the SOAPBody object that contains error and/or status information.
 * This information may relate to errors in the SOAPMessage object or to problems
 * that are not related to the content in the message itself. Problems not related
 * to the message itself are generally errors in processing, such as the inability
 * to communicate with an upstream server.
 *
 * The SOAPFault interface provides methods for retrieving the information contained
 * in a SOAPFault object and for setting the fault code, the fault actor, and a string
 * describing the fault. A fault code is one of the codes defined in the SOAP 1.1 specification
 * that describe the fault. An actor is an intermediate recipient to whom a message was routed.
 * The message path may include one or more actors, or, if no actors are specified, the message
 * goes only to the default actor, which is the final intended recipient.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPFaultImpl extends SOAPBodyElementDoc implements SOAPFault
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPFaultImpl.class);

   private SOAPElement faultcode;
   private SOAPElement faultstring;
   private SOAPElement faultactor;
   private Locale faultStringLocale;

   public SOAPFaultImpl() throws SOAPException
   {
      this(SOAPConstants.SOAP_ENV_PREFIX, SOAPConstants.URI_NS_SOAP_ENVELOPE);
   }

   public SOAPFaultImpl(String prefix, String namespace) throws SOAPException
   {
      super(new NameImpl("Fault", prefix, namespace));
      SOAPFactoryImpl factory = new SOAPFactoryImpl();
      addChildElement(faultcode = factory.createElement("faultcode"));
      addChildElement(faultstring = factory.createElement("faultstring"));
   }

   /** Creates an optional Detail object and sets it as the Detail object for this SOAPFault  object.
    */
   public Detail addDetail() throws SOAPException
   {
      Detail detail = getDetail();
      if (detail != null)
         throw new SOAPException("SOAPFault already contains a detail node");

      SOAPFactoryImpl factory = new SOAPFactoryImpl();
      detail = (Detail)addChildElement(factory.createDetail());
      return detail;
   }

   /** Returns the optional detail element for this SOAPFault  object.
    */
   public Detail getDetail()
   {
      Detail detail = null;
      Iterator it = getChildElements(new NameImpl("detail"));
      while (it.hasNext())
      {
         Object obj = it.next();
         if (obj instanceof Detail)
         {
            detail = (Detail)obj;
         }
         else if (obj instanceof SOAPElementImpl)
         {
            try
            {
               SOAPElementImpl soapEl = (SOAPElementImpl)obj;
               SOAPFactoryImpl factory = new SOAPFactoryImpl();
               detail = (Detail)addChildElement(factory.createDetail());

               Iterator childIt = DOMUtils.getChildElements(soapEl);
               while (childIt.hasNext())
               {
                  Element domElement = (Element)childIt.next();
                  SOAPElement detailEntry = new DetailEntryImpl(factory.createElement(domElement));
                  detail.addChildElement(detailEntry);
               }
            }
            catch (SOAPException e)
            {
               throw new WSException("Unable to create fault detail: " + e.getMessage());
            }

         }
      }

      return detail;
   }

   /** Gets the fault actor for this SOAPFault object.
    */
   public String getFaultActor()
   {
      return (faultactor != null ? faultactor.getValue() : null);
   }

   /** Gets the fault code for this SOAPFault object.
    */
   public String getFaultCode()
   {
      String value = faultcode.getValue();
      return value;
   }

   /**
    * Gets the mandatory SOAP 1.1 fault code for this SOAPFault object as a SAAJ Name object.
    */
   public Name getFaultCodeAsName()
   {
      QName qname = QNameBuilder.buildQName(this, faultcode.getValue());
      return new NameImpl(qname);
   }

   /** Gets the fault string for this SOAPFault object.
    */
   public String getFaultString()
   {
      String value = faultstring.getValue();
      return value;
   }

   /** Gets the locale of the fault string for this SOAPFault object.
    */
   public Locale getFaultStringLocale()
   {
      return faultStringLocale;
   }

   /** Sets this SOAPFault object with the given fault actor.
    */
   public void setFaultActor(String faultActor) throws SOAPException
   {
      if (faultactor == null)
      {
         SOAPFactoryImpl factory = new SOAPFactoryImpl();
         addChildElement(faultactor = factory.createElement("faultactor"));
      }

      faultactor.setValue(faultActor);
   }

   /** Sets this SOAPFault object with the give fault code.
    */
   public void setFaultCode(String faultCode) throws SOAPException
   {
      // Must be of the form "prefix:localName" where the prefix has been defined in a namespace declaration.
      if (faultCode == null || faultCode.indexOf(":") < 1)
         throw new IllegalArgumentException("Invalid faultCode: " + faultCode);

      QName qname = QNameBuilder.buildQName(this, faultCode);
      setFaultCode(new NameImpl(qname));
   }

   /** Sets this SOAPFault object with the given fault code.
    */
   public void setFaultCode(Name faultName) throws SOAPException
   {
      String nsURI = faultName.getURI();
      String prefix = faultName.getPrefix();
      String localName = faultName.getLocalName();

      // For lazy folkes like the CTS that don't bother to give 
      // a namesapce URI, assume they use a standard code
      if ("".equals(nsURI))
      {
         log.warn("Empty namespace URI with fault code '" + faultName + "', assuming: " + Constants.NS_SOAP11_ENV);
         nsURI = Constants.NS_SOAP11_ENV;
         prefix = getPrefix();
      }

      if (nsURI.length() > 0 && prefix.length() == 0)
         prefix = "codeNS";
      
      String prevNS = getNamespaceURI(prefix);
      if (prevNS == null)
         addNamespaceDeclaration(prefix, nsURI);

      String faultCode = prefix + ":" + localName;
      faultcode.setValue(faultCode);
   }

   public void setFaultCode(QName faultCode) throws SOAPException
   {
      setFaultCode(new NameImpl(faultCode));
   }
   
   /** Sets the fault string for this SOAPFault object to the given string.
    */
   public void setFaultString(String faultString) throws SOAPException
   {
      faultstring.setValue(faultString);
   }

   /** Sets the fault string for this SOAPFault object to the given string and localized to the given locale.
    */
   public void setFaultString(String faultString, Locale locale) throws SOAPException
   {
      setFaultString(faultString);
      this.faultStringLocale = locale;
   }

   public void writeElement(Writer writer)
   {
      new DOMWriter(writer).print(this);
   }

   public void addFaultReasonText(String text, Locale locale) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public void appendFaultSubcode(QName subcode) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public QName getFaultCodeAsQName()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public String getFaultNode()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public Iterator getFaultReasonLocales() throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public String getFaultReasonText(Locale locale) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public Iterator getFaultReasonTexts() throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public String getFaultRole()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public Iterator getFaultSubcodes()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public boolean hasDetail()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public void removeAllFaultSubcodes()
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public void setFaultNode(String uri) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public void setFaultRole(String uri) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }
}