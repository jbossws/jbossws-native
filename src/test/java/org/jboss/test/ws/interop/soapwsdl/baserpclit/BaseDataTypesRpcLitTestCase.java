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
package org.jboss.test.ws.interop.soapwsdl.baserpclit;

import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.soapwsdl.BaseDataTypesSupport;
import org.jboss.test.ws.interop.soapwsdl.BaseDataTypesSEI;
import org.jboss.test.ws.interop.soapwsdl.BaseDataTypesProxy;
import junit.framework.Test;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Feb-2006
 */
public class BaseDataTypesRpcLitTestCase extends BaseDataTypesSupport {

   static IBaseDataTypesRpcLit targetPort;
   static BaseDataTypesSEI proxy;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(BaseDataTypesRpcLitTestCase.class, "jbossws-interop-BaseDataTypesRpcLit.war");
   }

    protected void setUp() throws Exception
   {
      super.setUp();

      super.setUp();

      if (targetPort == null)
      {
         URL wsdlLocation = new File("resources/interop/soapwsdl/BaseDataTypesRpcLit/WEB-INF/wsdl/service.wsdl").toURL();
         Service service = Service.create(wsdlLocation, new QName("", "BaseDataTypesRpcLitService") );
         targetPort = service.getPort(IBaseDataTypesRpcLit.class);
         ((BindingProvider)targetPort).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://jbossws.demo.jboss.com:8080/baserpclit/endpoint");
         proxy = (BaseDataTypesSEI)BaseDataTypesProxy.newInstance(targetPort);
      }
   }

   protected BaseDataTypesSEI getTargetPort() throws Exception {
      return this.proxy;
   }
}
