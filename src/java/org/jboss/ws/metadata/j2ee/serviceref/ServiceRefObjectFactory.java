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
package org.jboss.ws.metadata.j2ee.serviceref;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.jboss.ws.WSException;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * An ObjectModelFactory for ServiceRefMetaData
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2007
 */
public class ServiceRefObjectFactory implements ObjectModelFactory
{
   // Hide constructor
   private ServiceRefObjectFactory()
   {
   }

   public static ServiceRefObjectFactory newInstance()
   {
      return new ServiceRefObjectFactory();
   }

   public UnifiedServiceRefMetaData parse(String xmlFragment) 
   {
      try
      {
         InputStream is = new ByteArrayInputStream(xmlFragment.getBytes());
         
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         return (UnifiedServiceRefMetaData)unmarshaller.unmarshal(is, this, null);
      }
      catch (Exception ex)
      {
         WSException.rethrow(ex);
         return null;
      }
   }
   
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      return new UnifiedServiceRefMetaData();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   // ******************************************************** 
   // START ServiceRefMetaData 

   public void setValue(UnifiedServiceRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      /* Standard properties */
      if (localName.equals("service-ref-name"))
      {
         ref.setServiceRefName(value);
      }
      else if (localName.equals("service-interface"))
      {
         ref.setServiceInterface(value);
      }
      else if (localName.equals("service-ref-type"))
      {
         ref.setServiceRefType(value);
      }
      else if (localName.equals("wsdl-file"))
      {
         ref.setWsdlFile(value);
      }
      else if (localName.equals("jaxrpc-mapping-file"))
      {
         ref.setMappingFile(value);
      }
      else if (localName.equals("service-qname"))
      {
         ref.setServiceQName(QName.valueOf(value));
      }

      /* JBoss properties */
      else if (localName.equals("service-impl-class"))
      {
         ref.setServiceImplClass(value);
      }
      else if (localName.equals("config-name"))
      {
         ref.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         ref.setConfigFile(value);
      }
      else if (localName.equals("wsdl-override"))
      {
         ref.setWsdlOverride(value);
      }
      else if (localName.equals("handler-chain"))
      {
         ref.setHandlerChain(value);
      }
   }

   public Object newChild(UnifiedServiceRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("port-component-ref"))
         child = new UnifiedPortComponentRefMetaData(ref);
      else if (localName.equals("handler"))
         child = new UnifiedHandlerMetaData();
      else if (localName.equals("handler-chains"))
         child = new UnifiedHandlerChainsMetaData();
      
      else if (localName.equals("call-property"))
         child = new UnifiedCallPropertyMetaData();

      return child;
   }

   public Object newChild(UnifiedHandlerChainsMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("handler-chain"))
         child = new UnifiedHandlerChainMetaData();

      return child;
   }

   public void addChild(UnifiedServiceRefMetaData parent, UnifiedPortComponentRefMetaData pcRef, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.addPortComponentRef(pcRef);
   }

   public void addChild(UnifiedServiceRefMetaData parent, UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.addHandler(handler);
   }

   public void addChild(UnifiedServiceRefMetaData parent, UnifiedHandlerChainsMetaData handlerChains, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.setHandlerChains(handlerChains);
   }

   public void addChild(UnifiedServiceRefMetaData parent, UnifiedCallPropertyMetaData callProp, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.addCallProperty(callProp);
   }

   public void addChild(UnifiedHandlerChainsMetaData parent, UnifiedHandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.addHandlerChain(handlerChain);
   }

   public void setValue(UnifiedPortComponentRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-endpoint-interface"))
      {
         ref.setServiceEndpointInterface(value);
      }
      else if (localName.equals("enable-mtom"))
      {
         ref.setEnableMTOM(Boolean.valueOf(value));
      }
      else if (localName.equals("port-component-link"))
      {
         ref.setPortComponentLink(value);
      }
      else if (localName.equals("port-qname"))
      {
         ref.setPortQName(QName.valueOf(value));
      }
      else if (localName.equals("config-name"))
      {
         ref.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         ref.setConfigFile(value);
      }
   }

   public Object newChild(UnifiedPortComponentRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("call-property"))
         child = new UnifiedCallPropertyMetaData();
      if (localName.equals("stub-property"))
         child = new UnifiedStubPropertyMetaData();
      return child;
   }

   public void setValue(UnifiedHandlerChainMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-name-pattern"))
      {
         ref.setServiceNamePattern(QName.valueOf(value));
      }
      else if (localName.equals("port-name-pattern"))
      {
         ref.setPortNamePattern(QName.valueOf(value));
      }
      else if (localName.equals("protocol-binding"))
      {
         ref.setProtocolBindings(value);
      }
   }

   public void setValue(UnifiedHandlerMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("handler-name"))
      {
         ref.setHandlerName(value);
      }
      else if (localName.equals("handler-class"))
      {
         ref.setHandlerClass(value);
      }
      else if (localName.equals("soap-header"))
      {
         ref.addSoapHeader(QName.valueOf(value));
      }
      else if (localName.equals("soap-role"))
      {
         ref.addSoapRole(value);
      }
      else if (localName.equals("port-name"))
      {
         ref.addPortName(value);
      }
   }

   public Object newChild(UnifiedHandlerMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("init-param"))
         child = new UnifiedInitParamMetaData();

      return child;
   }

   public void addChild(UnifiedHandlerMetaData parent, UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      parent.addInitParam(param);
   }

   public void setValue(UnifiedInitParamMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("param-name"))
      {
         ref.setParamName(value);
      }
      else if (localName.equals("param-value"))
      {
         ref.setParamValue(value);
      }
   }

   public void setValue(UnifiedCallPropertyMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("prop-name"))
      {
         ref.setPropName(value);
      }
      else if (localName.equals("prop-value"))
      {
         ref.setPropValue(value);
      }
   }

   public void setValue(UnifiedStubPropertyMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("prop-name"))
      {
         ref.setPropName(value);
      }
      else if (localName.equals("prop-value"))
      {
         ref.setPropValue(value);
      }
   }
   
   public void addChild(UnifiedPortComponentRefMetaData ref, UnifiedCallPropertyMetaData callProp, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      ref.addCallProperty(callProp);
   }

   public void addChild(UnifiedPortComponentRefMetaData ref, UnifiedStubPropertyMetaData stubProp, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      ref.addStubProperty(stubProp);
   }

   // END ServiceRefMetaData 
   // ******************************************************** 
}
