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
package org.jboss.ws.extensions.security.jaxws;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.server.UnifiedVirtualFile;
import org.jboss.ws.extensions.security.WSSecurityDispatcher;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfigFactory;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;

/**
 * An abstract JAXWS handler that delegates to the WSSecurityDispatcher
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Nov-2005
 */
public abstract class WSSecurityHandler extends GenericSOAPHandler
{
   // provide logging
   private static Logger log = Logger.getLogger(WSSecurityHandler.class);

   protected boolean handleInboundSecurity(MessageContext msgContext)
   {
      try
      {
         if (getSecurityConfiguration(msgContext) != null)
         {
            WSSecurityDispatcher.handleInbound((CommonMessageContext)msgContext);
         }
      }
      catch (SOAPException ex)
      {
         log.error("Cannot handle inbound ws-security", ex);
         return false;
      }
      return true;
   }

   protected boolean handleOutboundSecurity(MessageContext msgContext)
   {
      try
      {
         if (getSecurityConfiguration(msgContext) != null)
         {
            WSSecurityDispatcher.handleOutbound((CommonMessageContext)msgContext);
         }
      }
      catch (SOAPException ex)
      {
         log.error("Cannot handle outbound ws-security", ex);
         return false;
      }
      return true;
   }

   /**
    * Load security config from vfsRoot
    * @param msgContext
    */
   private WSSecurityConfiguration getSecurityConfiguration(MessageContext msgContext)
   {
      EndpointMetaData epMetaData = ((CommonMessageContext)msgContext).getEndpointMetaData();
      ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();

      if(null == serviceMetaData.getSecurityConfiguration()) // might be set through ServiceObjectFactory
      {
         UnifiedVirtualFile vfsRoot = serviceMetaData.getUnifiedMetaData().getRootFile();
         boolean successful = false;
         WSSecurityConfiguration config = null;
         try
         {
            WSSecurityConfigFactory wsseConfFactory = WSSecurityConfigFactory.newInstance();
            config = wsseConfFactory.createConfiguration(vfsRoot, getConfigResourceName());
            if(config!=null) successful = true;

         }
         catch (IOException e)
         {
            successful = false;
         }

         if(!successful)
            throw new WSException("Failed to access security config");

         // it's required further down the processing chain
         // TODO: cleanup
         serviceMetaData.setSecurityConfiguration(config);

      }

      return serviceMetaData.getSecurityConfiguration();
   }

   protected abstract String getConfigResourceName();
}
