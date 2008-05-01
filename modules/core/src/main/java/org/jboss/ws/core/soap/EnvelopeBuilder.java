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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;


/**
 * @author Heiko Braun, <heiko.braun@jboss.com>
 * @author Thomas.Diesler@jboss.com
 * @since 19-Apr-2006
 */
public interface EnvelopeBuilder
{
   Style getStyle();
   
   void setStyle(Style style);
   
   SOAPEnvelope build(SOAPMessage soapMessage, InputStream in, boolean ignoreParseError) throws IOException, SOAPException;
   
   SOAPEnvelope build(SOAPMessage soapMessage, Reader reader, boolean ignoreParseError) throws IOException, SOAPException;
   
   SOAPEnvelope build(SOAPMessage soapMessage, Element domEnv) throws SOAPException;
   
   SOAPBodyElement buildBodyElementDoc(SOAPBodyImpl soapBody, Element domBodyElement) throws SOAPException;

   SOAPBodyElement buildBodyElementRpc(SOAPBodyImpl soapBody, Element domBodyElement) throws SOAPException;
}
