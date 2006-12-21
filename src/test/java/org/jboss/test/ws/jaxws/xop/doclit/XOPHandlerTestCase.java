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
package org.jboss.test.ws.jaxws.xop.doclit;

import junit.framework.Test;
import org.jboss.logging.Logger;
import org.jboss.test.ws.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URL;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since 05.12.2006
 */
public class XOPHandlerTestCase extends XOPBase {

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-xop-doclit/MTOMEndpointBean";

   private Logger log = Logger.getLogger(XOPHandlerTestCase.class);

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(XOPHandlerTestCase.class, "jaxws-xop-doclit.jar, jaxws-xop-doclit-client.jar");
   }

   protected void setUp() throws Exception
   {

      QName serviceName = new QName("http://org.jboss.ws/xop/doclit", "MTOMService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS+"?wsdl");

      javax.xml.ws.Service service = javax.xml.ws.Service.create(wsdlURL, serviceName);
      port = service.getPort(MTOMEndpoint.class);

      // enable MTOM
      binding = (SOAPBinding)((BindingProvider) port).getBinding();
      binding.getHandlerChain().add(new MTOMProtocolHandler());

      // debug request/reponse

      /*BindingProvider bp = (BindingProvider)port;
      Map<String,Object> context = bp.getRequestContext();
      context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8081/jaxws-xop-doclit/MTOMEndpointBean");
      */
   }

   protected MTOMEndpoint getPort() {
      return port;
   }

   protected SOAPBinding getBinding() {
      return binding;
   }

}