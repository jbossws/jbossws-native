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
package org.jboss.ws.integration.jboss42;

// $Id$

import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.rpc.soap.SOAPFaultException;

import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.WSException;
import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.core.server.AbstractServiceEndpointInvoker;
import org.jboss.ws.core.server.ServiceEndpointInfo;
import org.jboss.ws.core.server.ServiceEndpointInvoker;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.utils.ObjectNameFactory;

/**
 * Handles invocations on EJB3 endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Jul-2005
 */
public class ServiceEndpointInvokerEJB3 extends AbstractServiceEndpointInvoker implements ServiceEndpointInvoker
{
   // provide logging
   private Logger log = Logger.getLogger(ServiceEndpointInvokerEJB3.class);

   private MBeanServer server;
   private ObjectName objectName;

   public ServiceEndpointInvokerEJB3()
   {
      server = MBeanServerLocator.locateJBoss();
   }
   
   /** Initialize the service endpoint */
   @Override
   public void initServiceEndpoint(ServiceEndpointInfo seInfo) 
   {
      super.initServiceEndpoint(seInfo);
      
      String ejbName = seInfo.getServerEndpointMetaData().getLinkName();
      UnifiedDeploymentInfo udi = seInfo.getUnifiedDeploymentInfo();
      String nameStr = "jboss.j2ee:name=" + ejbName + ",service=EJB3,jar=" + udi.simpleName;
      if (udi.parent != null)
      {
         nameStr += ",ear=" + udi.parent.simpleName;
      }
      
      objectName = ObjectNameFactory.create(nameStr.toString());
   }
   
   /** Load the SEI implementation bean if necessary 
    */
   public Class loadServiceEndpoint(ServiceEndpointInfo seInfo)
   {
      if (server.isRegistered(objectName) == false)
         throw new WSException("Cannot find service endpoint target: " + objectName);
      
      return null;
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public Object createServiceEndpoint(ServiceEndpointInfo seInfo, Object endpointContext, Class seiImplClass)
   {
      return null;
   }

   /** Invoke an instance of the SEI implementation bean */
   public void invokeServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl, EndpointInvocation epInv) throws Exception
   {
      log.debug("invokeServiceEndpoint: " + epInv.getJavaMethod().getName());

      /* [FIXME] how to do this for EJB3
       
       // these are provided by the ServerLoginHandler
       Principal principal = SecurityAssociation.getPrincipal();
       Object credential = SecurityAssociation.getCredential();

       CommonMessageContext msgContext = MessageContextAssociation.getMessageContext();
       
       Invocation inv = new Invocation(null, method, args, null, principal, credential);
       inv.setValue(InvocationKey.SOAP_MESSAGE_CONTEXT, msgContext);
       inv.setValue(InvocationKey.SOAP_MESSAGE, msgContext.getMessage());
       inv.setType(InvocationType.SERVICE_ENDPOINT);
       */

      // invoke on the container
      try
      {
         // setup the invocation
         Method seiMethod = epInv.getJavaMethod();
         Object[] args = epInv.getRequestPayload();

         Dispatcher dispatcher = Dispatcher.singleton;
         String canonicalName = objectName.getCanonicalName();
         StatelessContainer container = (StatelessContainer)dispatcher.getRegistered(canonicalName);
         if (container == null)
            throw new WSException("Cannot obtain container from Dispatcher: " + canonicalName);

         Class implClass = container.getBeanClass();
         Method implMethod = getImplMethod(implClass, seiMethod);

         Object retObj = container.localInvoke(implMethod, args);
         epInv.setReturnValue(retObj);
      }
      catch (Throwable e)
      {
         handleInvocationException(e);
      }
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public void destroyServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl)
   {
      // do nothing
   }
}
