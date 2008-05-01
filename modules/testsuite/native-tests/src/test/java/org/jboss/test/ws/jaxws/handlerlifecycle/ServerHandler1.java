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
package org.jboss.test.ws.jaxws.handlerlifecycle;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

public class ServerHandler1 extends LifecycleHandler
{
   protected boolean handleOutboundMessage(MessageContext msgContext)
   {
      boolean doNext = true;
      if (doNext && getTestMethod(msgContext).startsWith("testPropertyScoping"))
      {
         if (msgContext.get("server-handler-prop") != Boolean.TRUE)
            throw new IllegalStateException("Cannot find server-handler-prop");

         if (msgContext.get("server-app-prop") != Boolean.TRUE)
            throw new IllegalStateException("Cannot find server-app-prop");
      }
      return doNext;
   }

   protected boolean handleInboundMessage(MessageContext msgContext)
   {
      boolean doNext = true;
      if (doNext && getTestMethod(msgContext).startsWith("testPropertyScoping"))
      {
         // set a handler prop
         msgContext.put("server-handler-prop", Boolean.TRUE);

         // set a app prop
         msgContext.put("server-app-prop", Boolean.TRUE);
         msgContext.setScope("server-app-prop", Scope.APPLICATION);
      }
      return doNext;
   }
}
