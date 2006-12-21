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
package org.jboss.ws.integration.jboss50;

import java.util.Iterator;

import javax.management.ObjectName;
import javax.naming.Context;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ws.core.utils.ObjectNameFactory;

/**
 * MBean interface.
 */
public interface ServiceRefHandlerMBean
{
   // default object name
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ws:service=ServiceRefHandler");

   /**
    * Binds a JAXRPC Service into the callers ENC for every service-ref element
    *
    * @param envCtx      ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param deployment  The client's deployment unit
    * @throws DeploymentException if it goes wrong
    */
   void setupServiceRefEnvironment(Context envCtx, Iterator serviceRefs, Object deployment);
}
