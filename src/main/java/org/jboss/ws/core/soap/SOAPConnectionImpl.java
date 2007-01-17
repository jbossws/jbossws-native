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

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.rpc.Stub;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.EndpointReference;

import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.marshal.MarshalFactory;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.WSTimeoutException;
import org.jboss.ws.extensions.xop.XOPContext;

/**
 * SOAPConnection implementation
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 *
 * @since 02-Feb-2005
 */
public class SOAPConnectionImpl extends SOAPConnection
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPConnectionImpl.class);
   private static Logger msgLog = Logger.getLogger("jbossws.SOAPMessage");

   private Map<String, Object> config = new HashMap<String, Object>();

   private static Map<String, String> metadataMap = new HashMap<String, String>();
   static
   {
      metadataMap.put(Stub.USERNAME_PROPERTY, "http.basic.username");
      metadataMap.put(Stub.PASSWORD_PROPERTY, "http.basic.password");
      metadataMap.put(BindingProvider.USERNAME_PROPERTY, "http.basic.username");
      metadataMap.put(BindingProvider.PASSWORD_PROPERTY, "http.basic.password");
   }
   private static Map<String, String> configMap = new HashMap<String, String>();
   static
   {
      // Remoting constants since 2.0.0.GA
      //configMap.put(StubExt.PROPERTY_KEY_STORE, SSLSocketBuilder.REMOTING_KEY_STORE_FILE_PATH);
      //configMap.put(StubExt.PROPERTY_KEY_STORE_PASSWORD, SSLSocketBuilder.REMOTING_KEY_STORE_PASSWORD);
      //configMap.put(StubExt.PROPERTY_KEY_STORE_TYPE, SSLSocketBuilder.REMOTING_KEY_STORE_TYPE);
      //configMap.put(StubExt.PROPERTY_TRUST_STORE, SSLSocketBuilder.REMOTING_TRUST_STORE_FILE_PATH);
      //configMap.put(StubExt.PROPERTY_TRUST_STORE_PASSWORD, SSLSocketBuilder.REMOTING_TRUST_STORE_PASSWORD);
      //configMap.put(StubExt.PROPERTY_TRUST_STORE_TYPE, SSLSocketBuilder.REMOTING_TRUST_STORE_TYPE);

      configMap.put(StubExt.PROPERTY_KEY_STORE, "org.jboss.remoting.keyStore");
      configMap.put(StubExt.PROPERTY_KEY_STORE_PASSWORD, "org.jboss.remoting.keyStorePassword");
      configMap.put(StubExt.PROPERTY_KEY_STORE_TYPE, "org.jboss.remoting.keyStoreType");
      configMap.put(StubExt.PROPERTY_TRUST_STORE, "org.jboss.remoting.trustStore");
      configMap.put(StubExt.PROPERTY_TRUST_STORE_PASSWORD, "org.jboss.remoting.trustStorePassword");
      configMap.put(StubExt.PROPERTY_TRUST_STORE_TYPE, "org.jboss.remoting.trustStoreType");
   }

   private boolean closed;

   public SOAPConnectionImpl()
   {
      // HTTPClientInvoker conect sends gratuitous POST
      // http://jira.jboss.com/jira/browse/JBWS-711
      config.put(Client.ENABLE_LEASE, false);
   }

   /**
    * Sends the given message to the specified endpoint and blocks until it has
    * returned the response.
    */
   public SOAPMessage call(SOAPMessage reqMessage, Object endpoint) throws SOAPException
   {
      return callInternal(reqMessage, endpoint, false);
   }

   /**
    * Sends the given message to the specified endpoint. This method is logically
    * non blocking.
    */
   public SOAPMessage callOneWay(SOAPMessage reqMessage, Object endpoint) throws SOAPException
   {
      return callInternal(reqMessage, endpoint, true);
   }

   /** Sends the given message to the specified endpoint. */
   private SOAPMessage callInternal(SOAPMessage reqMessage, Object endpoint, boolean oneway) throws SOAPException
   {
      if (reqMessage == null)
         throw new IllegalArgumentException("Given SOAPMessage cannot be null");
      if (endpoint == null)
         throw new IllegalArgumentException("Given endpoint cannot be null");

      if (closed)
         throw new SOAPException("SOAPConnection is already closed");

      Object timeout = null;
      String targetAddress;
      Map callProps;

      if (endpoint instanceof EndpointInfo)
      {
         EndpointInfo epInfo = (EndpointInfo)endpoint;
         targetAddress = epInfo.getTargetAddress();
         callProps = epInfo.getProperties();

         if (callProps.containsKey(StubExt.PROPERTY_CLIENT_TIMEOUT))
         {
            timeout = callProps.get(StubExt.PROPERTY_CLIENT_TIMEOUT);
            targetAddress = addURLParameter(targetAddress, "timeout", timeout.toString());
         }
      }
      else if (endpoint instanceof EndpointReference)
      {
         EndpointReference epr = (EndpointReference)endpoint;
         targetAddress = epr.getAddress().toString();
         callProps = null;
      }
      else
      {
         targetAddress = endpoint.toString();
         callProps = null;
      }

      // enforce xop transitions
      // TODO: there should be a clear transition to an immutable object model
      XOPContext.eagerlyCreateAttachments();

      // save object model changes
      if (reqMessage.saveRequired())
         reqMessage.saveChanges();

      // setup remoting client
      Map metadata = createRemotingMetaData(reqMessage, callProps);
      Client client = createRemotingClient(endpoint, targetAddress, oneway);

      try
      {
         // debug the outgoing message
         if (msgLog.isTraceEnabled())
         {
            SOAPEnvelope soapReqEnv = reqMessage.getSOAPPart().getEnvelope();
            String envStr = SOAPElementWriter.writeElement((SOAPElementImpl)soapReqEnv, true);
            msgLog.trace("Remoting meta data: " + metadata);
            msgLog.trace("Outgoing SOAPMessage\n" + envStr);
         }

         SOAPMessage resMessage = null;
         try
         {
            if (oneway == true)
            {
               client.invokeOneway(reqMessage, metadata, false);
            }
            else
            {
               resMessage = (SOAPMessage)client.invoke(reqMessage, metadata);
            }
         }
         catch (RuntimeException rte)
         {
            if (timeout != null && rte.getCause() instanceof SocketTimeoutException)
               throw new WSTimeoutException("Timeout after: " + timeout + "ms", new Long(timeout.toString()));
            else throw rte;
         }

         // Disconnect the remoting client
         client.disconnect();

         // debug the incomming response message
         if (resMessage != null && msgLog.isTraceEnabled())
         {
            SOAPEnvelope soapResEnv = resMessage.getSOAPPart().getEnvelope();
            String envStr = SOAPElementWriter.writeElement((SOAPElementImpl)soapResEnv, true);
            msgLog.trace("Incoming Response SOAPMessage\n" + envStr);
         }

         return resMessage;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Throwable t)
      {
         throw new SOAPException("Could not transmit message", t);
      }
   }

   private String addURLParameter(String url, String key, String value)
   {
      int qmIndex = url.indexOf("?");
      url += (qmIndex < 0 ? "?" : "&") + key + "=" + value;
      return url;
   }

   /** Closes this SOAPConnection
    */
   public void close() throws SOAPException
   {
      if (closed)
         throw new SOAPException("SOAPConnection is already closed");

      closed = true;
   }

   private Client createRemotingClient(Object endpoint, String targetAddress, boolean oneway) throws SOAPException
   {
      Client client;
      try
      {
         // Get the invoker from Remoting for a given endpoint address
         log.debug("Get locator for: " + endpoint);
         targetAddress = addURLParameter(targetAddress, InvokerLocator.DATATYPE, "SOAPMessage");
         InvokerLocator locator = new InvokerLocator(targetAddress);

         /* An HTTPClientInvoker may disconnect from the server and recreated by the remoting layer.
          * In that case the new invoker does not inherit the marshaller/unmarshaller from the disconnected invoker.
          * We therefore explicitly specify the invoker locator datatype and register the SOAP marshaller/unmarshaller
          * with the MarshalFactory. 
          * 
          * This applies to remoting-1.4.5 
          */
         SOAPMessageMarshaller marshaller = new SOAPMessageMarshaller();
         SOAPMessageUnMarshaller unmarshaller = new SOAPMessageUnMarshaller();
         MarshalFactory.addMarshaller("SOAPMessage", marshaller, unmarshaller);

         client = new Client(locator, "saaj", config);
         client.connect();

         client.setMarshaller(marshaller);

         if (oneway == false)
            client.setUnMarshaller(unmarshaller);
      }
      catch (MalformedURLException e)
      {
         throw new SOAPException("Malformed endpoint address", e);
      }
      catch (Exception e)
      {
         throw new SOAPException("Could not setup remoting client", e);
      }
      return client;
   }

   private Map createRemotingMetaData(SOAPMessage reqMessage, Map callProps) throws SOAPException
   {
      // R2744 A HTTP request MESSAGE MUST contain a SOAPAction HTTP header field
      // with a quoted value equal to the value of the soapAction attribute of
      // soapbind:operation, if present in the corresponding WSDL description.

      // R2745 A HTTP request MESSAGE MUST contain a SOAPAction HTTP header field
      // with a quoted empty string value, if in the corresponding WSDL description,
      // the soapAction attribute of soapbind:operation is either not present, or
      // present with an empty string as its value.

      MimeHeaders mimeHeaders = reqMessage.getMimeHeaders();
      String[] action = mimeHeaders.getHeader("SOAPAction");
      if (action != null && action.length > 0)
      {
         String soapAction = action[0];

         // R1109 The value of the SOAPAction HTTP header field in a HTTP request MESSAGE MUST be a quoted string.
         if (soapAction.startsWith("\"") == false || soapAction.endsWith("\"") == false)
            soapAction = "\"" + soapAction + "\"";

         mimeHeaders.setHeader("SOAPAction", soapAction);
      }
      else
      {
         mimeHeaders.setHeader("SOAPAction", "\"\"");
      }

      Map<String, Object> metadata = new HashMap<String, Object>();

      // We need to unmarshall faults (HTTP 500)
      // metadata.put(HTTPMetadataConstants.NO_THROW_ON_ERROR, "true"); // since 2.0.0.GA
      metadata.put("NoThrowOnError", "true");

      Properties props = new Properties();
      metadata.put("HEADER", props);

      Iterator i = mimeHeaders.getAllHeaders();
      while (i.hasNext())
      {
         MimeHeader header = (MimeHeader)i.next();
         String currentValue = props.getProperty(header.getName());

         /*
          * Coalesce multiple headers into one
          *
          * From HTTP/1.1 RFC 2616:
          *
          * Multiple message-header fields with the same field-name MAY be
          * present in a message if and only if the entire field-value for that
          * header field is defined as a comma-separated list [i.e., #(values)].
          * It MUST be possible to combine the multiple header fields into one
          * "field-name: field-value" pair, without changing the semantics of
          * the message, by appending each subsequent field-value to the first,
          * each separated by a comma.
          */
         if (currentValue != null)
         {
            props.put(header.getName(), currentValue + "," + header.getValue());
         }
         else
         {
            props.put(header.getName(), header.getValue());
         }
      }

      if (callProps != null)
      {
         Iterator it = callProps.entrySet().iterator();

         // Get authentication type, default to BASIC authetication
         String authType = (String)callProps.get(StubExt.PROPERTY_AUTH_TYPE);
         if (authType == null)
            authType = StubExt.PROPERTY_AUTH_TYPE_BASIC;

         while (it.hasNext())
         {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            Object val = entry.getValue();

            // pass properties to remoting meta data
            if (metadataMap.containsKey(key))
            {
               String remotingKey = metadataMap.get(key);
               if ("http.basic.username".equals(remotingKey) || "http.basic.password".equals(remotingKey))
               {
                  if (authType.equals(StubExt.PROPERTY_AUTH_TYPE_BASIC))
                  {
                     metadata.put(remotingKey, val);
                  }
                  else
                  {
                     log.warn("Ignore '" + key + "' with auth typy: " + authType);
                  }
               }
               else
               {
                  metadata.put(remotingKey, val);
               }
            }

            // pass properties to remoting client config
            if (configMap.containsKey(key))
            {
               String remotingKey = configMap.get(key);
               config.put(remotingKey, val);
            }
         }
      }

      return metadata;
   }
}
