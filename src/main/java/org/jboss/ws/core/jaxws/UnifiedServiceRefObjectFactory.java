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
package org.jboss.ws.core.jaxws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxws.client.NameValuePair;
import org.jboss.ws.core.jaxws.client.PortInfo;
import org.jboss.ws.core.jaxws.client.UnifiedServiceRef;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * An ObjectModelFactory for UnifiedServiceRef
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2007
 */
public class UnifiedServiceRefObjectFactory implements ObjectModelFactory
{
   // Hide constructor
   private UnifiedServiceRefObjectFactory()
   {
   }

   public static UnifiedServiceRefObjectFactory newInstance()
   {
      return new UnifiedServiceRefObjectFactory();
   }

   public UnifiedServiceRef parse(Source source) 
   {
      // setup the XML binding Unmarshaller
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         StreamResult result = new StreamResult(baos);
         TransformerFactory tf = TransformerFactory.newInstance();
         tf.newTransformer().transform(source, result);
         
         InputStream is = new ByteArrayInputStream(baos.toByteArray());
         
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         return (UnifiedServiceRef)unmarshaller.unmarshal(is, this, null);
      }
      catch (Exception ex)
      {
         WSException.rethrow(ex);
         return null;
      }
   }
   
   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    */
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      return new UnifiedServiceRef();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   public void setValue(UnifiedServiceRef ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-ref-name"))
      {
         ref.setServiceRefName(value);
      }
      else if (localName.equals("service-class-name"))
      {
         ref.setServiceClassName(value);
      }
      else if (localName.equals("service-qname"))
      {
         ref.setServiceQName(QName.valueOf(value));
      }
      else if (localName.equals("config-name"))
      {
         ref.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         ref.setConfigFile(value);
      }
      else if (localName.equals("handler-chain"))
      {
         ref.setHandlerChain(value);
      }
      else if (localName.equals("wsdl-override"))
      {
         ref.setWsdlLocation(value);
      }
   }

   public Object newChild(UnifiedServiceRef parent, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;

      if (localName.equals("port-info"))
         child = new PortInfo(parent);

      return child;
   }

   public void addChild(UnifiedServiceRef parent, PortInfo portInfo, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.getPortInfos().add(portInfo);
   }

   public void setValue(PortInfo portInfo, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-endpoint-interface"))
      {
         portInfo.setServiceEndpointInterface(value);
      }
      else if (localName.equals("port-qname"))
      {
         portInfo.setPortQName(QName.valueOf(value));
      }
      else if (localName.equals("config-name"))
      {
         portInfo.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         portInfo.setConfigFile(value);
      }
   }

   public Object newChild(PortInfo portInfo, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("stub-property"))
         child = new NameValuePair();
      return child;
   }

   public void addChild(PortInfo parent, NameValuePair stubProp, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.getStubProperties().add(stubProp);
   }

   public void setValue(NameValuePair nvPair, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("name"))
      {
         nvPair.setName(value);
      }
      else if (localName.equals("value"))
      {
         nvPair.setValue(value);
      }
   }
}
