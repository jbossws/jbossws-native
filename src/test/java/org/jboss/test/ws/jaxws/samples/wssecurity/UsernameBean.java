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
package org.jboss.test.ws.jaxws.samples.wssecurity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.ws.extensions.security.Constants;
import org.jboss.ws.extensions.security.Util;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPEnvelopeImpl;
import org.jboss.ws.core.soap.SOAPHeaderImpl;
import org.w3c.dom.Element;

/**
 * The SEI implementation used by the SimpleUsernameTestCase
 *
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 * @since 15-Aug-2007
 * @version $Revision$ 
 */
@WebService(name = "UsernameEndpoint", serviceName = "UsernameService", targetNamespace = "http://org.jboss.ws/samples/wssecurity")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class UsernameBean
{
   private Logger log = Logger.getLogger(UsernameBean.class);
   
   @Resource
   WebServiceContext wsCtx;

   @WebMethod
   public String getUsernameToken()
   {
      String retObj = "";
      try
      {       
         MessageContext jaxwsContext = (MessageContext)wsCtx.getMessageContext();
         SOAPMessage soapMessage = ((SOAPMessageContext)jaxwsContext).getMessage();
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         soapMessage.writeTo(stream);
         log.info(stream.toString());
         
         SOAPPart soapPart = soapMessage.getSOAPPart();
         SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
         StringWriter strw = new StringWriter();
         ((SOAPEnvelopeImpl)soapEnvelope).writeElement(strw);
         log.info(strw.toString());

         SOAPHeader soapHeader = soapEnvelope.getHeader();
         strw = new StringWriter();
         ((SOAPHeaderImpl)soapHeader).writeElement(strw);
         retObj = strw.toString();
         log.info(retObj);

         QName secQName = new QName(Constants.WSSE_NS, "Security");
         Element secHeaderElement = Util.findElement(soapHeader, secQName);
         log.info(secHeaderElement);
      }
      catch (SOAPException se)
      {
         log.error(se.getMessage());
      }
      catch (IOException ioe)
      {
         log.error(ioe.getMessage());
      }
      return retObj;
   }
}
