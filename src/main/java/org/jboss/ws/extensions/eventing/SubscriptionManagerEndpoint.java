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
package org.jboss.ws.extensions.eventing;

// $Id$

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jboss.ws.extensions.eventing.element.RenewRequest;
import org.jboss.ws.extensions.eventing.element.RenewResponse;
import org.jboss.ws.extensions.eventing.element.StatusRequest;
import org.jboss.ws.extensions.eventing.element.StatusResponse;
import org.jboss.ws.extensions.eventing.element.UnsubscribeRequest;

/**
 * Subscription manager endpoint interface.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 16-Dec-2005
 */
public interface SubscriptionManagerEndpoint extends Remote
{

   StatusResponse getStatus(StatusRequest request) throws RemoteException;

   RenewResponse renew(RenewRequest request) throws RemoteException;

   void unsubscribe(UnsubscribeRequest request) throws RemoteException;
}
