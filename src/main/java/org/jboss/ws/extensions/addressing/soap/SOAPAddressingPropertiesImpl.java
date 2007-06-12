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
package org.jboss.ws.extensions.addressing.soap;

//$Id$

import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.soap.NameImpl;
import org.jboss.ws.core.soap.SOAPFactoryImpl;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.extensions.addressing.AddressingPropertiesImpl;
import org.jboss.ws.extensions.addressing.EndpointReferenceImpl;
import org.jboss.wsf.spi.utils.DOMUtils;
import org.jboss.xb.binding.NamespaceRegistry;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.ReferenceParameters;
import javax.xml.ws.addressing.Relationship;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Subimplementation of <code>AddressingProperties</code> includes methods that
 * read and write the Message Addressing Properties to a <code>SOAPMessage</code>.
 * All individual properties must implement <code>SOAPAddressingElement</code>.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Nov-2005
 */
public class SOAPAddressingPropertiesImpl extends AddressingPropertiesImpl implements SOAPAddressingProperties
{
	private static AddressingConstants ADDR = new AddressingConstantsImpl();

	private NamespaceRegistry nsRegistry = new NamespaceRegistry();

	private String getRequiredHeaderContent(SOAPHeader soapHeader, QName qname)
	{
		Element element = DOMUtils.getFirstChildElement(soapHeader, qname);
		if(null == element) throw new AddressingException("Required element "+qname+" is missing");

		String value = DOMUtils.getTextContent(element);
		if(null == value || value.equals("")) throw new AddressingException("Required element "+qname+" is missing");
		
		return value;
	}

	private String getOptionalHeaderContent(SOAPHeader soapHeader, QName qname)
	{
		Element element = DOMUtils.getFirstChildElement(soapHeader, qname);
		if (element != null)
		{
			return DOMUtils.getTextContent(element);
		}

		return null;
	}
	
	public void readHeaders(SOAPMessage message) throws AddressingException
	{
		try
		{
			SOAPHeader soapHeader = message.getSOAPHeader();

			SOAPAddressingBuilder builder = new SOAPAddressingBuilderImpl();
			AddressingConstants ADDR = builder.newAddressingConstants();
			registerNamespaces(ADDR, soapHeader);
			
			// required elements
			String action = getRequiredHeaderContent(soapHeader, ADDR.getActionQName());			
			String to = getRequiredHeaderContent(soapHeader, ADDR.getToQName());			

			// wsa:Action
			// This REQUIRED element of type xs:anyURI conveys the [action] property.
			// The [children] of this element convey the value of this property.
			setAction(builder.newURI(action));

			// wsa:To
			// This REQUIRED element (of type xs:anyURI) provides the value for the [destination] property.
			setTo(builder.newURI(to));

			// Read wsa:From
			// This OPTIONAL element (of type wsa:EndpointReferenceType) provides the value for the [source endpoint] property.
			Element wsaFrom = DOMUtils.getFirstChildElement(soapHeader, ADDR.getFromQName());
			if (wsaFrom != null)
			{
				EndpointReferenceImpl ref = new EndpointReferenceImpl(wsaFrom);
				setReplyTo(ref);
			}

			// Read wsa:ReplyTo
			// This OPTIONAL element (of type wsa:EndpointReferenceType) provides the value for the [reply endpoint] property.
			// This element MUST be present if a reply is expected. If this element is present, wsa:MessageID MUST be present.
			Element wsaReplyTo = DOMUtils.getFirstChildElement(soapHeader, ADDR.getReplyToQName());
			if (wsaReplyTo != null)
			{
				EndpointReferenceImpl ref = new EndpointReferenceImpl(wsaReplyTo);
				setReplyTo(ref);
			}
			
			// Read wsa:FaultTo
			// This OPTIONAL element (of type wsa:EndpointReferenceType) provides the value for the [fault endpoint] property.
			// If this element is present, wsa:MessageID MUST be present.
			Element wsaFaultTo = DOMUtils.getFirstChildElement(soapHeader, ADDR.getFaultToQName());
			if (wsaFaultTo != null)
			{
				EndpointReferenceImpl ref = new EndpointReferenceImpl(wsaFaultTo);
				setFaultTo(ref);
			}

			// Read wsa:MessageID
			// This OPTIONAL element conveys the [message id] property.
			// This element MUST be present if wsa:ReplyTo or wsa:FaultTo is present.
			if(wsaReplyTo!=null || wsaFaultTo!=null)
			{
				String msgIdValue = getRequiredHeaderContent(soapHeader, ADDR.getMessageIDQName());
				setMessageID(builder.newURI(msgIdValue));
			}
			else
			{
				String messageID = getOptionalHeaderContent(soapHeader, ADDR.getMessageIDQName());
				if(messageID!=null) setMessageID(builder.newURI(messageID));
			}
			
			// Read wsa:RelatesTo
			// This OPTIONAL (repeating) element information item contributes one abstract [relationship] property value,
			// in the form of a (URI, QName) pair.
			// The [children] property of this element (which is of type xs:anyURI) conveys the [message id]
			// of the related message. This element MUST be present if the message is a reply.
			Iterator itRelatesTo = DOMUtils.getChildElements(soapHeader, ADDR.getRelatesToQName());
			List<Relationship> relList = new ArrayList<Relationship>();
			while (itRelatesTo.hasNext())
			{
				Element wsaRelatesTo = (Element)itRelatesTo.next();
				QName type = DOMUtils.getAttributeValueAsQName(wsaRelatesTo, ADDR.getRelationshipTypeName());
				String uri = DOMUtils.getTextContent(wsaRelatesTo);
				Relationship rel = builder.newRelationship(new URI(uri));
				rel.setType(type);
				relList.add(rel);
			}
			Relationship[] relArr = (Relationship[])Array.newInstance(Relationship.class, relList.size());
			relList.toArray(relArr);
			setRelatesTo(relArr);

			// Read wsa:ReferenceParameters
			QName refQName = new QName(getNamespaceURI(), "IsReferenceParameter");
			ReferenceParameters refParams = getReferenceParameters();
			Iterator it = soapHeader.examineAllHeaderElements();
			while (it.hasNext())
			{
				SOAPHeaderElement headerElement = (SOAPHeaderElement)it.next();
				if ("true".equals(DOMUtils.getAttributeValue(headerElement, refQName)))
				{
					refParams.addElement(headerElement);
				}
			}
		}
		catch (SOAPException ex)
		{
			throw new AddressingException("Cannot read headers", ex);
		}
		catch (URISyntaxException ex)
		{
			throw new AddressingException("Cannot read headers", ex);
		}
	}

	private void registerNamespaces(AddressingConstants ADDR, SOAPHeader soapHeader)
	{
		// Register wsa namespace
		nsRegistry.registerURI(ADDR.getNamespaceURI(), ADDR.getNamespacePrefix());

		// Register namespaces
		NamedNodeMap attribs = soapHeader.getAttributes();
		for (int i = 0; i < attribs.getLength(); i++)
		{
			Attr attr = (Attr)attribs.item(i);
			String attrName = attr.getName();
			String attrValue = attr.getValue();
			if (attrName.startsWith("xmlns:"))
			{
				String prefix = attrName.substring(6);
				nsRegistry.registerURI(attrValue, prefix);
			}
		}
	}

	public void writeHeaders(SOAPMessage message) throws AddressingException
	{
		try
		{
			SOAPFactoryImpl factory = (SOAPFactoryImpl)SOAPFactory.newInstance();

			SOAPHeader soapHeader = message.getSOAPHeader();

			if (getAction() == null)
				throw new AddressingException("Required addressing property wsa:Action");

			// Add the xmlns:wsa declaration
			soapHeader.addNamespaceDeclaration(ADDR.getNamespacePrefix(), ADDR.getNamespaceURI());

			// Write wsa:To
			if (getTo() != null)
			{
				SOAPElement wsaTo = soapHeader.addChildElement(new NameImpl(ADDR.getToQName()));
				wsaTo.addTextNode(getTo().getURI().toString());
			}

			// Write wsa:From
			if (getFrom() != null)
			{
				EndpointReferenceImpl epr = (EndpointReferenceImpl)getFrom();
				epr.setRootQName(ADDR.getFromQName());
				SOAPElement soapElement = factory.createElement(epr.toElement());
				soapElement.removeNamespaceDeclaration(ADDR.getNamespacePrefix());
				soapHeader.addChildElement(soapElement);
			}

			// Write wsa:ReplyTo
			if (getReplyTo() != null)
			{
				EndpointReferenceImpl epr = (EndpointReferenceImpl)getReplyTo();
				epr.setRootQName(ADDR.getReplyToQName());
				SOAPElement soapElement = factory.createElement(epr.toElement());
				soapElement.removeNamespaceDeclaration(ADDR.getNamespacePrefix());
				soapHeader.addChildElement(soapElement);
			}

			// Write wsa:FaultTo
			if (getFaultTo() != null)
			{
				EndpointReferenceImpl epr = (EndpointReferenceImpl)getFaultTo();
				epr.setRootQName(ADDR.getFaultToQName());
				SOAPElement soapElement = factory.createElement(epr.toElement());
				soapElement.removeNamespaceDeclaration(ADDR.getNamespacePrefix());
				soapHeader.addChildElement(soapElement);
			}

			// Write wsa:Action
			SOAPElement wsaAction = soapHeader.addChildElement(new NameImpl(ADDR.getActionQName()));
			wsaAction.addTextNode(getAction().getURI().toString());

			// Write wsa:MessageID
			if (getMessageID() != null)
			{
				SOAPElement wsaMessageId = soapHeader.addChildElement(new NameImpl(ADDR.getMessageIDQName()));
				wsaMessageId.addTextNode(getMessageID().getURI().toString());
			}

			// Write wsa:RelatesTo
			if (getRelatesTo() != null)
			{
				for (Relationship rel : getRelatesTo())
				{
					SOAPElement wsaRelatesTo = soapHeader.addChildElement(new NameImpl(ADDR.getRelatesToQName()));
					if (rel.getType() != null)
					{
						wsaRelatesTo.setAttribute(ADDR.getRelationshipTypeName(), getPrefixedName(rel.getType()));
					}
					wsaRelatesTo.addTextNode(rel.getID().toString());
				}
			}

			// Write wsa:ReferenceParameters
			ReferenceParameters refParams = getReferenceParameters();
			if (refParams.getElements().size() > 0 || refParams.getAttributes().size() > 0)
			{
				SOAPElement wsaRefParams = soapHeader.addChildElement(new NameImpl(ADDR.getReferenceParametersQName()));
				appendAttributes(wsaRefParams, refParams.getAttributes());
				appendElements(wsaRefParams, refParams.getElements());
			}

			appendElements(soapHeader, getElements());
		}
		catch (SOAPException ex)
		{
			throw new AddressingException("Cannot read headers", ex);
		}
	}

	public void setMu(boolean mu)
	{
		throw new NotImplementedException();
	}

	private void appendAttributes(SOAPElement soapElement, Map<QName, String> attributes)
	{
		for (QName qname : attributes.keySet())
		{
			String qualname = getPrefixedName(qname);
			String value = attributes.get(qname);
			soapElement.setAttribute(qualname, value);
		}
	}

	private void appendElements(SOAPElement soapElement, List<Object> elements)
	{
		try
		{
			SOAPFactoryImpl factory = (SOAPFactoryImpl)SOAPFactory.newInstance();
			for (Object obj : elements)
			{
				if (obj instanceof Element)
				{
					SOAPElement child = factory.createElement((Element)obj);
					soapElement.addChildElement(child);
				}
				else if (obj instanceof String)
				{
					Element el = DOMUtils.parse((String)obj);
					SOAPElement child = factory.createElement(el);
					soapElement.addChildElement(child);
				}
				else
				{
					throw new AddressingException("Unsupported element: " + obj.getClass().getName());
				}
			}
		}
		catch (RuntimeException rte)
		{
			throw rte;
		}
		catch (Exception ex)
		{
			throw new AddressingException("Cannot append elements", ex);
		}
	}

	private String getPrefixedName(QName qname)
	{
		String prefix = qname.getPrefix();
		String localPart = qname.getLocalPart();
		String qualname = (prefix != null && prefix.length() > 0 ? prefix + ":" + localPart : localPart);
		return qualname;
	}
}
