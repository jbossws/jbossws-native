/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ResourceBundle;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.core.soap.utils.SOAPElementWriter;
import org.jboss.ws.core.soap.utils.XMLFragment;
import org.jboss.ws.core.utils.CachedOutputStream;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.w3c.dom.Element;

/**
 * Trace incoming/outgoing messages
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * 
 * @since 04-Apr-2007
 */
public final class MessageTrace
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(MessageTrace.class);
   private static final Logger msgLog = Logger.getLogger(MessageTrace.class);
   private static ServerConfig serverConfig = null;
   private static boolean serverConfigInit = false;

   private MessageTrace()
   {
      // forbidden constructor
   }

   public static void traceMessage(String messagePrefix, Object message)
   {
      if (!msgLog.isTraceEnabled()) return;
      
      if (message instanceof SOAPMessage)
      {
         try
         {
            if (message instanceof SOAPMessageImpl) {
               SOAPEnvelope soapEnv = ((SOAPMessage)message).getSOAPPart().getEnvelope();
               if (soapEnv != null)
               {
                  String envStr = SOAPElementWriter.writeElement(soapEnv, true);
                  msgLog.trace(messagePrefix + "\n" + envStr);
               }
            }
            else
            {
               SOAPMessage soapMessage = (SOAPMessage) message;
               String encoding = (String)soapMessage.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
               if (encoding == null)
               {
                  encoding = "UTF-8";
               }
               
               CachedOutputStream os = null;
               try
               {
                  os = new CachedOutputStream();
                  os.setThreshold(64*1024);
                  os.holdTempFile();
                  ServerConfig sc = getServerConfig();
                  if (sc != null) {
                     os.setOutputDir(sc.getServerTempDir());
                  }
                  soapMessage.writeTo(os);
                  os.flush();
                  if (os.getTempFile() != null)
                  {
                     msgLog.trace("SOAP Message saved to tmp file: " + os.getTempFile().getAbsolutePath());
                  }
                  else
                  {
                     StringBuilder sb = new StringBuilder();
                     write(sb, os, encoding, soapMessage.getMimeHeaders().getHeader(MimeConstants.CONTENT_TYPE)[0]);
                     msgLog.trace(messagePrefix + "\n" + sb.toString());
                  }
               }
               finally
               {
                  if (os != null)
                  {
                     os.close();
                  }
               }
            }
         }
         catch (Exception ex)
         {
            msgLog.error(BundleUtils.getMessage(bundle, "CANNOT_TRACE_SOAPMESSAGE"),  ex);
         }
      }
      else if (message instanceof byte[])
      {
         Element root = new XMLFragment(new StreamSource(new ByteArrayInputStream((byte[])message))).toElement();
         String xmlString = DOMWriter.printNode(root, true);
         msgLog.trace(messagePrefix + "\n" + xmlString);
      }
      else if (message instanceof String)
      {
         Element root = new XMLFragment(new StreamSource(new ByteArrayInputStream(((String)message).getBytes()))).toElement();
         String xmlString = DOMWriter.printNode(root, true);
         msgLog.trace(messagePrefix + "\n" + xmlString);
      }
      else
      {
          msgLog.warn(BundleUtils.getMessage(bundle, "UNSUPPORTED_MESSAGE_TYPE",  message));
      }
   }
   
   private static void write(StringBuilder builder, CachedOutputStream cos, String encoding, String contentType) throws Exception
   {
      if ((contentType != null && contentType.indexOf("xml") >= 0 && contentType.toLowerCase().indexOf("multipart/related") < 0) && cos.size() > 0)
      {
         TransformerFactory tf = TransformerFactory.newInstance();
         try {
            tf.setAttribute("indent-number", 2);
         } catch (Throwable t) {} //ignore
         Transformer serializer = tf.newTransformer();
         serializer.setOutputProperty(OutputKeys.INDENT, "yes");
         serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

         StringWriter swriter = new StringWriter();
         serializer.transform(new StreamSource(cos.getInputStream()), new StreamResult(swriter));
         builder.append(swriter.toString());
      }
      else
      {
         if (encoding == null || encoding.trim().length() == 0)
         {
            cos.writeCacheTo(builder, "UTF-8");
         }
         else
         {
            cos.writeCacheTo(builder, encoding);
         }

      }
   }
   
   private static synchronized ServerConfig getServerConfig()
   {
      if (!serverConfigInit)
      {
         try {
            final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
            SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
            serverConfig = spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
         } catch (Exception e) {
            //ignore
         } finally {
            serverConfigInit = true;
         }
      }
      return serverConfig;
   }
}
