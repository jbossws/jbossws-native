/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1999;

import javax.jws.WebService;

import org.jboss.ws.api.annotation.EndpointConfig;

/**
 * Test Endpoint to test UsernameToken authorization / authentication
 * for POJO endpoints.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 12th January 2008
 * @see https://jira.jboss.org/jira/browse/JBWS-1999
 */
@WebService(name = "Endpoint", serviceName = "EndpointService", targetNamespace = "http://ws.jboss.org/jbws1999", endpointInterface = "org.jboss.test.ws.jaxws.jbws1999.Endpoint")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
public class EndpointImpl implements Endpoint
{

   public String echoEnemyRequired(final String message)
   {
      return message;
   }

   public String echoFriendRequired(final String message)
   {
      return message;
   }

   public String echoNoSecurity(final String message)
   {
      return message;
   }

   public String echoUnchecked(final String message)
   {
      return message;
   }

}
