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
package org.jboss.test.ws.jaxws.wsaddressing.replyto;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;


/**
 * Test endpoint using ws-addressing
 *
 * NOTE: This test uses a JAX-RPC client against a JAX-WS endpoint.
 *  
 * @author Thomas.Diesler@jboss.org
 */
public class ApplicationClient
{
   private static InitialEndpoint initial;
   private static ReplyToEndpoint replyto;
   private static FaultToEndpoint faultto;

   protected static void init(final String serverHost) throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      Service initialService = (Service)iniCtx.lookup("java:comp/env/service/InitialService");
      initial = (InitialEndpoint)initialService.getPort(InitialEndpoint.class);
      Service replytoService = (Service)iniCtx.lookup("java:comp/env/service/ReplyToService");
      replyto = (ReplyToEndpoint)replytoService.getPort(ReplyToEndpoint.class);

      String endpointAddress = "http://" + serverHost + ":8080/jaxws-wsaddressing-faultto/FaultToService";
      QName serviceName = new QName("http://org.jboss.ws/addressing/replyto", "FaultToEndpointService");
      javax.xml.ws.Service service = javax.xml.ws.Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      faultto = (FaultToEndpoint) service.getPort(FaultToEndpoint.class);
   }
   
   public static void main(String[] args) throws Exception
   {
      System.out.println("TEST START");
      final String serverHost = args[0];
      init(serverHost);
      _testReplyToMessage(serverHost);
      _testFaultToMessage(serverHost);
      _testInitial();
      _testReplyTo();
      _testFaultTo();
      System.out.println("TEST END");
   }

   /** This sends a valid message to the ReplyTo endpoint and verfies whether we can read it of again.
    */
   private static void _testReplyToMessage(final String serverHost) throws Exception
   {
      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Header/>" +
         "  <env:Body>" +
         "    <ns1:addItemResponse xmlns:ns1='http://org.jboss.ws/addressing/replyto'>" +
         "      <result>Mars Bar</result>" +
         "    </ns1:addItemResponse>" +
         "  </env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + serverHost + ":8080/jaxws-wsaddressing-replyto/ReplyToService");
      con.call(reqMsg, epURL);
      if (!"Mars Bar".equals(replyto.getLastItem()))
         throw new WebServiceException("APPCLIENT TEST FAILURE");
   }

   /** This sends a fault message to the FaultTo endpoint and verfies whether we can read it of again.
    */
   private static void _testFaultToMessage(final String serverHost) throws Exception
   {
      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Header/>" +
         "  <env:Body>" +
         "    <env:Fault>" +
         "      <faultcode>env:Client</faultcode>" +
         "      <faultstring>java.lang.IllegalArgumentException: Mars Bar</faultstring>" +
         "    </env:Fault>" +
         "  </env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + serverHost + ":8080/jaxws-wsaddressing-faultto/FaultToService");
      con.call(reqMsg, epURL);

      if (!"java.lang.IllegalArgumentException: Mars Bar".equals(faultto.getLastFault()))
         throw new WebServiceException("APPCLIENT TEST FAILURE");
   }

   private static void _testInitial() throws Exception
   {
      String item = initial.addItem("Ice Cream");
      if (item != null)
         throw new WebServiceException("APPCLIENT TEST FAILURE: Expected null, but was: " + item);

      item = initial.addItem("Invalid Value");
      if (item != null)
         throw new WebServiceException("APPCLIENT TEST FAILURE: Expected null, but was: " + item);
   }

   private static void _testReplyTo() throws Exception
   {
      String item = replyto.getLastItem();
      
      if (!"Ice Cream".equals(item))
         throw new WebServiceException("APPCLIENT TEST FAILURE: Expected null, but was: " + item);
   }

   private static void _testFaultTo() throws Exception
   {
      String lastFault = faultto.getLastFault();

      /* JAX-WS 10.2.2.3: the fields of the fault message are populated according to
       * the following rules of precedence:
       *
       * faultstring
       * 1. SOAPFaultException.getFault().getFaultString()
       * 2. Exception.getMessage()
       * 3. Exception.toString()
       *
       * this test used to expect the value returned by toString() */
      if (!"Invalid Value".equals(lastFault))
         throw new WebServiceException("APPCLIENT TEST FAILURE");
   }
}
