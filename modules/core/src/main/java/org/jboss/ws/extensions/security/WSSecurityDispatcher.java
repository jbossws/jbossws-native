/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.extensions.security;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.soap.SOAPFaultImpl;
import org.jboss.ws.extensions.security.exception.InvalidSecurityHeaderException;
import org.jboss.ws.extensions.security.exception.WSSecurityException;
import org.jboss.ws.extensions.security.nonce.DefaultNonceFactory;
import org.jboss.ws.extensions.security.nonce.NonceFactory;
import org.jboss.ws.extensions.security.operation.AuthorizeOperation;
import org.jboss.ws.extensions.security.operation.EncodingOperation;
import org.jboss.ws.extensions.security.operation.EncryptionOperation;
import org.jboss.ws.extensions.security.operation.RequireEncryptionOperation;
import org.jboss.ws.extensions.security.operation.RequireOperation;
import org.jboss.ws.extensions.security.operation.RequireSignatureOperation;
import org.jboss.ws.extensions.security.operation.RequireTimestampOperation;
import org.jboss.ws.extensions.security.operation.SendUsernameOperation;
import org.jboss.ws.extensions.security.operation.SignatureOperation;
import org.jboss.ws.extensions.security.operation.TimestampOperation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.wsse.Authenticate;
import org.jboss.ws.metadata.wsse.Authorize;
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
import org.jboss.ws.metadata.wsse.Username;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.w3c.dom.Element;

public class WSSecurityDispatcher implements WSSecurityAPI
{
   // provide logging
   private static Logger log = Logger.getLogger(WSSecurityDispatcher.class);
   
   private static boolean VERBOSE_EXCEPTION_REPORTING = Boolean.getBoolean("org.jboss.ws.native.security.verbose_exception_reporting");

   public void decodeMessage(WSSecurityConfiguration configuration, SOAPMessage message, Config operationConfig) throws SOAPException
   {
      Config config = getActualConfig(configuration, operationConfig);
      SOAPHeader soapHeader = message.getSOAPHeader();
      QName secQName = new QName(Constants.WSSE_NS, "Security");
      Element secHeaderElement = (soapHeader != null) ? Util.findElement(soapHeader, secQName) : null;

      boolean fault = message.getSOAPBody().getFault() != null;
      if (secHeaderElement == null)
      {
         if (hasRequirements(config, fault))
            throw convertToFault(new InvalidSecurityHeaderException("This service requires <wsse:Security>, which is missing."), true);
      }

      try
      {
         if (secHeaderElement != null)
         {
            decodeHeader(configuration, config, message, secHeaderElement, fault);
         }

         authorize(config);
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

   private void decodeHeader(WSSecurityConfiguration configuration, Config config, SOAPMessage message, Element secHeaderElement, boolean fault) throws WSSecurityException
   {
      SecurityStore securityStore = new SecurityStore(configuration);
      NonceFactory factory = Util.loadFactory(NonceFactory.class, configuration.getNonceFactory(), DefaultNonceFactory.class);

      Authenticate authenticate = null;

      if (config != null)
      {
         authenticate = config.getAuthenticate();
      }

      SecurityDecoder decoder = new SecurityDecoder(securityStore, factory, configuration.getTimestampVerification(), authenticate);
      
      List<RequireOperation> operations = buildRequireOperations(config, fault);
      
      decoder.init(operations);

      decoder.decode(message.getSOAPPart(), secHeaderElement);

      if (log.isTraceEnabled())
         log.trace("Decoded Message:\n" + DOMWriter.printNode(message.getSOAPPart(), true));

      decoder.verify(operations);
      if (log.isDebugEnabled())
         log.debug("Verification is successful");

      decoder.complete();
   }

   private void authorize(Config config) throws WSSecurityException
   {
      if (config != null)
      {
         Authorize authorize = config.getAuthorize();
         if (authorize != null)
         {
            AuthorizeOperation authorizeOp = new AuthorizeOperation(authorize);
            authorizeOp.process();
         }
      }
   }

   public void encodeMessage(WSSecurityConfiguration configuration, SOAPMessage message, Config operationConfig, String user, String password) throws SOAPException
   {
      Config config = getActualConfig(configuration, operationConfig);
      log.debug("WS-Security config: " + config);

      boolean fault = message.getSOAPBody().getFault() != null;
      // Nothing to process
      if (config == null || (fault && !config.includesFaults()))
         return;

      ArrayList<EncodingOperation> operations = new ArrayList<EncodingOperation>();
      Timestamp timestamp = config.getTimestamp();
      if (timestamp != null)
      {
         operations.add(new TimestampOperation(timestamp.getTtl()));
      }

      Username username = config.getUsername();
      if (username != null && user != null && password != null)
      {
         NonceFactory factory = Util.loadFactory(NonceFactory.class, configuration.getNonceFactory(), DefaultNonceFactory.class);
         operations.add(new SendUsernameOperation(user, password, username.isDigestPassword(), username.isUseNonce(), username.isUseCreated(), factory.getGenerator()));
      }

      Sign sign = config.getSign();
      if (sign != null && (!fault || sign.isIncludeFaults()))
      {
         List<Target> targets = convertTargets(sign.getTargets());
         if (sign.isIncludeTimestamp())
         {
            if (timestamp == null)
               operations.add(new TimestampOperation(null));

            if (targets != null && targets.size() > 0)
               targets.add(new WsuIdTarget("timestamp"));
         }

         operations.add(new SignatureOperation(targets, sign.getAlias(), sign.getTokenRefType(), sign.getSecurityDomainAliasLabel()));
      }

      Encrypt encrypt = config.getEncrypt();
      if (encrypt != null && (!fault || encrypt.isIncludeFaults()))
      {
         List<Target> targets = convertTargets(encrypt.getTargets());
         operations.add(new EncryptionOperation(targets, encrypt.getAlias(), encrypt.getAlgorithm(), encrypt.getWrap(), encrypt.getTokenRefType(), encrypt
               .getSecurityDomainAliasLabel()));
      }

      if (operations.size() == 0)
         return;

      if (log.isDebugEnabled())
         log.debug("Encoding Message:\n" + DOMWriter.printNode(message.getSOAPPart(), true));

      try
      {
         SecurityStore securityStore = new SecurityStore(configuration);
         SecurityEncoder encoder = new SecurityEncoder(operations, securityStore);
         
         if ((sign != null || encrypt != null) && message instanceof SOAPMessageImpl)
         {
            ((SOAPMessageImpl)message).prepareForDOMAccess();
         }
         encoder.encode(message.getSOAPPart());
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

   public void cleanup()
   {
      //Reset username/password since they're stored using a ThreadLocal
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      SecurityAdaptor securityAdaptor = spiProvider.getSPI(SecurityAdaptorFactory.class).newSecurityAdapter();
      securityAdaptor.setPrincipal(null);
      securityAdaptor.setCredential(null);
   }

   private List<Target> convertTargets(List<org.jboss.ws.metadata.wsse.Target> targets)
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

   private RuntimeException convertToFault(WSSecurityException e) throws SOAPException
   {
      return convertToFault(e, VERBOSE_EXCEPTION_REPORTING);
   }
   
   private RuntimeException convertToFault(WSSecurityException e, boolean verbose) throws SOAPException
   {
      //Try to reduce redundant stack trace elements printed to log
      chopStackTrace(e);
      log.error("Original WSSecurityException: ", e);

      if(isSOAP12())
      {
         SOAPFaultImpl fault = new SOAPFaultImpl(
            org.jboss.ws.Constants.PREFIX_ENV, 
            org.jboss.ws.Constants.NS_SOAP12_ENV
         );

         if(e.isInternalError())
            fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
         else
            fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);

         fault.appendFaultSubcode(e.getFaultCode());
         fault.setFaultString(e.getFaultString());

         return new SOAPFaultException(fault);
      }
      else
      {
         if (verbose) 
         {
            return new CommonSOAPFaultException(e.getFaultCode(), e.getFaultString());
         } 
         else 
         {
            QName faultCode = new QName(Constants.JBOSS_WSSE_NS, "GenericError", Constants.JBOSS_WSSE_PREFIX);
            return new CommonSOAPFaultException(faultCode, "A WS-Security error occurred.");
         }
      }
   }

   private void chopStackTrace(Exception e)
   {
      StackTraceElement[] original = e.getStackTrace();
      int cutOffElement = 0;
      for(; cutOffElement < original.length; cutOffElement++)
      {
         StackTraceElement elem = original[cutOffElement];
         String className = elem.getClassName();
         if("javax.servlet.http.HttpServlet".equals(className))
         {
            StackTraceElement[] newSte = new StackTraceElement[cutOffElement + 1];
            System.arraycopy(original, 0, newSte, 0, cutOffElement + 1);
            e.setStackTrace(newSte);
         }
      }
   }

   private List<RequireOperation> buildRequireOperations(Config operationConfig, boolean fault)
   {
      if (operationConfig == null)
         return null;

      Requires requires = operationConfig.getRequires();
      if (requires == null)
         return null;

      ArrayList<RequireOperation> operations = new ArrayList<RequireOperation>();
      RequireTimestamp requireTimestamp = requires.getRequireTimestamp();
      if (requireTimestamp != null)
         operations.add(new RequireTimestampOperation(requireTimestamp.getMaxAge()));

      RequireSignature requireSignature = requires.getRequireSignature();
      if (requireSignature != null && (!fault || requireSignature.isIncludeFaults()))
      {
         List<Target> targets = convertTargets(requireSignature.getTargets());
         operations.add(new RequireSignatureOperation(targets));
      }

      RequireEncryption requireEncryption = requires.getRequireEncryption();
      if (requireEncryption != null && (!fault || requireEncryption.isIncludeFaults()))
      {
         List<Target> targets = convertTargets(requireEncryption.getTargets());
         operations.add(new RequireEncryptionOperation(targets, requireEncryption.getdKeyWrapAlgorithms(), requireEncryption.getAlgorithms()));
      }

      return operations;
   }

   private Config getActualConfig(WSSecurityConfiguration configuration, Config operationConfig)
   {
      if (operationConfig == null && hasSubConfigs(configuration))
      {
         //if no configuration override and the provided configuration has port /
         //operation configs, we try getting the right operation config
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
            {
               operationConfig = selectOperationConfig(configuration, port, opMetaData.getQName());
            }
            else
            {
               //No operation metadata matched, meaning we don't know what operationConfig to use.
               //This is to be solved either by removing useless operation configs or by requiring
               //WS-Addressing to be used for telling the server which operation is being invoked
               throw new WebServiceException("Could not determine the operation configuration to be used for processing the request");
            }
         }
      }
      //null operationConfig means default behavior
      return operationConfig != null ? operationConfig : configuration.getDefaultConfig();
   }

   private boolean hasSubConfigs(WSSecurityConfiguration configuration)
   {
      return !configuration.getPorts().isEmpty();
   }

   private Config selectOperationConfig(WSSecurityConfiguration configuration, QName portName, QName opName)
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

   private boolean hasRequirements(Config config, boolean fault)
   {
      Requires requires = (config != null) ? config.getRequires() : null;
      return requires != null && (!fault || requires.includesFaults());

   }

   private static boolean isSOAP12()
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      if (msgContext != null)
      {
         EndpointMetaData emd = msgContext.getEndpointMetaData();
         String bindingId = emd.getBindingId();
         if (SOAPBinding.SOAP12HTTP_BINDING.equals(bindingId) || SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingId))
         {
            return true;
         }
      }

      return false;
   }
}
