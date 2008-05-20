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
package org.jboss.test.ws.jaxws.jbws1988;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.extensions.security.Constants;
import org.jboss.ws.extensions.security.WSSecurityAPI;
import org.jboss.ws.extensions.security.WSSecurityDispatcher;
import org.jboss.ws.metadata.wsse.Config;
import org.jboss.ws.metadata.wsse.Username;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityOMFactory;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.w3c.dom.Element;

/**
 * Tests of the username token profile message creation/parsing
 *
 * @author alessio.soldano@jboss.com
 * @since 12-Mar-2008
 */
public class MessageTestCase extends JBossWSTest
{
   private String serverConf = "<jboss-ws-security xmlns='http://www.jboss.com/ws-security/config' "
      + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
      + "xsi:schemaLocation='http://www.jboss.com/ws-security/config "
      + "http://www.jboss.com/ws-security/schema/jboss-ws-security_1_0.xsd'>"
      + "<config>"
      + "<requires/>"
      + "</config>"
      + "</jboss-ws-security>";
   
   private String clientConf = "<jboss-ws-security xmlns='http://www.jboss.com/ws-security/config' "
      + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
      + "xsi:schemaLocation='http://www.jboss.com/ws-security/config "
      + "http://www.jboss.com/ws-security/schema/jboss-ws-security_1_0.xsd'>"
      + "<config>"
      + "<username digestPassword='true'/>"
      + "</config>"
      + "</jboss-ws-security>";
   
   private String testMessage = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" 
      + " <env:Header>"
      + "  <tns:someHeader xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'"
      + "    tns:test='hi' xmlns:tns='http://org.jboss.ws/2004'>some header value</tns:someHeader>" 
      + " </env:Header> "
      + " <env:Body wsu:Id='element-9-1205139829909-17908832' xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>"
      + "  <tns:echoString2 xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' xmlns:tns='http://org.jboss.ws/2004' "
      + "   xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>" 
      + "   <string>Hello World!</string>"
      + "  </tns:echoString2>" 
      + "  <tns:echoString xmlns:tns='http://org.jboss.ws/2004'>" 
      + "   <string>Hello World!</string>" 
      + "  </tns:echoString>"
      + " </env:Body>" 
      + "</env:Envelope>";
   
   public void testDecodeMessageWithNonceAndCreated() throws Exception
   {
      String envStr = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      		"<env:Header>" +
      		"<wsse:Security env:mustUnderstand='1' xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd' " +
      		"xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>" +
      		"<wsse:UsernameToken wsu:Id='token-1-1205341951321-19004769'>" +
      		"<wsse:Username>kermit</wsse:Username>" +
      		"<wsse:Password Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordDigest'>IEeuDaP/NTozwiyJHzTgBoCCDjg=</wsse:Password>" +
      		"<wsse:Nonce EncodingType='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary'>gHGIdDEWjX1Ay/LiVd3qJ1ua8VbjXis8CJwNDQh1ySA=</wsse:Nonce>" +
      		"<wsse:Created>CREATED</wsse:Created>" +
      		"</wsse:UsernameToken>" +
      		"</wsse:Security>" +
      		"</env:Header>" +
      		"<env:Body><ns1:echo xmlns:ns1='http://org.jboss.ws/jbws1988'><arg0>Hi!</arg0></ns1:echo></env:Body>" +
      		"</env:Envelope>";

      WSSecurityConfiguration configuration = WSSecurityOMFactory.newInstance().parse(new StringReader(serverConf));
      
      //"2008-03-12T17:12:31.310Z"
      Calendar created = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      
      WSSecurityAPI sec = new WSSecurityDispatcher();
      sec.decodeMessage(configuration, getMessage(created, envStr), null);
      
      created.add(Calendar.MINUTE, -10);
      try
      {
         sec.decodeMessage(configuration, getMessage(created, envStr), null);
         fail();
      }
      catch (Exception e)
      {
         //OK
      }
      finally
      {
         //Reset username/password since they're stored using a ThreadLocal
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         SecurityAdaptor securityAdaptor = spiProvider.getSPI(SecurityAdaptorFactory.class).newSecurityAdapter();
         securityAdaptor.setPrincipal(null);
         securityAdaptor.setCredential(null);
      }
   }
   
   private SOAPMessage getMessage(Calendar created, String envStr) throws Exception
   {
      envStr = envStr.replaceAll("CREATED", SimpleTypeBindings.marshalDateTime(created));
      ByteArrayInputStream inputStream = new ByteArrayInputStream(envStr.getBytes());
      MessageFactory factory = new MessageFactoryImpl();
      return factory.createMessage(null, inputStream);
   }
   
   public void testEncodeMessageWithNonceAndCreated() throws Exception
   {
      WSSecurityConfiguration configuration = WSSecurityOMFactory.newInstance().parse(new StringReader(clientConf));
      ByteArrayInputStream inputStream = new ByteArrayInputStream(testMessage.getBytes());
      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      WSSecurityAPI sec = new WSSecurityDispatcher();
      sec.encodeMessage(configuration, soapMsg, null, "kermit", "therealfrog");
      Element securityEl = (Element)soapMsg.getSOAPHeader().getChildElements(Constants.WSSE_HEADER_QNAME).next();
      Element usernameTokenEl = (Element)DOMUtils.getChildElements(securityEl, new QName(Constants.WSSE_NS, "UsernameToken")).next();
      assertPassword(usernameTokenEl);
      Element nonceEl = (Element)DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Nonce")).next();
      assertNotNull(nonceEl);
      assertNotNull(DOMUtils.getTextContent(nonceEl));
      assertEquals(nonceEl.getAttribute("EncodingType"), Constants.WSS_SOAP_NS+"#Base64Binary");
      Element createdEl = (Element)DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Created")).next();
      assertNotNull(createdEl);
      assertNotNull(DOMUtils.getTextContent(createdEl));
   }
   
   public void testEncodeMessageWithNonce() throws Exception
   {
      WSSecurityConfiguration configuration = WSSecurityOMFactory.newInstance().parse(new StringReader(clientConf));
      ByteArrayInputStream inputStream = new ByteArrayInputStream(testMessage.getBytes());
      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      WSSecurityAPI sec = new WSSecurityDispatcher();
      Username username = new Username(true, true, false);
      Config config = new Config();
      config.setUsername(username);
      sec.encodeMessage(configuration, soapMsg, config, "kermit", "therealfrog");
      Element securityEl = (Element)soapMsg.getSOAPHeader().getChildElements(Constants.WSSE_HEADER_QNAME).next();
      Element usernameTokenEl = (Element)DOMUtils.getChildElements(securityEl, new QName(Constants.WSSE_NS, "UsernameToken")).next();
      assertPassword(usernameTokenEl);
      Element nonceEl = (Element)DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Nonce")).next();
      assertNotNull(nonceEl);
      assertNotNull(DOMUtils.getTextContent(nonceEl));
      assertEquals(nonceEl.getAttribute("EncodingType"), Constants.WSS_SOAP_NS+"#Base64Binary");
      assertFalse(DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Created")).hasNext());
   }
   
   public void testEncodeMessageWithCreated() throws Exception
   {
      WSSecurityConfiguration configuration = WSSecurityOMFactory.newInstance().parse(new StringReader(clientConf));
      ByteArrayInputStream inputStream = new ByteArrayInputStream(testMessage.getBytes());
      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      WSSecurityAPI sec = new WSSecurityDispatcher();
      Username username = new Username(true, false, true);
      Config config = new Config();
      config.setUsername(username);
      sec.encodeMessage(configuration, soapMsg, config, "kermit", "therealfrog");
      Element securityEl = (Element)soapMsg.getSOAPHeader().getChildElements(Constants.WSSE_HEADER_QNAME).next();
      Element usernameTokenEl = (Element)DOMUtils.getChildElements(securityEl, new QName(Constants.WSSE_NS, "UsernameToken")).next();
      assertPassword(usernameTokenEl);
      assertFalse(DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Nonce")).hasNext());
      Element createdEl = (Element)DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Created")).next();
      assertNotNull(createdEl);
      assertNotNull(DOMUtils.getTextContent(createdEl));
   }
   
   private void assertPassword(Element usernameTokenEl) {
      Element passwordEl = (Element)DOMUtils.getChildElements(usernameTokenEl, new QName(Constants.WSSE_NS, "Password")).next();
      assertNotNull(passwordEl);
      assertNotNull(DOMUtils.getTextContent(passwordEl));
      assertEquals(passwordEl.getAttribute("Type"), Constants.WSSE_NS+"#PasswordDigest");
   }
}