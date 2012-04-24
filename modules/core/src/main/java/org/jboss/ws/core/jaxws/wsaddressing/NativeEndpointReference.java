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
package org.jboss.ws.core.jaxws.wsaddressing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

/**
 * Internal representation of the W3CEndpointReference.
 * This allows the W3CEndpointReference to programmatically
 * build an EndpointReference specifying every parameter.
 * Instances of this class are converted to (and can be
 * created from) W3CEndpointReference using the
 * @see EndpointReferenceUtil class.
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @since 28-Feb-2009
 */
@XmlRootElement(name = "EndpointReference", namespace = NativeEndpointReference.WSA_NS)
@XmlType(name = "EndpointReferenceType", namespace = NativeEndpointReference.WSA_NS)
public final class NativeEndpointReference extends EndpointReference
{
   protected static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
   private static final String WSAM_NS = "http://www.w3.org/2007/05/addressing/metadata";
   private static final String WSDLI_NS = "http://www.w3.org/ns/wsdl-instance";
   private static final QName SERVICE_QNAME = new QName(WSAM_NS, "ServiceName", "wsam");
   private static final QName INTERFACE_QNAME = new QName(WSAM_NS, "InterfaceName", "wsam");
   private static final QName WSDL_LOCATION_QNAME = new QName(WSDLI_NS, "wsdlLocation", "wsdli");
   private static final String ENDPOINT_ATTRIBUTE = "EndpointName";
   private static final JAXBContext jc = getJaxbContext();
   
   // private but necessary properties for databinding
   @XmlElement(name = "Address", namespace = WSA_NS)
   private Address address;
   @XmlElement(name = "ReferenceParameters", namespace = WSA_NS)
   private Elements referenceParameters;
   @XmlElement(name = "Metadata", namespace = WSA_NS)
   private Elements metadata;
   @XmlAnyAttribute
   private Map<QName, String> attributes;
   @XmlAnyElement
   private List<Element> elements;

   // not marshalled
   private QName serviceName;
   private Element serviceNameElement;
   private QName endpointName;
   private QName interfaceName;
   private String wsdlLocation;
   
   public NativeEndpointReference()
   {
   }

   /**
    * Creates an EPR from infoset representation
    *
    * @param source A source object containing valid XmlInfoset
    * instance consistent with the W3C WS-Addressing Core
    * recommendation.
    *
    * @throws WebServiceException
    *   If the source does NOT contain a valid W3C WS-Addressing
    *   EndpointReference.
    * @throws NullPointerException
    *   If the <code>null</code> <code>source</code> value is given
    */
   public NativeEndpointReference(Source source)
   {
      try
      {
         NativeEndpointReference epr = jc.createUnmarshaller().unmarshal(source, NativeEndpointReference.class).getValue();
         this.address = epr.address;
         if ((epr.referenceParameters != null) && (!epr.referenceParameters.isEmpty()))
         {
            this.referenceParameters = epr.referenceParameters;
         }
         if ((epr.metadata != null) && (!epr.metadata.isEmpty()))
         {
            this.metadata = epr.metadata;
         }
         this.attributes = epr.attributes;
         this.elements = epr.elements;
         if (epr.metadata != null)
         {
            Map<QName, String> metadataAttributes = epr.metadata.getAttributes();
            if (metadataAttributes != null)
            {
               final String wsdlLocation = metadataAttributes.get(WSDL_LOCATION_QNAME);
               if (wsdlLocation != null)
               {
                  int spaceIndex = wsdlLocation.indexOf(" ");
                  if (spaceIndex == -1)
                     throw new IllegalArgumentException("wsdlLocation have to specify both wsdl namespace and target wsdl location");
                  
                  this.setWsdlLocation(wsdlLocation.substring(spaceIndex).trim());
               }
            }
            List<Element> metadataElements = epr.metadata.getElements();
            if (metadataElements != null)
            {
               for (Element e : epr.metadata.getElements())
               {
                  if (WSAM_NS.equals(e.getNamespaceURI()))
                  {
                     if (e.getLocalName().equals(SERVICE_QNAME.getLocalPart()))
                     {
                        this.serviceName = this.getQName(e, e.getTextContent());
                        String endpointName = e.getAttribute(ENDPOINT_ATTRIBUTE); 
                        if (endpointName != null)
                        {
                           this.endpointName = new QName(
                              this.serviceName.getNamespaceURI(),
                              endpointName,
                              this.serviceName.getPrefix()
                           );
                        }
                     }
                     if (e.getLocalName().equals(INTERFACE_QNAME.getLocalPart()))
                     {
                        this.interfaceName = this.getQName(e, e.getTextContent());
                     }
                  }
               }
            }
         }
      }
      catch (JAXBException e)
      {
         throw new WebServiceException("Error unmarshalling NativeEndpointReference", e);
      }
      catch (ClassCastException e)
      {
         throw new WebServiceException("Source did not contain NativeEndpointReference",  e);
      }
   }

   @XmlTransient
   public String getAddress()
   {
      return address != null ? address.getUri() : null;
   }

   public void setAddress(String address)
   {
      if (address == null)
         return;
      
      this.address = new Address(address);
   }
   
   @XmlTransient
   public QName getServiceName()
   {
      return serviceName;
   }

   public void setServiceName(final QName serviceName)
   {
      if (serviceName == null)
         return;
      
      this.serviceName = serviceName;
      this.serviceNameElement = DOMUtils.createElement(SERVICE_QNAME);
      final String attrName = this.getNamespaceAttributeName(serviceName.getPrefix());
      this.serviceNameElement.setAttribute(attrName, serviceName.getNamespaceURI());
      this.serviceNameElement.setTextContent(this.toString(serviceName));
      if (this.metadata == null)
         this.metadata = new Elements();
      
      this.metadata.addElement(this.serviceNameElement);
   }

   @XmlTransient
   public QName getEndpointName()
   {
      return endpointName;
   }

   public void setEndpointName(QName endpointName)
   {
      if (endpointName == null)
         return;
      
      this.endpointName = endpointName;
   }

   @XmlTransient
   public QName getInterfaceName()
   {
      return interfaceName;
   }

   public void setInterfaceName(final QName interfaceName)
   {
      if (interfaceName == null)
         return;
         
      this.interfaceName = interfaceName;
      Element interfaceNameElement  = DOMUtils.createElement(INTERFACE_QNAME);
      final String attrName = this.getNamespaceAttributeName(interfaceName.getPrefix());
      interfaceNameElement.setAttribute(attrName, interfaceName.getNamespaceURI());
      interfaceNameElement.setTextContent(this.toString(interfaceName));
      if (this.metadata == null)
         this.metadata = new Elements();
      
      this.metadata.addElement(interfaceNameElement);
   }
   
   @XmlTransient
   public List<Element> getMetadata()
   {
      if (this.metadata == null)
         return null;
      
      return this.metadata.getElements();
   }

   public void setMetadata(List<Element> metadata)
   {
      if ((metadata == null) || (metadata.size() == 0))
         return;
      
      if (this.metadata == null)
         this.metadata = new Elements();
      
      this.metadata.setElements(metadata);
   }

   @XmlTransient
   public URL getWsdlLocation()
   {
      if (this.wsdlLocation != null)
      {
         return this.toURL(this.wsdlLocation);
      }
      else
      {
         String address = this.getAddress();
         if (address != null)
         {
            return this.toURL(address + "?wsdl");
         }
      }
      
      return null;
   }
   
   public void setWsdlLocation(String wsdlLocation)
   {
      if (wsdlLocation == null)
         return;
      
      this.wsdlLocation = wsdlLocation;
   }

   @XmlTransient
   public List<Element> getReferenceParameters()
   {
      if (this.referenceParameters == null)
         return null;
      
      return this.referenceParameters.getElements();
   }

   public void setReferenceParameters(List<Element> metadata)
   {
      if ((metadata == null) || (metadata.size() == 0))
         return;
      
      if (this.referenceParameters == null)
         this.referenceParameters = new Elements();
      
      this.referenceParameters.setElements(metadata);
   }
   
   /**
    * Directly read a NativeEndpointReference from the given source
    * instead of leveraging the Provider's readEndpointReference method.
    * 
    * @param eprInfoset
    * @return
    */
   public static EndpointReference readFrom(Source eprInfoset)
   {
      if (eprInfoset == null)
         throw new NullPointerException("Provided eprInfoset cannot be null");
      try
      {
         return new NativeEndpointReference(eprInfoset);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(Result result)
   {
      if (this.endpointName != null && this.serviceNameElement != null)
      {
         this.serviceNameElement.setAttribute(ENDPOINT_ATTRIBUTE, this.endpointName.getLocalPart());
         
         if (this.wsdlLocation != null)
         {
            if (this.metadata == null)
               this.metadata = new Elements();

            String wsdlNamespace = null;
            if (this.endpointName != null)
            {
               wsdlNamespace = this.endpointName.getNamespaceURI();
            }
            else if (this.serviceName != null)
            {
               wsdlNamespace = this.serviceName.getNamespaceURI();
            }

            if (wsdlNamespace == null && this.wsdlLocation != null)
               throw new IllegalStateException("Either serviceName or endpointName have to be specified when providing wsdlLocation");
            
            this.metadata.addAttribute(WSDL_LOCATION_QNAME, wsdlNamespace + " " + this.wsdlLocation);
         }
      }
      try
      {
         Marshaller marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
         marshaller.marshal(this, result);
      }
      catch (JAXBException e)
      {
         throw new WebServiceException("Error marshalling NativeEndpointReference",  e);
      }
   }

   private URL toURL(final String s)
   {
      try
      {
         return new URL(s);
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException(e.getMessage(), e);
      }
   }

   private static JAXBContext getJaxbContext()
   {
      try
      {
         return JAXBContext.newInstance(new Class[] { NativeEndpointReference.class });
      }
      catch (JAXBException ex)
      {
         throw new WebServiceException("=Cannot obtain JAXB context",  ex);
      }
   }

   private String toString(final QName qname)
   {
      StringBuilder sb = new StringBuilder();
      if (qname.getPrefix() != null && qname.getPrefix().length() > 0)
      {
         sb.append(qname.getPrefix());
         sb.append(':');
      }
      sb.append(qname.getLocalPart());
      return sb.toString();
   }
   
   private QName getQName(Element e, String nodeValue)
   {
      if (nodeValue == null)
         throw new RuntimeException("Missing text content for element : " + e.getNodeName());
      
      final int separatorIndex = nodeValue.indexOf(':');
      if (separatorIndex == -1)
      {
         final String namespace = e.getAttribute("xmlns");
         return new QName(namespace, nodeValue);
      }
      else
      {
         final String prefix = nodeValue.substring(0, separatorIndex);
         final String localPart = nodeValue.substring(separatorIndex + 1);
         final String namespace = e.lookupNamespaceURI(prefix);
         return new QName(namespace, localPart, prefix);
      }
   }

   private String getNamespaceAttributeName(final String prefix)
   {
      if (prefix == null || "".equals(prefix))
         return "xmlns";
      
      return "xmlns:" + prefix;
   }
   
   private static class Address
   {
      @XmlValue
      String uri;
      @XmlAnyAttribute
      Map<QName, String> attributes;
      
      protected Address()
      {
      }

      public Address(String uri)
      {
         this.uri = uri;
      }

      @XmlTransient
      public String getUri()
      {
         return uri;
      }

      public void setUri(String uri)
      {
         this.uri = uri;
      }

      @XmlTransient
      public Map<QName, String> getAttributes()
      {
         return attributes;
      }

      public void setAttributes(Map<QName, String> attributes)
      {
         this.attributes = attributes;
      }
   }

   private static class Elements
   {
      @XmlAnyElement
      List<Element> elements;
      @XmlAnyAttribute
      Map<QName, String> attributes;
      
      protected Elements()
      {
      }

      public Elements(List<Element> elements)
      {
         this.elements = elements;
      }

      @XmlTransient
      public List<Element> getElements()
      {
         return elements;
      }

      public void setElements(List<Element> elements)
      {
         if (this.elements == null)
         {
            this.elements = elements;
         }
         else
         {
            this.elements.addAll(elements);
         }
      }
      
      public void addElement(Element e)
      {
         if (this.elements == null)
         {
            this.elements = new LinkedList<Element>();
         }
         this.elements.add(e);
      }

      @XmlTransient
      public Map<QName, String> getAttributes()
      {
         return attributes;
      }

      public void setAttributes(Map<QName, String> attributes)
      {
         if (this.attributes == null)
         {
            this.attributes = attributes;
         }
         else
         {
            this.attributes.putAll(attributes);
         }
      }
      
      public void addAttribute(QName attrName, String attrValue)
      {
         if (this.attributes == null)
         {
            this.attributes = new HashMap<QName, String>();
         }
         this.attributes.put(attrName, attrValue);
      }
      
      @XmlTransient
      public boolean isEmpty()
      {
         final boolean noAttributes = this.attributes == null || this.attributes.size() == 0;
         final boolean noElements = this.elements == null || this.elements.size() == 0;
         
         return noAttributes && noElements;
      }
   }
   
}