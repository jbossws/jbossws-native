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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

// $Id$

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.ws.core.jaxws.handler.GenericLogicalHandler;

public class LogicalJAXBHandler extends GenericLogicalHandler
{
   @Override
   public boolean handleOutbound(MessageContext msgContext)
   {
      return appendHandlerName(msgContext);
   }

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      return appendHandlerName(msgContext);
   }

   public boolean appendHandlerName(MessageContext msgContext)
   {
      try
      {
         // Get the payload as Source
         LogicalMessageContext logicalContext = (LogicalMessageContext)msgContext;
         JAXBContext jaxb = JAXBContext.newInstance(Echo.class.getPackage().getName());
         Object payload = logicalContext.getMessage().getPayload(jaxb);

         String handlerName = getHandlerName();

         if (payload instanceof Echo)
         {
            Echo echo = (Echo)payload;
            String value = echo.getString1();
            echo.setString1(value + ":" + handlerName);
         }
         else if (payload instanceof EchoResponse)
         {
            EchoResponse echo = (EchoResponse)payload;
            String value = echo.getResult();
            echo.setResult(value + ":" + handlerName);
         }
         else
         {
            throw new WebServiceException("Invalid payload type: " + payload);
         }

         // Set the updated payload
         logicalContext.getMessage().setPayload(payload, jaxb);

         return true;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
