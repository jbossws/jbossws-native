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
package org.jboss.test.ws.jaxws.endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests Endpoint deployment in J2EE environment.
 *
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@SuppressWarnings("serial")
public class EndpointServlet extends HttpServlet
{
   
   private Endpoint endpoint1;
   private Endpoint endpoint2;
   private static final String TEST_ELEMENT = "<fabrikam:CustomerKey xmlns:fabrikam='http://example.com/fabrikam'>123456789</fabrikam:CustomerKey>";
   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      
      endpoint1 = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new EndpointBean());
      endpoint1.publish("http://localhost:8081/jaxws-endpoint");
      endpoint2 = Endpoint.publish("http://localhost:8081/jaxws-endpoint2/endpoint/long/path", EndpointBean.class);
   }
   
   @Override
   public void destroy()
   {
      endpoint1.stop();
      endpoint2.stop();
      
      super.destroy();
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      // Create the port
      URL wsdlURL = getServletContext().getResource("/WEB-INF/wsdl/TestService.wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      // Invoke the endpoint
      String param = req.getParameter("param");
      String retStr = port.echo(param);
      
      //Test epr
      assertEndpointReference(endpoint1.getEndpointReference(DOMUtils.parse(TEST_ELEMENT)), TEST_ELEMENT);
      assertEndpointReference(endpoint1.getEndpointReference(W3CEndpointReference.class, (Element[])null), null);

      // Return the result
      PrintWriter pw = new PrintWriter(res.getWriter());
      pw.print(retStr);
      pw.close();
   }
   
   private void assertEndpointReference(EndpointReference epr, String refPar) throws IOException
   {
      Logger.getLogger(this.getClass()).info("epr: "+epr);
      assert(W3CEndpointReference.class.getName().equals(epr.getClass().getName()));
      Element endpointReference = DOMUtils.parse(epr.toString());
      assert("EndpointReference".equals(endpointReference.getNodeName()));
      assert("http://www.w3.org/2005/08/addressing".equals(endpointReference.getAttribute("xmlns")));
      NodeList addresses = endpointReference.getElementsByTagName("Address");
      assert(addresses.getLength() == 1);
      assert("http://127.0.0.1:8080/jaxws-endpoint".equals(addresses.item(0).getFirstChild().getNodeValue()));
      if (refPar != null)
      {
         assert(epr.toString().contains(refPar));
      }
   }
}
