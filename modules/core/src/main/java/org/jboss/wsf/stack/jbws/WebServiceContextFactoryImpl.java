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
package org.jboss.wsf.stack.jbws;

import javax.xml.ws.handler.MessageContext;

import org.jboss.ws.common.invocation.WebServiceContextEJB;
import org.jboss.ws.common.invocation.WebServiceContextJSE;
import org.jboss.wsf.spi.invocation.ExtensibleWebServiceContext;
import org.jboss.wsf.spi.invocation.InvocationType;
import org.jboss.wsf.spi.invocation.WebServiceContextFactory;

/**
 * 
 * @author alessio.soldano@jboss.com
 * 
 */
public class WebServiceContextFactoryImpl extends WebServiceContextFactory
{
   public ExtensibleWebServiceContext newWebServiceContext(InvocationType type, MessageContext messageContext)
   {
      ExtensibleWebServiceContext context = null;

      if(type.toString().indexOf("EJB")!=-1 || type.toString().indexOf("MDB")!=-1)
         context = new WebServiceContextEJB(new NativeWebServiceContext(messageContext));
      else
         context = new WebServiceContextJSE(new NativeWebServiceContext(messageContext));

      return context;
   }
}
