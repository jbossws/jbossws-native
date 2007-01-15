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
package org.jboss.ws.extensions.eventing.mgmt;

// $Id$

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.jboss.ws.extensions.eventing.deployment.EventingEndpointDI;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.jboss.ws.extensions.eventing.element.NotificationFailure;
import org.w3c.dom.Element;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 12-Dec-2005
 */
public interface SubscriptionManagerMBean
{
   public static final String BEAN_NAME = "SubscriptionManager";

   /**
    * Returns the core number of threads.
    *
    * @return long
    */
   int getCorePoolSize();

   /**
    * Returns the maximum allowed number of threads.
    *
    * @return int
    */
   int getMaximumPoolSize();

   /**
    * Returns the largest number of threads that have ever simultaneously been in the pool.
    *
    * @return int
    */
   int getLargestPoolSize();

   /**
    * Returns the approximate number of threads that are actively executing tasks.
    *
    * @return int
    */
   int getActiveCount();

   /**
    * Returns the approximate total number of tasks that have completed execution.
    *
    * @return long
    */
   long getCompletedTaskCount();

   public void setCorePoolSize(int corePoolSize);

   public void setMaxPoolSize(int maxPoolSize);

   public void setEventKeepAlive(long millies);

   // subscription endpointReference business
   SubscriptionTicket subscribe(URI eventSourceNS, EndpointReference notifyTo, EndpointReference endTo, Date expires, Filter filter) throws SubscriptionError;

   Date renew(URI identifier, Date lease) throws SubscriptionError;

   Date getStatus(URI identifier) throws SubscriptionError;

   void unsubscribe(URI identifier) throws SubscriptionError;

   // notification API
   void dispatch(URI eventSourceNS, Element payload);

   void registerEventSource(EventingEndpointDI deploymentInfo);

   void removeEventSource(URI eventSourceNS);

   String showSubscriptionTable();

   String showEventsourceTable();

   public void addNotificationFailure(NotificationFailure failure);

   public List<NotificationFailure> showNotificationFailures();

   public boolean isValidateNotifications();

   public void setValidateNotifications(boolean validateNotifications);
}
