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
package org.jboss.ws.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.tools.interfaces.WebservicesXMLCreator;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesFactory;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.w3c.dom.Element;

/**
 * Creates the webservices.xml deployment descriptor
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since   Jun 20, 2005 
 */
public class WebservicesXMLCreatorImpl implements WebservicesXMLCreator
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(WebservicesXMLCreatorImpl.class);
   // provide logging
   protected static final Logger log = Logger.getLogger(WebservicesXMLCreatorImpl.class);
   protected String targetNamespace = null;

   protected String seiName = null;

   protected String portName = null;

   protected String serviceName = null;

   protected String servletLink = null;

   protected String ejbLink = null;

   protected String wsdlFile = null;
   protected String mappingFile = null;

   protected boolean append = false;

   public WebservicesXMLCreatorImpl()
   {
   }

   public void setTargetNamespace(String targetNamespace)
   {
      this.targetNamespace = targetNamespace;
   }

   public void setSeiName(String seiName)
   {
      this.seiName = seiName;
   }

   public void setPortName(String portName)
   {
      this.portName = portName;
   }

   public void setServiceName(String serviceName)
   {
      this.serviceName = serviceName;
   }

   public void setEjbLink(String ejbLink)
   {
      this.ejbLink = ejbLink;
   }

   public void setServletLink(String servletLink)
   {
      this.servletLink = servletLink;
   }

   public void setMappingFile(String mappingFile)
   {
      this.mappingFile = mappingFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public void setAppend(boolean append)
   {
      this.append = append;
   }

   public void generateWSXMLDescriptor(File wsXmlFile) throws IOException
   {
      WebservicesMetaData webservices = constructWSMetaData();

      // handle append flag
      if (append && wsXmlFile.exists())
      {
         WebservicesMetaData existingWebservices;

         // parse existing webservices descriptor
         InputStream wsXmlStream = new FileInputStream(wsXmlFile);
         try
         {
            existingWebservices = WebservicesFactory.parse(wsXmlStream, wsXmlFile.toURI().toURL());
         }
         catch (Exception e)
         {
            throw new WSException(BundleUtils.getMessage(bundle, "COULD_NOT_UNMARSHAL_DESCRIPTOR",  wsXmlFile),  e);
         }
         finally
         {
            wsXmlStream.close();
         }

         // append generated webservice-descriptions to existing descriptor
         for (WebserviceDescriptionMetaData webserviceDescription : webservices.getWebserviceDescriptions())
            existingWebservices.addWebserviceDescription(webserviceDescription);

         webservices = existingWebservices;
      }

      // (re-)write generated webservices descriptor to file
      Element root = DOMUtils.parse(webservices.serialize());
      FileWriter fwriter = new FileWriter(wsXmlFile);
      new DOMWriter(fwriter).setPrettyprint(true).print(root);
      fwriter.close();
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
      pm1.setWsdlPort(new QName(this.targetNamespace, portName, "portNS"));
      pm1.setServiceEndpointInterface(seiName);
      if (this.servletLink != null && this.servletLink.length() > 0)
         pm1.setServletLink(this.servletLink);
      else pm1.setEjbLink(this.ejbLink);
      wsdm.addPortComponent(pm1);
   }

   private void checkEssentials()
   {
      if (serviceName == null)
         throw new WSException(BundleUtils.getMessage(bundle, "SERVICENAME_IS_NULL"));
      if (wsdlFile == null)
         throw new WSException(BundleUtils.getMessage(bundle, "WSDLFILE_IS_NULL"));
      if (mappingFile == null)
         throw new WSException(BundleUtils.getMessage(bundle, "MAPPINGFILE_IS_NULL"));
      if (targetNamespace == null)
         throw new WSException(BundleUtils.getMessage(bundle, "TARGETNAMESPACE_IS_NULL"));
      if (portName == null)
         throw new WSException(BundleUtils.getMessage(bundle, "PORTNAME_IS_NULL"));
      if (seiName == null)
         throw new WSException(BundleUtils.getMessage(bundle, "SEINAME_IS_NULL"));
      if (servletLink == null && ejbLink == null)
         throw new WSException(BundleUtils.getMessage(bundle, "EITHER_SERVLETLINK_OR_EJBLINK_SHOULD_NOT_BE_NULL"));
      if (servletLink != null && ejbLink != null)
         throw new WSException(BundleUtils.getMessage(bundle, "ONE_OF_SERVLETLINK_OR_EJBLINK_SHOULD_BE_NULL"));
   }
}
