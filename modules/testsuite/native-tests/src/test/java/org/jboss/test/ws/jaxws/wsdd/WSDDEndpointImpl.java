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
package org.jboss.test.ws.jaxws.wsdd;

import org.jboss.ws.extensions.xop.XOPContext;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since Mar 12, 2007
 */
@WebService
public class WSDDEndpointImpl implements WSDDEndpoint {

   @WebMethod
   public ResponseMessage echo(Message message)
   {
      System.out.println(message.msg);
      return new ResponseMessage(message.msg);
   }

   @WebMethod
   public Boolean checkMTOMEnabled()
   {
      // At this point it's not set
      // The MTOM property is avaialbel when the CommonSOAPBinding
      // did bin the reponse. Therefore we delegate this task to the handler impl.
      return Boolean.FALSE;
   }

}
