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

//$Id$

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.wsse.Config;
import org.jboss.ws.metadata.wsse.Encrypt;
import org.jboss.ws.metadata.wsse.Operation;
import org.jboss.ws.metadata.wsse.Port;
import org.jboss.ws.metadata.wsse.Requires;
import org.jboss.ws.metadata.wsse.Sign;
import org.jboss.ws.metadata.wsse.Timestamp;
import org.jboss.ws.metadata.wsse.Username;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;

/**
 * A Config whose attributes are derived from the specified message context.
 * This is useful to provide the WSSecurityDispatcher with the right config
 * according to the operation/port the current message is related to.
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Mar-2008
 */
public class MessageContextConfigSelector extends Config
{
   private static Logger log = Logger.getLogger(MessageContextConfigSelector.class);
   private CommonMessageContext ctx;
   private WSSecurityConfiguration configuration;
   private Config config;
   private QName opName;
   
   public MessageContextConfigSelector(CommonMessageContext ctx)
   {
      this.ctx = ctx;
      this.configuration = ctx.getEndpointMetaData().getServiceMetaData().getSecurityConfiguration();
      if (configuration == null)
         throw new WSException("Cannot obtain security configuration from message context");
      this.config = new Config(); //empty config, no wsse requirements / processing
   }

   public Encrypt getEncrypt()
   {
      readConfig();
      return config.getEncrypt();
   }

   public Requires getRequires()
   {
      readConfig();
      return config.getRequires();
   }

   public Sign getSign()
   {
      readConfig();
      return config.getSign();
   }

   public Timestamp getTimestamp()
   {
      readConfig();
      return config.getTimestamp();
   }

   public Username getUsername()
   {
      readConfig();
      return config.getUsername();
   }
   
   /**
    * Gets the operation & port the current message is headed to and
    * use them to get the right config to use.
    * 
    */
   private void readConfig()
   {
      //once the operation name is known the specific config
      //is not going to change
      if (opName == null)
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
            opName = opMetaData.getQName();
         
         Config opConfig = getConfig(port, opName);
         log.debug("WS-Security config: " + opConfig);
         if (opConfig != null)
            this.config = opConfig;
      }
   }
   
   private Config getConfig(QName portName, QName opName)
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
   
   public void setEncrypt(Encrypt encrypt)
   {
      throw new UnsupportedOperationException();
   }

   public void setSign(Sign sign)
   {
      throw new UnsupportedOperationException();
   }

   public void setTimestamp(Timestamp timestamp)
   {
      throw new UnsupportedOperationException();
   }

   public void setUsername(Username username)
   {
      throw new UnsupportedOperationException();
   }

   public void setRequires(Requires requires)
   {
      throw new UnsupportedOperationException();
   }
}
