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
package org.jboss.ws.tools.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import javax.wsdl.xml.WSDLLocator;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.ResourceURL;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/* A WSDLLocator that can handle wsdl imports
 */
class WSDLLocatorImpl implements WSDLLocator
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(WSDLLocatorImpl.class);
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLDefinitionsFactory.class);

   private EntityResolver entityResolver;
   private URL wsdlLocation;
   private String latestImportURI;

   public WSDLLocatorImpl(EntityResolver entityResolver, URL wsdlLocation)
   {
      if (wsdlLocation == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "WSDL_FILE_ARGUMENT_CANNOT_BE_NULL"));

      this.entityResolver = entityResolver;
      this.wsdlLocation = wsdlLocation;
   }

   public InputSource getBaseInputSource()
   {
      log.trace("getBaseInputSource [wsdlUrl=" + wsdlLocation + "]");
      try
      {
         InputStream inputStream = new ResourceURL(wsdlLocation).openStream();
         if (inputStream == null)
            throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_WSDL",  wsdlLocation ));

         return new InputSource(inputStream);
      }
      catch (IOException e)
      {
         throw new RuntimeException(BundleUtils.getMessage(bundle, "CANNOT_ACCESS_WSDL", new Object[]{ wsdlLocation ,  e.getMessage()}));
      }
   }

   public String getBaseURI()
   {
      return wsdlLocation.toExternalForm();
   }

   public InputSource getImportInputSource(String parent, String resource)
   {
      log.trace("getImportInputSource [parent=" + parent + ",resource=" + resource + "]");

      URL parentURL = null;
      try
      {
         parentURL = new URL(parent);
      }
      catch (MalformedURLException e)
      {
         log.error(BundleUtils.getMessage(bundle, "NOT_A_VALID_URL",  parent));
         return null;
      }

      String wsdlImport = null;
      String external = parentURL.toExternalForm();

      // An external URL
      if (resource.startsWith("http://") || resource.startsWith("https://"))
      {
         // [JBWS-3139] there's a bug in wsdl4j 1.6.2 where imported schemas are containing invalid IPv6 host name values :(
         // The URL value of root WSDL is not malformed, so we're reusing it for schemas URL construction.
         if (resource.indexOf(parentURL.getFile()) != -1) 
         {
            URI uri = null;
            try {
               uri = new URI(resource);
            }
            catch (Exception e)
            {
               throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_RESOLVE_IMPORTED_RESOURCE",  resource));
            }
            final String path = uri.getPath();
            final String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
            wsdlImport = parentURL.getProtocol() + "://" + parentURL.getHost() + ":" + parentURL.getPort() + path + query; 
         }
         else
         {
            wsdlImport = resource;
         }
      }

      // Absolute path
      else if (resource.startsWith("/"))
      {
         String beforePath = external.substring(0, external.indexOf(parentURL.getPath()));
         wsdlImport = beforePath + resource;
      }

      // A relative path
      else
      {
         String parentDir = external.substring(0, external.lastIndexOf("/"));

         // remove references to current dir
         while (resource.startsWith("./"))
            resource = resource.substring(2);

         // remove references to parentdir
         while (resource.startsWith("../"))
         {
            parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"));
            resource = resource.substring(3);
         }

         wsdlImport = parentDir + "/" + resource;
      }

      try
      {
         log.trace("Trying to resolve: " + wsdlImport);
         InputSource inputSource = entityResolver.resolveEntity(wsdlImport, wsdlImport);
         if (inputSource != null)
         {
            latestImportURI = wsdlImport;
         }
         else
         {
            throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_RESOLVE_IMPORTED_RESOURCE",  wsdlImport));
         }

         return inputSource;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_ACCESS_IMPORTED_WSDL", new Object[]{ wsdlImport ,  e.getMessage()}));
      }
   }

   public String getLatestImportURI()
   {
      return latestImportURI;
   }

   public void close()
   {
   }
}
