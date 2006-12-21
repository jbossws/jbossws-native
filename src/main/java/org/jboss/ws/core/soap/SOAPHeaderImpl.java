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

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.jboss.util.NotImplementedException;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * A representation of the SOAP header element. A SOAP header element consists of XML data that affects the way the
 * application-specific content is processed by the message provider. For example, transaction semantics,
 * authentication information, and so on, can be specified as the content of a SOAPHeader object.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPHeaderImpl extends SOAPElementImpl implements SOAPHeader
{
   public SOAPHeaderImpl(String prefix, String namespace)
   {
      super("Header", prefix, namespace);
   }

   private SOAPHeaderElementImpl convertToHeaderElement(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException("SOAPElement expected");

      SOAPElementImpl element = (SOAPElementImpl)node;

      // convert to SOAPHeaderElement
      element.detachNode();
      return new SOAPHeaderElementImpl(element);
   }

   /*** Add a SOAPHeaderElement as a child of this SOAPHeader instance.
    */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      // Check that we get a SOAPHeaderElement
      if ((child instanceof SOAPHeaderElement) == false)
         child = convertToHeaderElement(child);

      return super.addChildElement(child);
   }

   /** Creates a new SOAPHeaderElement object initialized with the specified name and adds it to this SOAPHeader object.
    */
   public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException
   {
      if (name == null || name.getURI().length() == 0 || name.getPrefix().length() == 0)
         throw new SOAPException("Invalid SOAPHeaderElement name: " + name);

      SOAPHeaderElementImpl headerElement = new SOAPHeaderElementImpl(name);
      addChildElement(headerElement);
      return headerElement;
   }

   public SOAPHeaderElement addHeaderElement(QName qname) throws SOAPException
   {
      return addHeaderElement(new NameImpl(qname));
   }

   /** Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object.
    */
   public Iterator examineAllHeaderElements()
   {
      // make a defensive copy
      ArrayList list = new ArrayList();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPHeaderElement shElement = (SOAPHeaderElement)it.next();
         list.add(shElement);
      }
      return list.iterator();
   }

   /** Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object that have the specified actor.
    */
   public Iterator examineHeaderElements(String actor)
   {
      if (actor == null)
         throw new IllegalArgumentException("Invalid actor: " + actor);

      // make a defensive copy
      ArrayList list = new ArrayList();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPHeaderElement shElement = (SOAPHeaderElement)it.next();
         if (actor.equals(shElement.getActor()))
            list.add(shElement);
      }
      return list.iterator();
   }

   /** Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object that have the specified
    * actor and that have a MustUnderstand attribute whose value is equivalent to true.
    */
   public Iterator examineMustUnderstandHeaderElements(String actor)
   {
      if (actor == null)
         throw new IllegalArgumentException("Invalid actor: " + actor);

      // make a defensive copy
      ArrayList list = new ArrayList();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPHeaderElement shElement = (SOAPHeaderElement)it.next();
         if (actor.equals(shElement.getActor()) && shElement.getMustUnderstand())
            list.add(shElement);
      }
      return list.iterator();
   }

   public Iterator extractAllHeaderElements()
   {
      // make a defensive copy
      ArrayList list = new ArrayList();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPHeaderElement shElement = (SOAPHeaderElement)it.next();
         removeChild(shElement);
         list.add(shElement);
      }
      return list.iterator();
   }

   public Iterator extractHeaderElements(String actor)
   {
      if (actor == null)
         throw new IllegalArgumentException("Invalid actor: " + actor);

      // make a defensive copy
      ArrayList list = new ArrayList();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPHeaderElement shElement = (SOAPHeaderElement)it.next();
         if (actor.equals(shElement.getActor()))
         {
            removeChild(shElement);
            list.add(shElement);
         }
      }
      return list.iterator();
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      if (!(newChild instanceof SOAPHeaderElementImpl || newChild instanceof DocumentFragment))
         newChild = convertToHeaderElement(newChild);

      return super.appendChild(newChild);
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      if (!(newChild instanceof SOAPHeaderElementImpl || newChild instanceof DocumentFragment))
         newChild = convertToHeaderElement(newChild);

      return super.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      if (!(newChild instanceof SOAPHeaderElementImpl || newChild instanceof DocumentFragment))
         newChild = convertToHeaderElement(newChild);

      return super.replaceChild(newChild, oldChild);
   }

   public SOAPHeaderElement addNotUnderstoodHeaderElement(QName qname) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public SOAPHeaderElement addUpgradeHeaderElement(Iterator soapURIs) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public SOAPHeaderElement addUpgradeHeaderElement(String[] soapURIs) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   public SOAPHeaderElement addUpgradeHeaderElement(String soapURI) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }
}