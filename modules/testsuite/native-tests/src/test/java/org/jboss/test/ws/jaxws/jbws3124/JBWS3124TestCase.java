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
package org.jboss.test.ws.jaxws.jbws3124;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-2942] Do not add empty Metadata tag to EndpointReference.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class JBWS3124TestCase extends JBossWSTest
{
   private final String XML_SOURCE = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
   "<EndpointReference xmlns='http://www.w3.org/2005/08/addressing'>\n" +
   "  <Address>http://localhost:8080/jaxws-endpointReference</Address>\n" +
   "  <Metadata/>\n" +
   "</EndpointReference>\n";

   public void testToString() throws Exception
   {
      assertTrue("Lost empty <Metadata/>", this.getXML(XML_SOURCE).indexOf("<Metadata") != -1);
      StreamSource source = new StreamSource(new StringReader(XML_SOURCE));
      EndpointReference epRef = EndpointReference.readFrom(source);
      assertTrue("Empty <Metadata/> serialized", epRef.toString().indexOf("<Metadata") == -1);
   }
   
   private String getXML(final String s) throws Exception
   {
      return DOMUtils.node2String(DOMUtils.parse(s));
   }
}
