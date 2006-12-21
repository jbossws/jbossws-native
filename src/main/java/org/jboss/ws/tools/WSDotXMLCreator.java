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
package org.jboss.ws.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.webservices.PortComponentMetaData;
import org.jboss.ws.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.ws.metadata.webservices.WebservicesMetaData;
import org.jboss.ws.tools.interfaces.WSDotXMLCreatorIntf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates the webservices.xml deployment descriptor
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since   Jun 20, 2005 
 */
public class WSDotXMLCreator implements WSDotXMLCreatorIntf
{
   // provide logging
   protected static final Logger log = Logger.getLogger(WSDotXMLCreator.class);
   protected String targetNamespace = null;

   protected String seiName = null;

   protected String portName = null;

   protected String serviceName = null;

   protected String servletLink = null;

   protected String ejbLink = null;

   protected String wsdlFile = null;
   protected String mappingFile = null;

   protected boolean append = false;

   public WSDotXMLCreator()
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setTargetNamespace(java.lang.String)
    */
   public void setTargetNamespace(String targetNamespace)
   {
      this.targetNamespace = targetNamespace;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setSeiName(java.lang.String)
    */
   public void setSeiName(String seiName)
   {
      this.seiName = seiName;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setPortName(java.lang.String)
    */
   public void setPortName(String portName)
   {
      this.portName = portName;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setServiceName(java.lang.String)
    */
   public void setServiceName(String serviceName)
   {
      this.serviceName = serviceName;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setEjbLink(java.lang.String)
    */
   public void setEjbLink(String ejbLink)
   {
      this.ejbLink = ejbLink;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setServletLink(java.lang.String)
    */
   public void setServletLink(String servletLink)
   {
      this.servletLink = servletLink;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setMappingFile(java.lang.String)
    */
   public void setMappingFile(String mappingFile)
   {
      this.mappingFile = mappingFile;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#setWsdlFile(java.lang.String)
    */
   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public void setAppend(boolean append)
   {
      this.append = append;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDotXMLCreatorIntf#generateWSXMLDescriptor(java.io.File)
    */
   public void generateWSXMLDescriptor(File file) throws IOException
   {
      WebservicesMetaData webservices = constructWSMetaData();

      Element webservicesElem;
      if (append && file.exists())
      {
         // append generated webservice descriptions to existing file
         // parse existing file
         InputStream wsxmlStream = new FileInputStream(file);
         webservicesElem = DOMUtils.parse(wsxmlStream);
         wsxmlStream.close();

         // obtain <webservice-description> subelements and append them to <webservices>
         Document webservicesDoc = webservicesElem.getOwnerDocument();
         for (WebserviceDescriptionMetaData wsdescription : webservices.getWebserviceDescriptions())
         {
            String wsdescriptionString = wsdescription.serialize();
            Element wsdescriptionElem = DOMUtils.parse(wsdescriptionString);
            webservicesElem.appendChild(webservicesDoc.importNode(wsdescriptionElem, true));
         }
      }
      else
      {
         // write generated webservices descriptor to new file
         String wmdata = webservices.serialize();
         webservicesElem = DOMUtils.parse(wmdata);
      }

      FileWriter fw = new FileWriter(file);
      fw.write(DOMWriter.printNode(webservicesElem, true));
      fw.close();
   }

   //PRIVATE METHODS

   private WebservicesMetaData constructWSMetaData()
   {
      WebservicesMetaData wm = new WebservicesMetaData();
      WebserviceDescriptionMetaData wsdm = new WebserviceDescriptionMetaData(wm);
      populateWebserviceDescriptionMetaData(wsdm);
      wm.addWebserviceDescription(wsdm);
      return wm;
   }

   private void populateWebserviceDescriptionMetaData(WebserviceDescriptionMetaData wsdm)
   {
      checkEssentials();
      wsdm.setWebserviceDescriptionName(this.serviceName);
      wsdm.setWsdlFile(this.wsdlFile);
      wsdm.setJaxrpcMappingFile(this.mappingFile);
      PortComponentMetaData pm1 = new PortComponentMetaData(wsdm);
      pm1.setPortComponentName(portName);
      pm1.setWsdlPort(new QName(this.targetNamespace, portName, "impl"));
      pm1.setServiceEndpointInterface(seiName);
      if (this.servletLink != null && this.servletLink.length() > 0)
         pm1.setServletLink(this.servletLink);
      else pm1.setEjbLink(this.ejbLink);
      wsdm.addPortComponent(pm1);
   }

   private void checkEssentials()
   {
      if (serviceName == null)
         throw new WSException("serviceName is null");
      if (wsdlFile == null)
         throw new WSException("wsdlFile is null");
      if (mappingFile == null)
         throw new WSException("mappingFile is null");
      if (targetNamespace == null)
         throw new WSException("targetNamespace is null");
      if (portName == null)
         throw new WSException("portName is null");
      if (seiName == null)
         throw new WSException("seiName is null");
      if (servletLink == null && ejbLink == null)
         throw new WSException("Either servletLink or ejbLink should not be null");
      if (servletLink != null && ejbLink != null)
         throw new WSException("One of servletLink or ejbLink should be null");
   }
}
