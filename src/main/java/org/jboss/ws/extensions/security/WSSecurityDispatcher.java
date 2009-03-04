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
package org.jboss.ws.extensions.security;

// $Id$

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.wsse.Config;
import org.jboss.ws.metadata.wsse.Encrypt;
import org.jboss.ws.metadata.wsse.Operation;
import org.jboss.ws.metadata.wsse.Port;
import org.jboss.ws.metadata.wsse.RequireEncryption;
import org.jboss.ws.metadata.wsse.RequireSignature;
import org.jboss.ws.metadata.wsse.RequireTimestamp;
import org.jboss.ws.metadata.wsse.Requires;
import org.jboss.ws.metadata.wsse.Sign;
import org.jboss.ws.metadata.wsse.Timestamp;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Element;

public class WSSecurityDispatcher
{
   // provide logging
   private static Logger log = Logger.getLogger(WSSecurityDispatcher.class);

   private static List<Target> convertTargets(List<org.jboss.ws.metadata.wsse.Target> targets)
   {
      if (targets == null)
         return null;

      ArrayList<Target> newList = new ArrayList<Target>(targets.size());

      for (org.jboss.ws.metadata.wsse.Target target : targets)
      {
         if ("qname".equals(target.getType()))
         {
            QNameTarget qnameTarget = new QNameTarget(QName.valueOf(target.getValue()), target.isContentOnly());
            newList.add(qnameTarget);
         }
         else if ("wsuid".equals(target.getType()))
         {
            newList.add(new WsuIdTarget(target.getValue()));
         }
      }

      return newList;
   }

   private static Config getConfig(WSSecurityConfiguration config, String portName, String opName)
   {
      Port port = config.getPorts().get(portName);
      if (port == null)
         return config.getDefaultConfig();

      Operation operation = port.getOperations().get(opName);
      if (operation == null)
      {
         Config portConfig = port.getDefaultConfig();
         return (portConfig == null) ? config.getDefaultConfig() : portConfig;

      }

      return operation.getConfig();
   }

   private static CommonSOAPFaultException convertToFault(WSSecurityException e)
   {
      return new CommonSOAPFaultException(e.getFaultCode(), e.getFaultString());
   }

   public static void handleInbound(CommonMessageContext ctx) throws SOAPException, SOAPFaultException
   {
      WSSecurityConfiguration config = getSecurityConfig(ctx);
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)ctx.getSOAPMessage();
      Config actualConfig = getActualConfig(config, null);

      SOAPHeader soapHeader = soapMessage.getSOAPHeader();
      QName secQName = new QName(Constants.WSSE_NS, "Security");
      Element secHeaderElement = (soapHeader != null) ? Util.findElement(soapHeader, secQName) : null;

      if (secHeaderElement == null)
      {
         // This is ok, we always allow faults to be received because WS-Security does not encrypt faults
         if (soapMessage.getSOAPBody().getFault() != null)
            return;

         OperationMetaData opMetaData = ctx.getOperationMetaData();
         if (opMetaData == null)
         {
            // Get the operation meta data from the soap message
            // for the server side inbound message.
            EndpointMetaData epMetaData = ctx.getEndpointMetaData();
            opMetaData = soapMessage.getOperationMetaData(epMetaData);
         }

         String operation = null;
         String port = null;
         if (opMetaData != null)
         {
            operation = opMetaData.getQName().toString();
            port = opMetaData.getEndpointMetaData().getPortName().getLocalPart();
         }
          

         if (hasRequirements(config, operation, port))
            throw convertToFault(new InvalidSecurityHeaderException("This service requires <wsse:Security>, which is missing."));

         return;
      }

      try
      {
         SecurityStore securityStore = new SecurityStore(config.getKeyStoreURL(), config.getKeyStoreType(), config.getKeyStorePassword(), config.getKeyPasswords(),
               config.getTrustStoreURL(), config.getTrustStoreType(), config.getTrustStorePassword());
         SecurityDecoder decoder = new SecurityDecoder(securityStore, config.getTimestampVerification(), actualConfig == null ? null : actualConfig.getAuthenticate());

         decoder.decode(soapMessage.getSOAPPart(), secHeaderElement);

         if (log.isTraceEnabled())
            log.trace("Decoded Message:\n" + DOMWriter.printNode(soapMessage.getSOAPPart(), true));

         OperationMetaData opMetaData = ctx.getOperationMetaData();
         if (opMetaData == null)
         {
            // Get the operation meta data from the soap message
            // for the server side inbound message.
            EndpointMetaData epMetaData = ctx.getEndpointMetaData();
            opMetaData = soapMessage.getOperationMetaData(epMetaData);
         }

         String operation = null;
         String port = null;
         if (opMetaData != null)
         {
            operation = opMetaData.getQName().toString();
            port = opMetaData.getEndpointMetaData().getPortName().getLocalPart();
         }

         List<OperationDescription<RequireOperation>> operations = buildRequireOperations(config, operation, port);

         decoder.verify(operations);
         if (log.isDebugEnabled())
            log.debug("Verification is successful");

         decoder.complete();
      }
      catch (WSSecurityException e)
      {
         if (e.isInternalError())
            log.error("Internal error occured handling inbound message:", e);
         else if (log.isDebugEnabled())
            log.debug("Returning error to sender: " + e.getMessage());

         throw convertToFault(e);
      }
   }

   private static WSSecurityConfiguration getSecurityConfig(CommonMessageContext ctx)
   {
      WSSecurityConfiguration config = ctx.getEndpointMetaData().getServiceMetaData().getSecurityConfiguration();
      if (config == null)
         throw new WSException("Cannot obtain security configuration from message context");

      return config;
   }

   private static Config getActualConfig(WSSecurityConfiguration configuration, Config operationConfig)
   {
      if (operationConfig == null)
      {
         //if no configuration override, we try getting the right operation config
         //according to the invoked operation that can be found using the context
         CommonMessageContext ctx = MessageContextAssociation.peekMessageContext();
         if (ctx != null)
         {
            EndpointMetaData epMetaData = ctx.getEndpointMetaData();
            QName port = epMetaData.getPortName();

            OperationMetaData opMetaData = ctx.getOperationMetaData();
            if (opMetaData == null)
            {
               // Get the operation meta data from the soap message
               // for the server side inbound message.
               SOAPMessageImpl soapMessage = (SOAPMessageImpl)ctx.getSOAPMessage();
               try
               {
                  opMetaData = soapMessage.getOperationMetaData(epMetaData);
               }
               catch (SOAPException e)
               {
                  throw new WebServiceException("Error while looking for the operation meta data: " + e);
               }
            }
            if (opMetaData != null)
               operationConfig = selectOperationConfig(configuration, port, opMetaData.getQName());
         }
      }
      //null operationConfig means default behavior
      return operationConfig != null ? operationConfig : configuration.getDefaultConfig();
   }

   private static Config selectOperationConfig(WSSecurityConfiguration configuration, QName portName, QName opName)
   {
      Port port = configuration.getPorts().get(portName != null ? portName.getLocalPart() : null);
      if (port == null)
         return configuration.getDefaultConfig();

      Operation operation = port.getOperations().get(opName != null ? opName.toString() : null);
      if (operation == null)
      {
         //if the operation name was not available or didn't match any wsse configured operation,
         //we fall back to the port wsse config (if available) or the default config.
         Config portConfig = port.getDefaultConfig();
         return (portConfig == null) ? configuration.getDefaultConfig() : portConfig;

      }
      return operation.getConfig();
   }

   private static boolean hasRequirements(WSSecurityConfiguration config, String operation, String port)
   {
      Config operationConfig = getConfig(config, port, operation);
      return (operationConfig != null && operationConfig.getRequires() != null);
   }

   private static List<OperationDescription<RequireOperation>> buildRequireOperations(WSSecurityConfiguration config, String operation, String port)
   {
      Config operationConfig = getConfig(config, port, operation);
      if (operationConfig == null)
         return null;

      Requires requires = operationConfig.getRequires();
      if (requires == null)
         return null;

      ArrayList<OperationDescription<RequireOperation>> operations = new ArrayList<OperationDescription<RequireOperation>>();
      RequireTimestamp requireTimestamp = requires.getRequireTimestamp();
      if (requireTimestamp != null)
         operations.add(new OperationDescription<RequireOperation>(RequireTimestampOperation.class, null, requireTimestamp.getMaxAge(), null, null));

      RequireSignature requireSignature = requires.getRequireSignature();
      if (requireSignature != null)
      {
         List<Target> targets = convertTargets(requireSignature.getTargets());
         operations.add(new OperationDescription<RequireOperation>(RequireSignatureOperation.class, targets, null, null, null));
      }

      RequireEncryption requireEncryption = requires.getRequireEncryption();
      if (requireEncryption != null)
      {
         List<Target> targets = convertTargets(requireEncryption.getTargets());
         operations.add(new OperationDescription<RequireOperation>(RequireEncryptionOperation.class, targets, null, null, null));
      }

      return operations;
   }

   public static void handleOutbound(CommonMessageContext ctx) throws SOAPException, SOAPFaultException
   {
      WSSecurityConfiguration config = getSecurityConfig(ctx);
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)ctx.getSOAPMessage();

      EndpointMetaData epMetaData = ctx.getEndpointMetaData();
      String port = epMetaData.getPortName().getLocalPart();
      
      String opName = null;
      OperationMetaData opMetaData = ctx.getOperationMetaData();
      if (opMetaData != null)
         opName = opMetaData.getQName().toString();

      Config opConfig = getConfig(config, port, opName);
      log.debug("WS-Security config: " + opConfig);

      // Nothing to process
      if (opConfig == null)
         return;

      ArrayList<OperationDescription<EncodingOperation>> operations = new ArrayList<OperationDescription<EncodingOperation>>();
      Timestamp timestamp = opConfig.getTimestamp();
      if (timestamp != null)
      {
         operations.add(new OperationDescription<EncodingOperation>(TimestampOperation.class, null, null, timestamp.getTtl(), null));
      }

      if (opConfig.getUsername() != null)
      {
         Object user = ctx.get(Stub.USERNAME_PROPERTY);
         Object pass = ctx.get(Stub.PASSWORD_PROPERTY);

         if (user == null && pass == null)
         {
            user = ctx.get(BindingProvider.USERNAME_PROPERTY);
            pass = ctx.get(BindingProvider.PASSWORD_PROPERTY);
         }

         if (user != null && pass != null)
         {
            operations.add(new OperationDescription<EncodingOperation>(SendUsernameOperation.class, null, user.toString(), pass.toString(), null));
            ctx.put(StubExt.PROPERTY_AUTH_TYPE, StubExt.PROPERTY_AUTH_TYPE_WSSE);
         }
      }

      Sign sign = opConfig.getSign();
      if (sign != null)
      {
         List<Target> targets = convertTargets(sign.getTargets());
         if (sign.isIncludeTimestamp())
         {
            if (timestamp == null)
               operations.add(new OperationDescription<EncodingOperation>(TimestampOperation.class, null, null, null, null));

            if (targets != null && targets.size() > 0)
               targets.add(new WsuIdTarget("timestamp"));
         }

         operations.add(new OperationDescription<EncodingOperation>(SignatureOperation.class, targets, sign.getAlias(), null, null));
      }

      Encrypt encrypt = opConfig.getEncrypt();
      if (encrypt != null)
      {
         List<Target> targets = convertTargets(encrypt.getTargets());
         operations.add(new OperationDescription<EncodingOperation>(EncryptionOperation.class, targets, encrypt.getAlias(), null, encrypt.getAlgorithm()));
      }

      if (operations.size() == 0)
         return;

      if (log.isDebugEnabled())
         log.debug("Encoding Message:\n" + DOMWriter.printNode(soapMessage.getSOAPPart(), true));

      try
      {
         SecurityStore securityStore = new SecurityStore(config.getKeyStoreURL(), config.getKeyStoreType(), config.getKeyStorePassword(), config.getKeyPasswords(),
               config.getTrustStoreURL(), config.getTrustStoreType(), config.getTrustStorePassword());
         SecurityEncoder encoder = new SecurityEncoder(operations, securityStore);
         encoder.encode(soapMessage.getSOAPPart());
      }
      catch (WSSecurityException e)
      {
         if (e.isInternalError())
            log.error("Internal error occured handling outbound message:", e);
         else if (log.isDebugEnabled())
            log.debug("Returning error to sender: " + e.getMessage());

         throw convertToFault(e);
      }
   }
}
