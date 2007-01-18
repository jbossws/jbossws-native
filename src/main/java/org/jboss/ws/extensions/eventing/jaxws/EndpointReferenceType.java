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
package org.jboss.ws.extensions.eventing.jaxws;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for EndpointReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://www.w3.org/2005/08/addressing}AttributedURIType"/>
 *         &lt;element name="ReferenceParameters" type="{http://www.w3.org/2005/08/addressing}ReferenceParametersType" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}Metadata" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndpointReferenceType", namespace = "http://www.w3.org/2005/08/addressing", propOrder = {
   "address",
   "referenceParameters",
   "metadata",
   "any"
   })
public class EndpointReferenceType {

   @XmlElement(name = "Address", namespace = "http://www.w3.org/2005/08/addressing")
   protected AttributedURIType address;
   @XmlElement(name = "ReferenceParameters", namespace = "http://www.w3.org/2005/08/addressing")
   protected ReferenceParametersType referenceParameters;
   @XmlElement(name = "Metadata", namespace = "http://www.w3.org/2005/08/addressing")
   protected MetadataType metadata;
   @XmlAnyElement(lax = true)
   protected List<Object> any;
   @XmlAnyAttribute
   private Map<QName, String> otherAttributes = new HashMap<QName, String>();

   /**
    * Gets the value of the address property.
    *
    * @return
    *     possible object is
    *     {@link AttributedURIType }
    *
    */
   public AttributedURIType getAddress() {
      return address;
   }

   /**
    * Sets the value of the address property.
    *
    * @param value
    *     allowed object is
    *     {@link AttributedURIType }
    *
    */
   public void setAddress(AttributedURIType value) {
      this.address = value;
   }

   /**
    * Gets the value of the referenceParameters property.
    *
    * @return
    *     possible object is
    *     {@link ReferenceParametersType }
    *
    */
   public ReferenceParametersType getReferenceParameters() {
      return referenceParameters;
   }

   /**
    * Sets the value of the referenceParameters property.
    *
    * @param value
    *     allowed object is
    *     {@link ReferenceParametersType }
    *
    */
   public void setReferenceParameters(ReferenceParametersType value) {
      this.referenceParameters = value;
   }

   /**
    * Gets the value of the metadata property.
    *
    * @return
    *     possible object is
    *     {@link MetadataType }
    *
    */
   public MetadataType getMetadata() {
      return metadata;
   }

   /**
    * Sets the value of the metadata property.
    *
    * @param value
    *     allowed object is
    *     {@link MetadataType }
    *
    */
   public void setMetadata(MetadataType value) {
      this.metadata = value;
   }

   /**
    * Gets the value of the any property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the any property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAny().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Element }
    * {@link Object }
    *
    *
    */
   public List<Object> getAny() {
      if (any == null) {
         any = new ArrayList<Object>();
      }
      return this.any;
   }

   /**
    * Gets a map that contains attributes that aren't bound to any typed property on this class.
    *
    * <p>
    * the map is keyed by the name of the attribute and
    * the value is the string value of the attribute.
    *
    * the map returned by this method is live, and you can add new attribute
    * by updating the map directly. Because of this design, there's no setter.
    *
    *
    * @return
    *     always non-null
    */
   public Map<QName, String> getOtherAttributes() {
      return otherAttributes;
   }

   public String toString() {
      return "EPR {address="+this.getAddress()+", refParam="+this.getReferenceParameters()+"}";
   }

}
