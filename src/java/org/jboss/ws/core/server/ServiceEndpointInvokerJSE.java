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
package org.jboss.ws.core.server;

// $Id: $

import java.lang.reflect.Method;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.core.jaxrpc.ServletEndpointContextImpl;
import org.jboss.ws.core.jaxws.WebServiceContextInjector;
import org.jboss.ws.core.jaxws.WebServiceContextJSE;
import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public class ServiceEndpointInvokerJSE extends AbstractServiceEndpointInvoker implements ServiceEndpointInvoker
{
   // provide logging
   private Logger log = Logger.getLogger(ServiceEndpointInvokerJSE.class);

   /** Load the SEI implementation bean if necessary */
   public Class loadServiceEndpoint() throws ClassNotFoundException
   {
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      ClassLoader cl = epMetaData.getClassLoader();
      String seiImplName = epMetaData.getServiceEndpointImplName();
      Class seiImplClass = cl.loadClass(seiImplName);
      return seiImplClass;
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public Object createServiceEndpointInstance(Object context, Class seiImplClass) throws IllegalAccessException, InstantiationException
   {
      Object seiImpl = seiImplClass.newInstance();
      if (seiImpl instanceof ServiceLifecycle && context != null)
      {
         try
         {
            ServiceLifecycle serviceLifecycle = ((ServiceLifecycle)seiImpl);
            ServletEndpointContext servletEndpointContext = new ServletEndpointContextImpl((EndpointContext)context);
            serviceLifecycle.init(servletEndpointContext);
         }
         catch (ServiceException ex)
         {
            throw new WSException(ex);
         }
      }
      return seiImpl;
   }

   /** Invoke an instance of the SEI implementation bean */
   public void invokeServiceEndpointInstance(Object seiImpl, EndpointInvocation epInv) throws SOAPFaultException, Exception
   {
      if(log.isDebugEnabled()) log.debug("invokeServiceEndpoint: " + epInv.getJavaMethod().getName());
      try
      {
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext instanceof SOAPMessageContextJAXWS)
         {
            WebServiceContext wsContext = new WebServiceContextJSE((SOAPMessageContextJAXWS)msgContext);
            new WebServiceContextInjector().injectContext(seiImpl, wsContext);
         }
         
         Class implClass = seiImpl.getClass();
         Method seiMethod = epInv.getJavaMethod();
         Method implMethod = getImplMethod(implClass, seiMethod);

         Object[] args = epInv.getRequestPayload();
         Object retObj = implMethod.invoke(seiImpl, args);
         epInv.setReturnValue(retObj);
      }
      catch (Exception e)
      {
         handleInvocationException(e);
      }
   }

   /** Destroy an instance of the SEI implementation bean if necessary */
   public void destroyServiceEndpointInstance(Object seiImpl)
   {
      if (seiImpl instanceof ServiceLifecycle)
      {
         ((ServiceLifecycle)seiImpl).destroy();
      }
   }
}
