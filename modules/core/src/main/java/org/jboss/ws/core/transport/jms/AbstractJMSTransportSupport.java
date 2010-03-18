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
package org.jboss.ws.core.transport.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.soap.SOAPException;

import org.hornetq.jms.client.HornetQDestination;
import org.jboss.logging.Logger;
import org.jboss.util.NestedRuntimeException;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Endpoint.EndpointState;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;

/**
 * The abstract base class for MDBs that want to act as web service endpoints.
 * A subclass should only need to implement the service endpoint interface.
 *
 * @author Thomas.Diesler@jboss.org
 */
public abstract class AbstractJMSTransportSupport implements MessageListener
{
   // logging support
   protected static Logger log = Logger.getLogger(AbstractJMSTransportSupport.class);

   private QueueConnectionFactory conFactory;
   
   /**
    * All messages come in here, if it is a BytesMessage we pass it on for further processing.
    */
   public void onMessage(Message message)
   {
      try
      {
         String msgStr = null;
         if (message instanceof BytesMessage)
         {
            msgStr = getMessageStr((BytesMessage)message);
         }
         else if (message instanceof TextMessage)
         {
            msgStr = ((TextMessage)message).getText();
         }
         else
         {
            log.warn("Invalid message type: " + message);
            return;
         }

         if (log.isDebugEnabled())
            log.debug("Incomming SOAP message: " + msgStr);

         String fromName = null;
         Destination destination = message.getJMSDestination();
         if (destination instanceof HornetQDestination)
            fromName = getFromName(destination, ((HornetQDestination)destination).isQueue());
         else if (destination instanceof Queue)
            fromName = getFromName(destination, true);
         else if (destination instanceof Topic)
            fromName = getFromName(destination, false);

         InputStream inputStream = new ByteArrayInputStream(msgStr.getBytes());
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
         processSOAPMessage(fromName, inputStream, outputStream);

         msgStr = new String(outputStream.toByteArray());
         if (log.isDebugEnabled())
            log.debug("Outgoing SOAP message: " + msgStr);

         if (msgStr.length() > 0)
         {
            Queue replyQueue = getReplyQueue(message);
            if (replyQueue != null)
            {
               sendResponse(replyQueue, msgStr);
            }
            else
            {
               log.warn("No reply queue, ignore response message");
            }
         }
         else
         {
            log.debug("SOAP response message is null");
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   private static String getFromName(Destination destination, boolean queue) throws JMSException
   {
      return queue ? "queue/" + ((Queue)destination).getQueueName() : "topic/" + ((Topic)destination).getTopicName();
   }
   
   protected void processSOAPMessage(String fromName, InputStream inputStream, OutputStream outStream) throws SOAPException, IOException, RemoteException
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      EndpointRegistry epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();

      Endpoint endpoint = getEndpointForDestination(epRegistry, fromName);

      if (endpoint == null)
         throw new IllegalStateException("Cannot find endpoint for: " + fromName);

      EndpointAssociation.setEndpoint(endpoint);
      try
      {
         boolean debugEnabled = log.isDebugEnabled();
         if (debugEnabled)
            log.debug("dipatchMessage: " + endpoint.getName());

         // [JBWS-1324]: workaround to prevent message processing before endpoint is started
         EndpointState state = endpoint.getState();
         ObjectName name = endpoint.getName();
         long startTime = System.currentTimeMillis();
         if (debugEnabled)
            log.debug(name + " is in state: " + state);
         while (state != EndpointState.STARTED && (System.currentTimeMillis() - startTime < 60000))
         {
            try
            {
               Thread.sleep(1000);
               state = endpoint.getState();
               if (debugEnabled)
                  log.debug(name + " is now in state: " + state);
            }
            catch (InterruptedException e)
            {
               throw new EJBException(e);
            }
         }

         RequestHandler reqHandler = endpoint.getRequestHandler();

         try
         {
            InvocationContext invContext = new InvocationContext();
            invContext.setTargetBean(this);

            reqHandler.handleRequest(endpoint, inputStream, outStream, invContext);
         }
         catch (Exception ex)
         {
            throw new RemoteException("Cannot process SOAP request", ex);
         }
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
      }
   }

   // The destination jndiName is encoded in the service object name under key 'jms'
   private Endpoint getEndpointForDestination(EndpointRegistry epRegistry, String fromName)
   {
      Endpoint endpoint = null;
      for (ObjectName oname : epRegistry.getEndpoints())
      {
         Endpoint aux = epRegistry.getEndpoint(oname);
         String jmsProp = aux.getName().getKeyProperty("jms");
         if (jmsProp != null && jmsProp.equals(fromName))
         {
            endpoint = aux;
            break;
         }
      }
      return endpoint;
   }

   private String getMessageStr(BytesMessage message) throws Exception
   {
      byte[] buffer = new byte[8 * 1024];
      ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length);
      int read = message.readBytes(buffer);
      while (read != -1)
      {
         out.write(buffer, 0, read);
         read = message.readBytes(buffer);
      }

      byte[] msgBytes = out.toByteArray();
      return new String(msgBytes);
   }

   /**
    * Get the reply queue.
    */
   protected Queue getReplyQueue(Message message) throws JMSException
   {
      Queue replyQueue = (Queue)message.getJMSReplyTo();
      return replyQueue;
   }

   /**
    * Respond to the call by sending a message to the reply queue
    */
   protected void sendResponse(Queue replyQueue, String msgStr) throws SOAPException, IOException, JMSException
   {
      QueueConnection qc = getQueueFactory().createQueueConnection();
      QueueSession session = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueSender sender = null;
      try
      {
         sender = session.createSender(replyQueue);
         TextMessage responseMessage = session.createTextMessage(msgStr);
         sender.send(responseMessage);
         log.info("Sent response");
      }
      finally
      {
         try
         {
            sender.close();
         }
         catch (JMSException ignored)
         {
         }
         try
         {
            session.close();
         }
         catch (JMSException ignored)
         {
         }
         try
         {
            qc.close();
         }
         catch (JMSException ignored)
         {
         }
      }
   }

   private QueueConnectionFactory getQueueFactory()
   {
      if (conFactory == null)
      {
         try
         {
            InitialContext ctx = new InitialContext();
            conFactory = (QueueConnectionFactory)ctx.lookup("java:/ConnectionFactory");
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception e)
         {
            throw new NestedRuntimeException(e);
         }
      }
      return conFactory;
   }
}
