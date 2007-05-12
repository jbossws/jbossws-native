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
package org.jboss.test.ws.jaxws.wseventing;

// $Id: NotificationTestCase.java 1757 2006-12-22 15:40:24Z thomas.diesler@jboss.com $

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.jaxws.SubscribeResponse;
import org.jboss.ws.extensions.eventing.mgmt.EventDispatcher;
import org.jboss.ws.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test the notification delivery.
 *
 * @author heiko@openj.net
 * @since 29-Apr-2005
 */
public class NotificationTestCase extends EventingSupport
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(NotificationTestCase.class, "jaxws-wseventing.war");
   }

   public void testNotification() throws Exception {

      SubscribeResponse response = doSubscribe();

      Element payload = DOMUtils.parse(eventString);
      try
      {
         InitialContext iniCtx = getInitialContext();
         EventDispatcher delegate = (EventDispatcher)
               iniCtx.lookup(EventingConstants.DISPATCHER_JNDI_NAME);
         delegate.dispatch(eventSourceURI, payload);
         Thread.sleep(3000);         
      }
      catch (Exception e)
      {         
         throw e;
      }
   }

}
