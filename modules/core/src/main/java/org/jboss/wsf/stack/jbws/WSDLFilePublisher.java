/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.jbws;

import static org.jboss.ws.NativeLoggers.ROOT_LOGGER;
import static org.jboss.ws.NativeMessages.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.wsdl.Definition;

import org.jboss.ws.common.Constants;
import org.jboss.ws.common.IOUtils;
import org.jboss.ws.common.utils.AbstractWSDLFilePublisher;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLWriter;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.w3c.dom.Document;

/** A helper class that publishes the wsdl files and their imports to the server/data/wsdl directory.
 *
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 * @author Thomas.Diesler@jboss.org
 * @since 02-June-2004
 */
public class WSDLFilePublisher extends AbstractWSDLFilePublisher
{
   public WSDLFilePublisher(ArchiveDeployment dep)
   {
      super(dep);
   }

   /** Publish the deployed wsdl file to the data directory
    */
   public void publishWsdlFiles(UnifiedMetaData wsMetaData) throws IOException
   {
      String deploymentName = dep.getCanonicalName();

      // For each service
      for (ServiceMetaData serviceMD : wsMetaData.getServices())
      {
         final String wsdlLocation = this.getWSDLLocation(serviceMD);
         final String publishLocation = serviceMD.getWsdlPublishLocation();
         final File wsdlFile = getPublishLocation(deploymentName, wsdlLocation, publishLocation);
         if (wsdlFile == null)
            continue;

         wsdlFile.getParentFile().mkdirs();

         // Get the wsdl definition and write it to the wsdl publish location
         Writer fWriter = null;
         try
         {
            fWriter = IOUtils.getCharsetFileWriter(wsdlFile, Constants.DEFAULT_XML_CHARSET);
            WSDLDefinitions wsdlDefinitions = serviceMD.getWsdlDefinitions();
            new WSDLWriter(wsdlDefinitions).write(fWriter, Constants.DEFAULT_XML_CHARSET);

            URL wsdlPublishURL = wsdlFile.toURI().toURL();
            ROOT_LOGGER.wsdlFilePublished(wsdlPublishURL);

            // udpate the wsdl file location 
            serviceMD.setWsdlLocation(wsdlPublishURL);

            // Process the wsdl imports
            Definition wsdl11Definition = wsdlDefinitions.getWsdlOneOneDefinition();
            if (wsdl11Definition != null)
            {
               List<String> published = new LinkedList<String>();
               publishWsdlImports(wsdlPublishURL, wsdl11Definition, published);

               // Publish XMLSchema imports
               Document document = wsdlDefinitions.getWsdlDocument();
               publishSchemaImports(wsdlPublishURL, document.getDocumentElement(), published);
            }
            else
            {
               throw MESSAGES.wsdl20NotSupported();
            }
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception e)
         {
            throw MESSAGES.cannotPublishWSDLTo(wsdlFile, e);
         }
         finally
         {
            if (fWriter != null)
            {
               fWriter.close();
            }
         }
      }
   }
   
   private String getWSDLLocation(final ServiceMetaData serviceMD)
   {
      if (serviceMD.getWsdlFileOrLocation() == null)
         return null;
      
      return serviceMD.getWsdlFileOrLocation().toExternalForm(); 
   }

   /**
    * Get the file publish location
    */
   private File getPublishLocation(String archiveName, String wsdlLocation, String wsdlPublishLocation) throws IOException
   {
      if (wsdlLocation == null)
      {
         ROOT_LOGGER.cannotGetWsdlPublishLocation();
         return null;
      }

      // Only file URLs are supported in <wsdl-publish-location>
      String publishLocation = wsdlPublishLocation;
      boolean predefinedLocation = publishLocation != null && publishLocation.startsWith("file:");

      File locationFile = null;
      if (predefinedLocation == false)
      {
         //JBWS-2829: windows issue
         if (archiveName.startsWith("http://")) {
             archiveName = archiveName.replace("http://", "http-");         
         }
   
         locationFile = new File(serverConfig.getServerDataDir().getCanonicalPath() + "/wsdl/" + archiveName);
      }
      else
      {
         try
         {
            locationFile = new File(new URL(publishLocation).getPath());
         }
         catch (MalformedURLException e)
         {
            throw MESSAGES.invalidPublishLocation(publishLocation, e);
         }
      }

      File result;
      if (wsdlLocation.indexOf(expLocation) >= 0)
      {
         wsdlLocation = wsdlLocation.substring(wsdlLocation.indexOf(expLocation) + expLocation.length());
         result = new File(locationFile + "/" + wsdlLocation);
      }
      else if (wsdlLocation.startsWith("vfs:") || wsdlLocation.startsWith("vfsfile:")
            || wsdlLocation.startsWith("file:") || wsdlLocation.startsWith("jar:")
            || wsdlLocation.startsWith("vfszip:"))
      {
         wsdlLocation = wsdlLocation.substring(wsdlLocation.lastIndexOf("/") + 1);
         result = new File(locationFile + "/" + wsdlLocation);
      }
      else
      {
         throw MESSAGES.invalidWsdlFile(wsdlLocation, expLocation);
      }

      return result;
   }
}
