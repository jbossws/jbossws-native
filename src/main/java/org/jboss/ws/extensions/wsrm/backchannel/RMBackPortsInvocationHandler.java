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
package org.jboss.ws.extensions.wsrm.backchannel;

import javax.management.MBeanServer;

import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.coyote.RequestMap;
import org.jboss.remoting.transport.http.HTTPMetadataConstants;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 20, 2007
 */
public final class RMBackPortsInvocationHandler implements ServerInvocationHandler
{
   public RMBackPortsInvocationHandler()
   {
      
   }

   public void addListener(InvokerCallbackHandler arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public Object invoke(InvocationRequest arg0) throws Throwable
   {
      RequestMap rm = (RequestMap)arg0.getRequestPayload();
      System.out.println("... locator ..." + arg0.getLocator());
      System.out.println("... subsystem ..." + arg0.getSubsystem());
      System.out.println("... parameter ..." + arg0.getParameter());
      System.out.println("... parameter ..." + arg0.getParameter().getClass().getName());
      System.out.println("... method ..." + rm.get(HTTPMetadataConstants.METHODTYPE));
      System.out.println("... path ..." + rm.get(HTTPMetadataConstants.PATH));
      System.out.println("return ..." + arg0.getReturnPayload());
      return null;
   }

   public void removeListener(InvokerCallbackHandler arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public void setInvoker(ServerInvoker arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public void setMBeanServer(MBeanServer arg0)
   {
      // TODO Auto-generated method stub
      
   }
   
}
