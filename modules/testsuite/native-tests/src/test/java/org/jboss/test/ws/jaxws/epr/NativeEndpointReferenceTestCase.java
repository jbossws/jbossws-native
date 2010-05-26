/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.epr;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.jboss.ws.core.jaxws.wsaddressing.NativeEndpointReference;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Tests NativeEndpointReference de/serializations.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NativeEndpointReferenceTestCase extends JBossWSTest
{
   private static final String URL = "http://localhost:8080/hello";
   private static final String WSDL_URL = URL + "?wsdl";
   private static final String MY_NS = "http://helloservice.org/wsdl";
   private static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
   private static final String WSAM_NS = "http://www.w3.org/2007/05/addressing/metadata";
   private static final String WSAM_PREFIX = "wsam";
   private static final String MY_PREFIX = "myns";
   private static final QName PARAM1_QNAME = new QName("http://helloservice.org/param1", "param1", "ns1");
   private static final QName PARAM2_QNAME = new QName("http://helloservice.org/param2", "param2", "ns2");
   private static final QName WSAM_SERVICE_QNAME = new QName(WSAM_NS, "ServiceName");
   private static final QName WSAM_INTERFACE_QNAME = new QName(WSAM_NS, "InterfaceName");
   private static final QName METADATA_QNAME = new QName(WSA_NS, "Metadata");
   private static final String XML = 
      "<EndpointReference xmlns='http://www.w3.org/2005/08/addressing'> " +
      "  <Address>http://localhost:8080/hello</Address>" +
      "  <ReferenceParameters>" +
      "    <ns1:param1 wsa:IsReferenceParameter='true' xmlns:ns1='http://helloservice.org/param1' xmlns:wsa='http://www.w3.org/2005/08/addressing'>Hello</ns1:param1>" +
      "    <ns2:param2 wsa:IsReferenceParameter='true' xmlns:ns2='http://helloservice.org/param2' xmlns:wsa='http://www.w3.org/2005/08/addressing'>World</ns2:param2>" +
      "  </ReferenceParameters>" +
      "  <Metadata wsdli:wsdlLocation='http://helloservice.org/wsdl http://localhost:8080/hello?wsdl' xmlns:wsdli='http://www.w3.org/ns/wsdl-instance'>" +
      "    <wsam:ServiceName EndpointName='myns:HelloPort' xmlns:myns='http://helloservice.org/wsdl' xmlns:wsam='http://www.w3.org/2007/05/addressing/metadata'>myns:HelloService</wsam:ServiceName>" +
      "    <wsam:InterfaceName xmlns:myns='http://helloservice.org/wsdl' xmlns:wsam='http://www.w3.org/2007/05/addressing/metadata'>myns:Hello</wsam:InterfaceName>" +
      "  </Metadata>" +
      "</EndpointReference>";

   public void testNativeEndpointReferenceFromSource() throws Exception
   {
      System.out.println(DOMUtils.node2String(DOMUtils.parse(XML)));
      final Source xml = new DOMSource(DOMUtils.parse(XML));
      NativeEndpointReference epr = new NativeEndpointReference(xml);
      DOMResult dr = new DOMResult(); 
      epr.writeTo(dr);
      Node endpointReferenceElement = dr.getNode();
      System.out.println(DOMUtils.node2String(endpointReferenceElement));
      assertMetaData(endpointReferenceElement);
      assertRefParam(endpointReferenceElement, PARAM1_QNAME, "Hello");
      assertRefParam(endpointReferenceElement, PARAM2_QNAME, "World");
      assertEquals(new QName(MY_NS, "HelloService", MY_PREFIX), epr.getServiceName());
      assertEquals(new QName(MY_NS, "Hello", MY_PREFIX), epr.getInterfaceName());
      assertEquals(new QName(MY_NS, "HelloPort", MY_PREFIX), epr.getEndpointName());
   }
   
   private static void assertRefParam(final Node root, final QName nodeName, final String refParamValue)
   {
      Element e = (Element)DOMUtils.getFirstChildElement(root, nodeName, true);
      assertNotNull("Reference parameter " + nodeName + " not found", e);
      String actual = DOMUtils.getTextContent(e);
      if ((actual == null) || (!actual.equals(refParamValue)))
      {
         fail("Reference parameter " + nodeName + " expected value is " + refParamValue);
      }
   }
   
   private static void assertMetaData(final Node root)
   {
      Element metadataElement = (Element)DOMUtils.getFirstChildElement(root, METADATA_QNAME, true);
      String wsdlLocationValue = metadataElement.getAttributeNodeNS("http://www.w3.org/ns/wsdl-instance", "wsdlLocation").getValue();
      assertEquals("wsdlLocation mismatch", wsdlLocationValue, MY_NS + " " + WSDL_URL);
      Element serviceNameElement = (Element)DOMUtils.getFirstChildElement(metadataElement, WSAM_SERVICE_QNAME);
      assertNamespaces(serviceNameElement);
      assertEquals("wrong text content in ServiceName element", "myns:HelloService", DOMUtils.getTextContent(serviceNameElement));
      String endpointNameValue = DOMUtils.getAttributeValue(serviceNameElement, "EndpointName");
      assertNotNull("cannot find endpointName attribute value", endpointNameValue);
      assertEquals("wrong endpointName attribute value", endpointNameValue, "myns:HelloPort");
      Element interfaceNameElement = (Element)DOMUtils.getFirstChildElement(metadataElement, WSAM_INTERFACE_QNAME);
      assertNamespaces(interfaceNameElement);
      assertEquals("wrong text content in InterfaceName element", "myns:Hello", DOMUtils.getTextContent(interfaceNameElement));
   }
   
   private static void assertNamespaces(final Element e)
   {
      String myNamespace = e.lookupNamespaceURI(MY_PREFIX);
      assertNotNull("namespace is null for prefix " + MY_PREFIX + ", are you using our patched xalan?", myNamespace);
      assertEquals("namespace mismatch", myNamespace, MY_NS);
      String wsamNamespace = e.lookupNamespaceURI(WSAM_PREFIX);
      assertNotNull("namespace is null for prefix " + WSAM_PREFIX + ", are you using our patched xalan?", wsamNamespace);
      assertEquals("namespace mismatch", wsamNamespace, WSAM_NS);
   }
}
