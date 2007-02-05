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

// $Id$

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.TypeMappingImpl;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.jaxrpc.binding.DeserializerFactoryBase;
import org.jboss.ws.core.jaxrpc.binding.DeserializerSupport;
import org.jboss.ws.core.jaxrpc.binding.NullValueSerializer;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.jaxrpc.binding.SerializerFactoryBase;
import org.jboss.ws.core.jaxrpc.binding.SerializerSupport;
import org.jboss.ws.core.soap.attachment.SwapableMemoryDataSource;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.core.utils.MimeUtils;
import org.jboss.ws.extensions.xop.XOPContext;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * A SOAPElement that gives access to its content as XML fragment or Java object.
 *
 * The SOAPContentElement has three content representations, which may exist in parallel.
 * The getter and setter of the content properties perform the conversions.
 * It is the responsibility of this objects to keep the representations in sync.
 *
 * +---------+         +-------------+          +-------------+
 * | Object  | <-----> | XMLFragment |  <-----> | DOMTree     |
 * +---------+         +-------------+          +-------------+
 *
 * The idea is, that jaxrpc handlers can work with both the object and the dom view of this SOAPElement.
 * Note, that state transitions may be expensive.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 13-Dec-2004
 */
public class SOAPContentElement extends SOAPElementImpl
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPContentElement.class);

   // The well formed XML content of this element.
   private String xmlFragment;
   // The java object content of this element.
   private Object objectValue;
   // True if the current DOM tree is valid
   private boolean isDOMValid;
   // True if the current content object is valid
   private boolean isObjectValid;
   // True while expanding to DOM
   private boolean expandingToDOM;

   // The associated parameter
   private ParameterMetaData paramMetaData;

   /** Construct a SOAPContentElement
    */
   public SOAPContentElement(Name name)
   {
      super(name);
      isDOMValid = true;
   }

   public SOAPContentElement(QName qname)
   {
      super(qname);
      isDOMValid = true;
   }

   public SOAPContentElement(SOAPElementImpl element)
   {
      super(element);
      isDOMValid = true;
   }

   public ParameterMetaData getParamMetaData()
   {
      if (paramMetaData == null)
         throw new IllegalStateException("Parameter meta data not available");

      return paramMetaData;
   }

   public void setParamMetaData(ParameterMetaData paramMetaData)
   {
      this.paramMetaData = paramMetaData;
   }

   public QName getXmlType()
   {
      return getParamMetaData().getXmlType();
   }

   public Class getJavaType()
   {
      return getParamMetaData().getJavaType();
   }

   public boolean isDOMValid()
   {
      return isDOMValid;
   }

   public boolean isObjectValid()
   {
      return isObjectValid;
   }

   public boolean isFragmentValid()
   {
      return xmlFragment != null;
   }
   
   /** Get the payload as source. 
    */
   public Source getPayload()
   {
      // expand to DOM, so the source is repeatedly readable
      expandToDOM();
      return new DOMSource(this);
   }

   /** Set the payload as source 
    */
   public void setPayload(Source source)
   {
      try
      {
         TransformerFactory tf = TransformerFactory.newInstance();
         ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
         tf.newTransformer().transform(source, new StreamResult(baos));
         String xmlFragment = new String(baos.toByteArray());
         if (xmlFragment.startsWith("<?xml"))
         {
            int index = xmlFragment.indexOf(">");
            xmlFragment = xmlFragment.substring(index + 1);
         }
         setXMLFragment(xmlFragment);
      }
      catch (TransformerException ex)
      {
         WSException.rethrow(ex);
      }
   }

   public String getXMLFragment()
   {
      // Serialize the valueContent
      if (xmlFragment == null && isObjectValid)
      {
         assertContentMapping();

         QName xmlType = getXmlType();
         Class javaType = getJavaType();
         QName xmlName = getElementQName();

         if(log.isDebugEnabled()) log.debug("getXMLFragment from Object [xmlType=" + xmlType + ",javaType=" + javaType + "]");

         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         SerializationContext serContext = msgContext.getSerializationContext();
         serContext.setProperty(ParameterMetaData.class.getName(), getParamMetaData());

         TypeMappingImpl typeMapping = serContext.getTypeMapping();

         try
         {
            SerializerSupport ser;
            if (objectValue != null)
            {
               SerializerFactoryBase serializerFactory = getSerializerFactory(typeMapping, javaType, xmlType);
               ser = (SerializerSupport)serializerFactory.getSerializer();
            }
            else
            {
               ser = new NullValueSerializer();
            }

            String tmpFragment = ser.serialize(xmlName, xmlType, getObjectValue(), serContext, null);
            if(log.isDebugEnabled()) log.debug("xmlFragment: " + tmpFragment);

            setXMLFragment(tmpFragment);
         }
         catch (BindingException e)
         {
            throw new WSException(e);
         }
      }

      // Generate the xmlFragment from the DOM tree
      else if (xmlFragment == null && isDOMValid)
      {
         if(log.isDebugEnabled()) log.debug("getXMLFragment from DOM");
         xmlFragment = DOMWriter.printNode(this, false);
         if(log.isDebugEnabled()) log.debug("xmlFragment: " + xmlFragment);
         invalidateDOMContent();
      }

      if (xmlFragment == null || xmlFragment.startsWith("<") == false)
         throw new WSException("Invalid XMLFragment: " + xmlFragment);

      return xmlFragment;
   }

   public void setXMLFragment(String xmlFragment)
   {
      if(log.isDebugEnabled()) log.debug("setXMLFragment: " + xmlFragment);

      if (xmlFragment == null || xmlFragment.startsWith("<") == false)
         throw new WSException("Invalid XMLFragment: " + xmlFragment);

      removeContentsAsIs();
      resetElementContent();

      this.xmlFragment = xmlFragment;
      invalidateDOMContent();
      invalidateObjectContent();
   }

   public Object getObjectValue()
   {
      if (isObjectValid == false)
      {
         QName xmlType = getXmlType();
         Class javaType = getJavaType();

         if(log.isDebugEnabled()) log.debug("getObjectValue [xmlType=" + xmlType + ",javaType=" + javaType + "]");
         assertContentMapping();

         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         SerializationContext serContext = msgContext.getSerializationContext();
         ParameterMetaData pmd = getParamMetaData();
         serContext.setProperty(ParameterMetaData.class.getName(), pmd);
         List<Class> registeredTypes = pmd.getOperationMetaData().getEndpointMetaData().getRegisteredTypes();
         serContext.setProperty(SerializationContext.CONTEXT_TYPES, registeredTypes.toArray(new Class[0]));

         try
         {
            // Get the deserializer from the type mapping
            TypeMappingImpl typeMapping = serContext.getTypeMapping();
            DeserializerFactoryBase deserializerFactory = getDeserializerFactory(typeMapping, javaType, xmlType);
            DeserializerSupport des = (DeserializerSupport)deserializerFactory.getDeserializer();

            String strContent = getXMLFragment();

            Object obj = des.deserialize(getElementQName(), xmlType, strContent, serContext);
            if (obj != null)
            {
               Class objType = obj.getClass();
               boolean isAssignable = JavaUtils.isAssignableFrom(javaType, objType);
               if (isAssignable == false && javaType.isArray())
               {
                  try
                  {
                     Method toArrayMethod = objType.getMethod("toArray", new Class[] {});
                     Class returnType = toArrayMethod.getReturnType();
                     if (JavaUtils.isAssignableFrom(javaType, returnType))
                     {
                        Method getValueMethod = objType.getMethod("getValue", new Class[] {});
                        Object value = getValueMethod.invoke(obj, new Object[] {});
                        if (value != null)
                        {
                           // Do not invoke toArray if getValue returns null
                           obj = toArrayMethod.invoke(obj, new Object[] {});
                        }
                        else
                        {
                           // if the fragment did not indicate a null return
                           // by an xsi:nil we return an empty array
                           Class componentType = javaType.getComponentType();
                           obj = Array.newInstance(componentType, 0);
                        }
                        isAssignable = true;
                     }
                  }
                  catch (Exception e)
                  {
                     // ignore
                  }
               }

               if (isAssignable == false)
               {
                  // handle XOP simple types, i.e. in RPC/LIT
                  try
                  {
                     String contentType = MimeUtils.resolveMimeType(javaType);
                     if(log.isDebugEnabled()) log.debug("Adopt DataHandler to " + javaType +", contentType "+ contentType);

                     DataSource ds = new SwapableMemoryDataSource(((DataHandler)obj).getInputStream(), contentType);
                     DataHandler dh = new DataHandler(ds);
                     obj = dh.getContent();

                     // 'application/octet-stream' will return a byte[] instead fo the stream
                     if(obj instanceof InputStream)
                     {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        dh.writeTo(bout);
                        obj = bout.toByteArray();
                     }
                  }
                  catch (IOException e)
                  {
                     throw new WSException("Failed to adopt XOP content type", e);
                  }

                  if(!JavaUtils.isAssignableFrom(javaType, obj.getClass()))
                  {
                     throw new WSException("Java type '" + javaType + "' is not assignable from: " + objType.getName());
                  }
               }
            }

            this.objectValue = obj;
            this.isObjectValid = true;
         }
         catch (BindingException e)
         {
            throw new WSException(e);
         }

         if(log.isDebugEnabled()) log.debug("objectValue: " + (objectValue != null ? objectValue.getClass().getName() : null));
      }

      return objectValue;
   }

   public void setObjectValue(Object objValue)
   {
      if(log.isDebugEnabled()) log.debug("setObjectValue: " + objValue);
      removeContentsAsIs();
      resetElementContent();
      this.objectValue = objValue;
      this.isObjectValid = true;
   }

   private void removeContentsAsIs()
   {
      log.trace("removeContentsAsIs");
      boolean cachedFlag = isDOMValid;
      try
      {
         this.isDOMValid = true;
         super.removeContents();
      }
      finally
      {
         this.isDOMValid = cachedFlag;
      }
   }

   // Get the serializer factory for a given javaType and xmlType
   private SerializerFactoryBase getSerializerFactory(TypeMappingImpl typeMapping, Class javaType, QName xmlType)
   {
      SerializerFactoryBase serializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(javaType, xmlType);

      // The type mapping might contain a mapping for the array wrapper bean
      if (serializerFactory == null && javaType.isArray())
      {
         Class arrayWrapperType = typeMapping.getJavaType(xmlType);
         if (arrayWrapperType != null)
         {
            try
            {
               Method toArrayMethod = arrayWrapperType.getMethod("toArray", new Class[] {});
               Class returnType = toArrayMethod.getReturnType();
               if (JavaUtils.isAssignableFrom(javaType, returnType))
               {
                  serializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(arrayWrapperType, xmlType);
               }
            }
            catch (NoSuchMethodException e)
            {
               // ignore
            }
         }
      }

      if (serializerFactory == null)
         throw new WSException("Cannot obtain serializer factory for: [xmlType=" + xmlType + ",javaType=" + javaType + "]");

      return serializerFactory;
   }

   // Get the deserializer factory for a given javaType and xmlType
   private DeserializerFactoryBase getDeserializerFactory(TypeMappingImpl typeMapping, Class javaType, QName xmlType)
   {
      DeserializerFactoryBase deserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(javaType, xmlType);

      // The type mapping might contain a mapping for the array wrapper bean
      if (deserializerFactory == null && javaType.isArray())
      {
         Class arrayWrapperType = typeMapping.getJavaType(xmlType);
         if (arrayWrapperType != null)
         {
            try
            {
               Method toArrayMethod = arrayWrapperType.getMethod("toArray", new Class[] {});
               Class returnType = toArrayMethod.getReturnType();
               if (JavaUtils.isAssignableFrom(javaType, returnType))
               {
                  deserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(arrayWrapperType, xmlType);
               }
            }
            catch (NoSuchMethodException e)
            {
               // ignore
            }
         }
      }

      if (deserializerFactory == null)
         throw new WSException("Cannot obtain deserializer factory for: [xmlType=" + xmlType + ",javaType=" + javaType + "]");

      return deserializerFactory;
   }

   /** Assert the notNull state of the xmlType and javaType
    */
   private void assertContentMapping()
   {
      if (getJavaType() == null)
         throw new WSException("javaType cannot be null");
      if (getXmlType() == null)
         throw new WSException("xmlType cannot be null");
   }

   // SOAPElement interface ********************************************************************************************

   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      log.trace("addChildElement: " + child);
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(child);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String localName, String prefix) throws SOAPException
   {
      log.trace("addChildElement: [localName=" + localName + ",prefix=" + prefix + "]");
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(localName, prefix);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException
   {
      log.trace("addChildElement: [localName=" + localName + ",prefix=" + prefix + ",uri=" + uri + "]");
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(localName, prefix, uri);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(Name name) throws SOAPException
   {
      log.trace("addChildElement: [name=" + name + "]");
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(name);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String name) throws SOAPException
   {
      log.trace("addChildElement: [name=" + name + "]");
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(name);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addTextNode(String value) throws SOAPException
   {
      log.trace("addTextNode: [value=" + value + "]");
      expandToDOM();
      SOAPElement soapElement = super.addTextNode(value);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public Iterator getChildElements()
   {
      log.trace("getChildElements");
      expandToDOM();
      return super.getChildElements();
   }

   public Iterator getChildElements(Name name)
   {
      log.trace("getChildElements: [name=" + name + "]");
      expandToDOM();
      return super.getChildElements(name);
   }

   public void removeContents()
   {
      log.trace("removeContents");
      expandToDOM();
      super.removeContents();
      invalidateObjectContent();
      invalidateXMLContent();
   }

   public Iterator getAllAttributes()
   {
      return super.getAllAttributes();
   }

   public String getAttribute(String name)
   {
      return super.getAttribute(name);
   }

   public Attr getAttributeNode(String name)
   {
      return super.getAttributeNode(name);
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName)
   {
      return super.getAttributeNodeNS(namespaceURI, localName);
   }

   public String getAttributeNS(String namespaceURI, String localName)
   {
      return super.getAttributeNS(namespaceURI, localName);
   }

   public String getAttributeValue(Name name)
   {
      return super.getAttributeValue(name);
   }

   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      log.trace("addAttribute: [name=" + name + ",value=" + value + "]");
      expandToDOM();
      return super.addAttribute(name, value);
   }

   public SOAPElement addNamespaceDeclaration(String prefix, String nsURI)
   {
      log.trace("addNamespaceDeclaration: [prefix=" + prefix + ",nsURI=" + nsURI + "]");
      expandToDOM();
      return super.addNamespaceDeclaration(prefix, nsURI);
   }

   public Name getElementName()
   {
      return super.getElementName();
   }

   public NodeList getElementsByTagName(String name)
   {
      log.trace("getElementsByTagName: [name=" + name + "]");
      expandToDOM();
      return super.getElementsByTagName(name);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      log.trace("getElementsByTagName: [nsURI=" + namespaceURI + ",localName=" + localName + "]");
      expandToDOM();
      return super.getElementsByTagNameNS(namespaceURI, localName);
   }

   public String getEncodingStyle()
   {
      return super.getEncodingStyle();
   }

   public Iterator getNamespacePrefixes()
   {
      return super.getNamespacePrefixes();
   }

   public String getNamespaceURI(String prefix)
   {
      return super.getNamespaceURI(prefix);
   }

   public TypeInfo getSchemaTypeInfo()
   {
      return super.getSchemaTypeInfo();
   }

   public String getTagName()
   {
      return super.getTagName();
   }

   public Iterator getVisibleNamespacePrefixes()
   {
      return super.getVisibleNamespacePrefixes();
   }

   public boolean hasAttribute(String name)
   {
      return super.hasAttribute(name);
   }

   public boolean hasAttributeNS(String namespaceURI, String localName)
   {
      return super.hasAttributeNS(namespaceURI, localName);
   }

   public boolean removeAttribute(Name name)
   {
      log.trace("removeAttribute: " + name.getQualifiedName());
      expandToDOM();
      return super.removeAttribute(name);
   }

   public void removeAttribute(String name) throws DOMException
   {
      log.trace("removeAttribute: " + name);
      expandToDOM();
      super.removeAttribute(name);
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException
   {
      log.trace("removeAttribute: " + oldAttr.getNodeName());
      expandToDOM();
      return super.removeAttributeNode(oldAttr);
   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      log.trace("removeAttributeNS: {" + namespaceURI + "}" + localName);
      expandToDOM();
      super.removeAttributeNS(namespaceURI, localName);
   }

   public boolean removeNamespaceDeclaration(String prefix)
   {
      log.trace("removeNamespaceDeclaration: " + prefix);
      expandToDOM();
      return super.removeNamespaceDeclaration(prefix);
   }

   public void setAttribute(String name, String value) throws DOMException
   {
      log.trace("setAttribute: [name=" + name + ",value=" + value + "]");
      expandToDOM();
      super.setAttribute(name, value);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException
   {
      log.trace("setAttributeNode: " + newAttr);
      expandToDOM();
      return super.setAttributeNode(newAttr);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      log.trace("setAttributeNodeNS: " + newAttr);
      expandToDOM();
      return super.setAttributeNodeNS(newAttr);
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
   {
      log.trace("setAttribute: [nsURI=" + namespaceURI + ",name=" + qualifiedName + ",value=" + value + "]");
      expandToDOM();
      super.setAttributeNS(namespaceURI, qualifiedName, value);
   }

   public void setEncodingStyle(String encodingStyle) throws SOAPException
   {
      super.setEncodingStyle(encodingStyle);
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      log.trace("setIdAttribute: [name=" + name + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttribute(name, isId);
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      log.trace("setIdAttributeNode: [idAttr=" + idAttr + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttributeNode(idAttr, isId);
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      log.trace("setIdAttributeNS: [nsURI=" + namespaceURI + ",name=" + localName + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttributeNS(namespaceURI, localName, isId);
   }

   // Node interface **************************************************************************************************

   public Node appendChild(Node newChild) throws DOMException
   {
      log.trace("appendChild: " + newChild);
      expandToDOM();
      Node node = super.appendChild(newChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public Node cloneNode(boolean deep)
   {
      log.trace("cloneNode: deep=" + deep);
      expandToDOM();
      return super.cloneNode(deep);
   }

   public NodeList getChildNodes()
   {
      log.trace("getChildNodes");
      expandToDOM();
      return super.getChildNodes();
   }

   public Node getFirstChild()
   {
      log.trace("getFirstChild");
      expandToDOM();
      return super.getFirstChild();
   }

   public Node getLastChild()
   {
      log.trace("getLastChild");
      expandToDOM();
      return super.getLastChild();
   }

   public String getValue()
   {
      log.trace("getValue");
      expandToDOM();
      return super.getValue();
   }

   public boolean hasChildNodes()
   {
      log.trace("hasChildNodes");
      expandToDOM();
      return super.hasChildNodes();
   }

   public Node removeChild(Node oldChild) throws DOMException
   {
      log.trace("removeChild: " + oldChild);
      expandToDOM();
      Node node = super.removeChild(oldChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      log.trace("replaceChild: [new=" + newChild + ",old=" + oldChild + "]");
      expandToDOM();
      Node node = super.replaceChild(newChild, oldChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public void setValue(String value)
   {
      log.trace("setValue: " + value);
      expandToDOM();
      super.setValue(value);
      invalidateObjectContent();
      invalidateXMLContent();
   }

   public NamedNodeMap getAttributes()
   {
      return super.getAttributes();
   }

   public boolean hasAttributes()
   {
      return super.hasAttributes();
   }

   // END Node interface ***********************************************************************************************

   /** Expand the content, generating appropriate child nodes
    */
   private void expandToDOM()
   {
      // SOAPContentElements should only be expanded when handlers do require it.
      if (isDOMValid == false && expandingToDOM == false)
      {
         log.trace("BEGIN: expandToDOM " + getElementName());
         expandingToDOM = true;

         // DOM expansion should only happen when a handler accesses the DOM API.
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext != null && !UnifiedMetaData.isFinalRelease())
         {
            Boolean allowExpand = (Boolean)msgContext.getProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            if (Boolean.TRUE.equals(allowExpand) == false)
               throw new WSException("Expanding content element to DOM");
         }

         try
         {
            if (xmlFragment == null && isObjectValid)
               xmlFragment = getXMLFragment();

            if (xmlFragment == null && isObjectValid == false)
               throw new IllegalStateException("Neither DOM, nor XML, nor Object valid");

            if (xmlFragment != null)
            {
               String wrappedXMLFragment = insertNamespaceDeclarations("<wrapper>" + xmlFragment + "</wrapper>");
               Element contentRoot = DOMUtils.parse(wrappedXMLFragment);
               contentRoot = DOMUtils.getFirstChildElement(contentRoot);

               String rootLocalName = contentRoot.getLocalName();
               String rootPrefix = contentRoot.getPrefix();
               String rootNS = contentRoot.getNamespaceURI();
               Name contentRootName = new NameImpl(rootLocalName, rootPrefix, rootNS);

               // Make sure the content root element name matches this element name
               Name name = getElementName();
               if (contentRootName.equals(name) == false)
                  throw new WSException("Content root name does not match element name: " + contentRootName + " != " + name);

               // Copy attributes
               DOMUtils.copyAttributes(this, contentRoot);

               SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();

               NodeList nlist = contentRoot.getChildNodes();
               for (int i = 0; i < nlist.getLength(); i++)
               {
                  Node child = nlist.item(i);
                  short childType = child.getNodeType();
                  if (childType == Node.ELEMENT_NODE)
                  {
                     SOAPElement soapElement = soapFactory.createElement((Element)child);
                     super.addChildElement(soapElement);
                     if (Constants.NAME_XOP_INCLUDE.equals(name) || isXOPParameter())
                        XOPContext.inlineXOPData(soapElement);
                  }
                  else if (childType == Node.TEXT_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     super.addTextNode(nodeValue);
                  }
                  else if (childType == Node.CDATA_SECTION_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     super.addTextNode(nodeValue);
                  }
                  else
                  {
                     log.trace("Ignore child type: " + childType);
                  }
               }
            }

            isDOMValid = true;
         }
         catch (RuntimeException e)
         {
            invalidateDOMContent();
            throw e;
         }
         catch (Exception e)
         {
            invalidateDOMContent();
            throw new WSException(e);
         }
         finally
         {
            expandingToDOM = false;
            log.trace("END: expandToDOM " + getElementName());
         }

         invalidateXMLContent();
         invalidateObjectContent();
      }
   }

   public String insertNamespaceDeclarations(String xmlfragment)
   {
      StringBuilder xmlBuffer = new StringBuilder(xmlfragment);

      int endIndex = xmlfragment.indexOf(">");
      int insIndex = endIndex;
      if (xmlfragment.charAt(insIndex - 1) == '/')
         insIndex = insIndex - 1;

      SOAPElement soapElement = this;
      while (soapElement != null)
      {
         Iterator it = soapElement.getNamespacePrefixes();
         while (it.hasNext())
         {
            String prefix = (String)it.next();
            String nsURI = soapElement.getNamespaceURI(prefix);
            String nsDecl = " xmlns:" + prefix + "='" + nsURI + "'";

            // Make sure there is not a duplicate on just the wrapper tag
            int nsIndex = xmlBuffer.indexOf("xmlns:" + prefix);
            if (nsIndex < 0 || nsIndex > endIndex)
            {
               xmlBuffer.insert(insIndex, nsDecl);
               endIndex += nsDecl.length();
            }
         }
         soapElement = soapElement.getParentElement();
      }

      log.trace("insertNamespaceDeclarations: " + xmlBuffer);
      return xmlBuffer.toString();
   }

   private void invalidateDOMContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateDOMContent");
         this.isDOMValid = false;
      }
   }

   private void invalidateObjectContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateObjectContent");
         this.isObjectValid = false;
         this.objectValue = null;
      }
   }

   private void invalidateXMLContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateXMLContent");
         this.xmlFragment = null;
      }
   }

   private void resetElementContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("resetElementContent");
         invalidateDOMContent();
         invalidateObjectContent();
         invalidateXMLContent();
      }
   }

   public void writeElement(Writer writer) throws IOException
   {
      handleMTOMTransitions();

      if (isDOMValid)
      {
         new DOMWriter(writer).print(this);
      }
      else
      {
         writer.write(getXMLFragment());
      }
   }

   /**
    * When a SOAPContentElement transitions between dom-valid and xml-valid
    * the XOP elements need to transition from XOP optimized to base64 and reverse.<p>
    *
    * If MTOM is disabled through a message context property we always enforce the
    * base64 representation by expanding to DOM, the same happens when a JAXRPC handler
    * accesses the SOAPContentElement.<p>
    *
    * If the element is in dom-valid state (because a handlers accessed it), upon marshalling
    * it's needs to be decided wether or not the <code>xop:Include</code> should be restored.
    * This as well depends upon the message context property.
    */
   public void handleMTOMTransitions()
   {
      // MTOM processing is only required on XOP parameters
      if( isXOPParameter() == false)
         return;

      if ( !XOPContext.isMTOMEnabled() )
      {
         // If MTOM is disabled, we force dom expansion on XOP parameters.
         // This will inline any XOP include element and remove the attachment part.
         // See SOAPFactoryImpl for details.

         if(log.isDebugEnabled()) log.debug("MTOM disabled: Force inline XOP data");
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         msgContext.setProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);
         expandToDOM();
      }
      else if ( isDOMValid && XOPContext.isMTOMEnabled() )
      {
         // When the DOM representation is valid,
         // but MTOM is enabled we need to convert the inlined
         // element back to an xop:Include element and create the attachment part

         if(log.isDebugEnabled()) log.debug("MTOM enabled: Restore XOP data");
         XOPContext.restoreXOPDataDOM(this);
      }
   }

   private boolean isXOPParameter()
   {
      return paramMetaData != null && paramMetaData.isXOP();
   }

   public void accept(SAAJVisitor visitor)
   {
      visitor.visitSOAPContentElement(this);
   }
}
