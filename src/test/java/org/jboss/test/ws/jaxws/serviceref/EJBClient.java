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
package org.jboss.test.ws.jaxws.serviceref;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.rmi.RemoteException;
import java.util.ArrayList;

@Remote(EJBRemote.class)
@RemoteBinding(jndiBinding = "/ejb/EJBClient")
@Stateless

public class EJBClient 
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJBClient.class);

   public String echo(String inStr) throws RemoteException
   {
      log.info("echo: " + inStr);

      ArrayList ports = new ArrayList(2);

      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add((TestEndpoint)((Service)iniCtx.lookup("java:comp/env/service1")).getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service2")).getTestEndpointPort());
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex);
      }

      for (int i = 0; i < ports.size(); i++)
      {
         TestEndpoint port = (TestEndpoint)ports.get(i);

         BindingProvider bp = (BindingProvider)port;
         boolean mtomEnabled = ((SOAPBinding)bp.getBinding()).isMTOMEnabled();
         boolean expectedSetting = (i==0) ? false : true;

         if(mtomEnabled != expectedSetting)
            throw new WebServiceException("MTOM settings (enabled="+expectedSetting+") not overridden through service-ref" );

         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }

      return inStr;
   }
}
