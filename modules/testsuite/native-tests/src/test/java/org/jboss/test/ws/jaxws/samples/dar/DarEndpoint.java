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
package org.jboss.test.ws.jaxws.samples.dar;

//$Id$

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.wsf.spi.annotation.WebContext;

/**
 * Performs DAR route optimization
 *
 * @author alessio.soldano@jboss.org
 * @since 31-Jan-2008
 */
@Stateless
@WebService(name = "DarEndpoint",
            targetNamespace = "http://org.jboss.ws/samples/dar",
            serviceName = "DarService")
@SOAPBinding(style = SOAPBinding.Style.RPC,
             use = SOAPBinding.Use.LITERAL)
//We're declaring the domain in the jboss.xml since we have different annotation packages for AS5 and AS42 
//@SecurityDomain("JBossWS")
@WebContext(contextRoot="/dar",
            urlPattern="/*",
            authMethod="BASIC",
            transportGuarantee="NONE",
            secureWSDLAccess=false)
public class DarEndpoint
{
   @WebMethod(operationName = "process", action = "http://org.jboss.test.ws.jaxws.samples.dar/action/processIn")
   public DarResponse process(DarRequest request)
   {
      DarProcessor processor = new DarProcessor();
      return processor.process(request);
   }
}
