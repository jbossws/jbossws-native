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
package org.jboss.test.ws.jaxws.samples.news;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.wsf.spi.annotation.WebContext;


@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.samples.news.NewspaperMTOM",
      name = "NewspaperMTOMEndpoint",
      targetNamespace = "http://org.jboss.ws/samples/news",
      serviceName = "NewspaperMTOMService")
@SOAPBinding(style = SOAPBinding.Style.RPC,
       use = SOAPBinding.Use.LITERAL)
@SecurityDomain("JBossWS")
@WebContext(contextRoot="/news",
      urlPattern="/newspaper/mtom",
      authMethod="BASIC",
      transportGuarantee="CONFIDENTIAL",
      secureWSDLAccess=false)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class SecureNewspaperMTOMEndpoint extends AbstractNewspaperMTOMEndpoint implements NewspaperMTOM
{
   
}
