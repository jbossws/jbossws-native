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
package org.jboss.test.ws.jaxws.jbws2526;

import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.ws.WSException;
import org.jboss.wsf.common.handler.GenericLogicalHandler;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Logical handler implementation.
 *
 */
public class LogicalHandler extends GenericLogicalHandler
{

   @Override
   protected boolean handleInbound(MessageContext messageContext)
   {
      LogicalMessageContext context = (LogicalMessageContext)messageContext;
      LogicalMessage message = context.getMessage();
      Object payload = message.getPayload();
      if (!(payload instanceof DOMSource))
         throw new WSException("Test requires DOMSource payload");

      Node node = ((DOMSource)payload).getNode();
      NodeList nodes = node.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node current = nodes.item(i);
         if (current.getNodeType() == Node.COMMENT_NODE)
         {
            if (!(current instanceof Comment))
               throw new WSException("Found comment node that does not implement org.w3c.dom.Comment");
         }
         else if ("Value2".equals(current.getLocalName()))
         {
            Node child = current.getFirstChild();
            child.setNodeValue("2");
         }
      }

      return true;
   }

}
