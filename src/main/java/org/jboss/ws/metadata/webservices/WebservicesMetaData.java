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
// $Id$
package org.jboss.ws.metadata.webservices;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ws.Constants;

// $Id$

/**
 * XML Binding root element for <code>webservices.xml</code>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class WebservicesMetaData
{
   // The required <webservice-description> elements
   private ArrayList<WebserviceDescriptionMetaData> webserviceDescriptions = new ArrayList<WebserviceDescriptionMetaData>();

   // The URL to the webservices.xml descriptor
   private URL descriptorURL;

   public WebservicesMetaData()
   {
   }

   public WebservicesMetaData(URL descriptorURL)
   {
      this.descriptorURL = descriptorURL;
   }

   public URL getDescriptorURL()
   {
      return descriptorURL;
   }

   public void addWebserviceDescription(WebserviceDescriptionMetaData webserviceDescription)
   {
      webserviceDescriptions.add(webserviceDescription);
   }

   public WebserviceDescriptionMetaData[] getWebserviceDescriptions()
   {
      WebserviceDescriptionMetaData[] array = new WebserviceDescriptionMetaData[webserviceDescriptions.size()];
      webserviceDescriptions.toArray(array);
      return array;
   }

   //Serialize as a String
   public String serialize()
   {
      StringBuilder buffer = new StringBuilder();
      //Construct the webservices.xml definitions
      List qnames = new ArrayList();
      Iterator iter = webserviceDescriptions.iterator();
      while (iter != null && iter.hasNext())
      {
         WebserviceDescriptionMetaData wmd = (WebserviceDescriptionMetaData)iter.next();
         qnames.addAll(wmd.getPortComponentQNames());
      }
      createHeader(buffer, qnames);
      for (WebserviceDescriptionMetaData wm : webserviceDescriptions)
         buffer.append(wm.serialize());
      buffer.append("</webservices>");
      return buffer.toString();
   }

   private void createHeader(StringBuilder buf, List qnames)
   {
      buf.append("<webservices xmlns='http://java.sun.com/xml/ns/j2ee'");
      buf.append(" xmlns:xsi='" + Constants.NS_SCHEMA_XSI + "'");
      //Lets append the port type namespaces
      Iterator iter = qnames.iterator();
      while (iter != null && iter.hasNext())
      {
         QName qn = (QName)iter.next();
         buf.append(" xmlns:").append(qn.getPrefix()).append("='").append(qn.getNamespaceURI()).append("'");
      }
      buf.append(" xsi:schemaLocation='http://java.sun.com/xml/ns/j2ee http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd'");
      buf.append(" version='1.1' >");
   }
}
