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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.extensions.security.exception.InvalidSecurityHeaderException;
import org.jboss.ws.extensions.security.exception.WSSecurityException;
import org.jboss.ws.metadata.wsse.Config;
import org.jboss.ws.metadata.wsse.Encrypt;
import org.jboss.ws.metadata.wsse.RequireEncryption;
import org.jboss.ws.metadata.wsse.RequireSignature;
import org.jboss.ws.metadata.wsse.RequireTimestamp;
import org.jboss.ws.metadata.wsse.Requires;
import org.jboss.ws.metadata.wsse.Sign;
import org.jboss.ws.metadata.wsse.Timestamp;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Element;

public class WSSecurityDispatcher implements WSSecurityAPI
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

   private static CommonSOAPFaultException convertToFault(WSSecurityException e)
   {
      return new CommonSOAPFaultException(e.getFaultCode(), e.getFaultString());
   }

   private static List<OperationDescription<RequireOperation>> buildRequireOperations(Config operationConfig)
   {
      if (operationConfig == null)
         return null;
      
      Requires requires = operationConfig.getRequires();
      if (requires == null)
         return null;

      ArrayList<OperationDescription<RequireOperation>> operations = new ArrayList<OperationDescription<RequireOperation>>();
      RequireTimestamp requireTimestamp = requires.getRequireTimestamp();
      if (requireTimestamp != null)
         operations.add(new OperationDescription<RequireOperation>(RequireTimestampOperation.class, null, requireTimestamp.getMaxAge(), null, null, null, null));

      RequireSignature requireSignature = requires.getRequireSignature();
      if (requireSignature != null)
      {
         List<Target> targets = convertTargets(requireSignature.getTargets());
         operations.add(new OperationDescription<RequireOperation>(RequireSignatureOperation.class, targets, null, null, null, null, null));
      }

      RequireEncryption requireEncryption = requires.getRequireEncryption();
      if (requireEncryption != null)
      {
         List<Target> targets = convertTargets(requireEncryption.getTargets());
         operations.add(new OperationDescription<RequireOperation>(RequireEncryptionOperation.class, targets, null, null, null, null, null));
      }

      return operations;
   }

   private static Config getActualConfig(WSSecurityConfiguration configuration, Config operationConfig)
   {
      //null operationConfig means default behavior
      return operationConfig != null ? operationConfig : configuration.getDefaultConfig();
   }
   
   private static boolean hasRequirements(Config config)
   {
      return config != null && config.getRequires() != null;
   }
   
   public void decodeMessage(WSSecurityConfiguration configuration, SOAPMessage message, Config operationConfig) throws SOAPException
   {
      Config config = getActualConfig(configuration, operationConfig);
      SOAPHeader soapHeader = message.getSOAPHeader();
      QName secQName = new QName(Constants.WSSE_NS, "Security");
      Element secHeaderElement = (soapHeader != null) ? Util.findElement(soapHeader, secQName) : null; 

      if (secHeaderElement == null)
      {
         // This is ok, we always allow faults to be received because WS-Security does not encrypt faults
         if (message.getSOAPBody().getFault() != null)
            return;

         if (hasRequirements(config))
            throw convertToFault(new InvalidSecurityHeaderException("This service requires <wsse:Security>, which is missing."));

         return;
      }

      try
      {
         SecurityStore securityStore = new SecurityStore(configuration.getKeyStoreURL(), configuration.getKeyStoreType(), configuration.getKeyStorePassword(),
               configuration.getKeyPasswords(), configuration.getTrustStoreURL(), configuration.getTrustStoreType(), configuration.getTrustStorePassword());
         SecurityDecoder decoder = new SecurityDecoder(securityStore);

         decoder.decode(message.getSOAPPart(), secHeaderElement);
         
         if (log.isTraceEnabled())
            log.trace("Decoded Message:\n" + DOMWriter.printNode(message.getSOAPPart(), true));

         List<OperationDescription<RequireOperation>> operations = buildRequireOperations(config);

         decoder.verify(operations);
         if(log.isDebugEnabled()) log.debug("Verification is successful");

         decoder.complete();
      }
      catch (WSSecurityException e)
      {
         if (e.isInternalError())
            log.error("Internal error occured handling inbound message:", e);
         else if(log.isDebugEnabled()) log.debug("Returning error to sender: " + e.getMessage());

         throw convertToFault(e);
      }
      
   }

   public void encodeMessage(WSSecurityConfiguration configuration, SOAPMessage message, Config operationConfig, String user, String password) throws SOAPException
   {
      Config config = getActualConfig(configuration, operationConfig);
      log.debug("WS-Security config: " + config);
      
      // Nothing to process
      if (config == null)
         return;

      ArrayList<OperationDescription<EncodingOperation>> operations = new ArrayList<OperationDescription<EncodingOperation>>();
      Timestamp timestamp = config.getTimestamp();
      if (timestamp != null)
      {
         operations.add(new OperationDescription<EncodingOperation>(TimestampOperation.class, null, null, timestamp.getTtl(), null, null, null));
      }

      if (config.getUsername() != null && user != null && password != null)
      {
         operations.add(new OperationDescription<EncodingOperation>(SendUsernameOperation.class, null, user, password, null, null, null));
      }

      Sign sign = config.getSign();
      if (sign != null)
      {
         List<Target> targets = convertTargets(sign.getTargets());
         if (sign.isIncludeTimestamp())
         {
            if (timestamp == null)
               operations.add(new OperationDescription<EncodingOperation>(TimestampOperation.class, null, null, null, null, null, null));

            if (targets != null && targets.size() > 0)
               targets.add(new WsuIdTarget("timestamp"));
         }

         operations.add(new OperationDescription<EncodingOperation>(SignatureOperation.class, targets, sign.getAlias(), null, null, null, sign.getTokenRefType()));
      }

      Encrypt encrypt = config.getEncrypt();
      if (encrypt != null)
      {
         List<Target> targets = convertTargets(encrypt.getTargets());
         operations.add(new OperationDescription<EncodingOperation>(EncryptionOperation.class, targets, encrypt.getAlias(), null, encrypt.getAlgorithm(), encrypt.getWrap(), encrypt.getTokenRefType()));
      }

      if (operations.size() == 0)
         return;

      if(log.isDebugEnabled()) log.debug("Encoding Message:\n" + DOMWriter.printNode(message.getSOAPPart(), true));

      try
      {
         SecurityStore securityStore = new SecurityStore(configuration.getKeyStoreURL(), configuration.getKeyStoreType(), configuration.getKeyStorePassword(),
               configuration.getKeyPasswords() , configuration.getTrustStoreURL(), configuration.getTrustStoreType(), configuration.getTrustStorePassword());
         SecurityEncoder encoder = new SecurityEncoder(operations, securityStore);
         encoder.encode(message.getSOAPPart());
      }
      catch (WSSecurityException e)
      {
         if (e.isInternalError())
            log.error("Internal error occured handling outbound message:", e);
         else if(log.isDebugEnabled()) log.debug("Returning error to sender: " + e.getMessage());

         throw convertToFault(e);
      }
   }

}
