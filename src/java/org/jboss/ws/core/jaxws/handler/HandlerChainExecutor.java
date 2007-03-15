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
package org.jboss.ws.core.jaxws.handler;

// $Id:HandlerChainExecutor.java 710 2006-08-08 20:19:52Z thomas.diesler@jboss.com $

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.jaxws.SOAPFaultHelperJAXWS;
import org.jboss.ws.core.soap.SOAPEnvelopeImpl;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * Executes a list of JAXWS handlers.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public class HandlerChainExecutor
{
   private static Logger log = Logger.getLogger(HandlerChainExecutor.class);

   // The endpoint meta data
   private EndpointMetaData epMetaData;
   // The list of handlers 
   protected List<Handler> handlers = new ArrayList<Handler>();
   // The list of handlers tp close
   protected List<Handler> closeHandlers = new ArrayList<Handler>();
   // The index of the first handler that returned false during processing
   protected int falseIndex = -1;

   public HandlerChainExecutor(EndpointMetaData epMetaData, List<Handler> unsortedChain)
   {
      this.epMetaData = epMetaData;

      // Sort handler logical handlers first
      List<Handler> sortedChain = new ArrayList<Handler>();
      for (Handler handler : unsortedChain)
      {
         if (handler instanceof LogicalHandler)
            sortedChain.add(handler);
      }
      for (Handler handler : unsortedChain)
      {
         if ((handler instanceof LogicalHandler) == false)
            sortedChain.add(handler);
      }

      log.debug("Create a handler executor: " + sortedChain);
      for (Handler handler : sortedChain)
      {
         handlers.add(handler);
      }
   }

   /**
    * Indicates the end of lifecycle for a HandlerChain.
    */
   public void close(MessageContext msgContext)
   {
      log.debug("close");
      int handlerIndex = closeHandlers.size() - 1;
      for (; handlerIndex >= 0; handlerIndex--)
      {
         Handler currHandler = closeHandlers.get(handlerIndex);
         currHandler.close(msgContext);
      }
   }

   public boolean handleRequest(MessageContext msgContext)
   {
      boolean doNext = true;

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleRequest");

         SOAPMessageContextJAXWS soapContext = (SOAPMessageContextJAXWS)msgContext;
         soapContext.setProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);

         int handlerIndex = 0;
         Handler currHandler = null;
         try
         {
            String lastMessageTrace = null;
            for (; doNext && handlerIndex < handlers.size(); handlerIndex++)
            {
               currHandler = handlers.get(handlerIndex);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapContext.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart("BEFORE handleRequest - " + currHandler, soapPart, lastMessageTrace);
               }

               doNext = handleMessage(currHandler, soapContext);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapContext.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart("AFTER handleRequest - " + currHandler, soapPart, lastMessageTrace);
               }
            }
         }
         catch (RuntimeException ex)
         {
            doNext = false;
            processHandlerFailure(ex);
         }
         finally
         {
            // we start at this index in the response chain
            if (doNext == false)
               falseIndex = handlerIndex;

            soapContext.removeProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            log.debug("Exit: handleRequest with status: " + doNext);
         }
      }

      return doNext;
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      boolean doNext = true;

      SOAPMessageContextJAXWS soapContext = (SOAPMessageContextJAXWS)msgContext;
      soapContext.setProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleResponse");

         int handlerIndex = handlers.size() - 1;
         if (falseIndex != -1)
            handlerIndex = falseIndex - 1;

         Handler currHandler = null;
         try
         {
            String lastMessageTrace = null;
            for (; doNext && handlerIndex >= 0; handlerIndex--)
            {
               currHandler = handlers.get(handlerIndex);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapContext.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart("BEFORE handleResponse - " + currHandler, soapPart, lastMessageTrace);
               }

               doNext = handleMessage(currHandler, soapContext);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapContext.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart("AFTER handleResponse - " + currHandler, soapPart, lastMessageTrace);
               }
            }
         }
         catch (RuntimeException ex)
         {
            doNext = false;
            processHandlerFailure(ex);
         }
         finally
         {
            // we start at this index in the fault chain
            if (doNext == false)
               falseIndex = handlerIndex;

            soapContext.removeProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            log.debug("Exit: handleResponse with status: " + doNext);
         }
      }

      return doNext;
   }

   public boolean handleFault(MessageContext msgContext, Exception ex)
   {
      boolean doNext = true;

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleFault");

         SOAPMessageContextJAXWS soapContext = (SOAPMessageContextJAXWS)msgContext;
         soapContext.setProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);
         SOAPMessage soapMessage = soapContext.getMessage();

         // If the message is not already a fault message then it is replaced with a fault message
         try
         {
            if (soapMessage == null || soapMessage.getSOAPBody().getFault() == null)
            {
               soapMessage = SOAPFaultHelperJAXWS.exceptionToFaultMessage(ex);
               soapContext.setMessage(soapMessage);
            }
         }
         catch (SOAPException se)
         {
            throw new WebServiceException("Cannot convert exception to fault message", ex);
         }
         
         int handlerIndex = handlers.size() - 1;
         if (falseIndex != -1)
            handlerIndex = falseIndex - 1;
         
         Handler currHandler = null;
         try
         {
            String lastMessageTrace = null;
            for (; doNext && handlerIndex >= 0; handlerIndex--)
            {
               currHandler = handlers.get(handlerIndex);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapMessage.getSOAPPart();
                  lastMessageTrace = traceSOAPPart("BEFORE handleFault - " + currHandler, soapPart, lastMessageTrace);
               }

               doNext = handleFault(currHandler, soapContext);

               if (log.isTraceEnabled())
               {
                  SOAPPart soapPart = soapMessage.getSOAPPart();
                  lastMessageTrace = traceSOAPPart("AFTER handleFault - " + currHandler, soapPart, lastMessageTrace);
               }
            }
         }
         catch (RuntimeException rte)
         {
            doNext = false;
            processHandlerFailure(rte);
         }
         finally
         {
            // we start at this index in the response chain
            if (doNext == false)
               falseIndex = handlerIndex;

            soapContext.removeProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            log.debug("Exit: handleFault with status: " + doNext);
         }
      }

      return doNext;
   }

   // 4.14 Conformance (Exceptions During Handler Processing): Exceptions thrown during handler processing on
   // the client MUST be passed on to the application. If the exception in question is a subclass of WebService-
   // Exception then an implementation MUST rethrow it as-is, without any additional wrapping, otherwise it
   // MUST throw a WebServiceException whose cause is set to the exception that was thrown during handler processing.
   private void processHandlerFailure(Exception ex)
   {
      log.error("Exception during handler processing", ex);
      if (ex instanceof WebServiceException)
      {
         throw (WebServiceException)ex;
      }
      throw new WebServiceException(ex);
   }

   private boolean handleMessage(Handler currHandler, SOAPMessageContextJAXWS msgContext)
   {
      MessageContext handlerContext = msgContext;
      if (currHandler instanceof LogicalHandler)
      {
         if (epMetaData.getStyle() == Style.RPC)
            throw new WebServiceException("Cannot use logical handler with RPC");

         handlerContext = new LogicalMessageContextImpl(msgContext);
      }

      if (closeHandlers.contains(currHandler) == false)
         closeHandlers.add(currHandler);
      
      
      boolean doNext = false;
      try
      {
         msgContext.setCurrentScope(Scope.HANDLER);
         doNext = currHandler.handleMessage(handlerContext);
      }
      finally
      {
         msgContext.setCurrentScope(Scope.APPLICATION);
      }

      return doNext;
   }

   private boolean handleFault(Handler currHandler, SOAPMessageContextJAXWS msgContext)
   {
      MessageContext handlerContext = msgContext;
      if (currHandler instanceof LogicalHandler)
      {
         if (epMetaData.getStyle() == Style.RPC)
            throw new WebServiceException("Cannot use logical handler with RPC");
         
         handlerContext = new LogicalMessageContextImpl(msgContext);
      }

      if (closeHandlers.contains(currHandler) == false)
         closeHandlers.add(currHandler);
      
      boolean doNext = false;
      try
      {
         msgContext.setCurrentScope(Scope.HANDLER);
         doNext = currHandler.handleFault(handlerContext);
      }
      finally
      {
         msgContext.setCurrentScope(Scope.APPLICATION);
      }

      return doNext;
   }

   /**
    * Trace the SOAPPart, do nothing if the String representation is equal to the last one.
    */
   protected String traceSOAPPart(String logMsg, SOAPPart soapPart, String lastMessageTrace)
   {
      try
      {
         SOAPEnvelopeImpl soapEnv = (SOAPEnvelopeImpl)soapPart.getEnvelope();
         String envString = DOMWriter.printNode(soapEnv, true);
         if (envString.equals(lastMessageTrace))
         {
            log.trace(logMsg + ": unchanged");
         }
         else
         {
            log.trace(logMsg + "\n" + envString);
            lastMessageTrace = envString;
         }
         return lastMessageTrace;
      }
      catch (SOAPException e)
      {
         log.error("Cannot get SOAPEnvelope", e);
         return null;
      }
   }
}
