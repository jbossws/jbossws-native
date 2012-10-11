/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.ws.NativeMessages;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.spi.management.ServerConfig;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the delivery of the WSDL and its included artifacts.
 * It rewrites the include URL's.
 *
 * http://www.jboss.org/index.html?module=bb&op=viewtopic&p=3871263#3871263
 *
 * For a discussion of this topic.
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * 
 * @since 23-Mar-2005
 */
public class WSDLRequestHandler
{
   // provide logging
   private static Logger log = Logger.getLogger(WSDLRequestHandler.class);

   private final URL wsdlLocation;
   private final String wsdlPublishLoc;
   private final ServerConfig config;

   public WSDLRequestHandler(URL wsdlLocationFromMetadata, String wsdlPublishLocationFromMetadata, ServerConfig config)
   {
      if (wsdlLocationFromMetadata == null)
         throw NativeMessages.MESSAGES.illegalNullArgument("wsdlLocationFromMetadata");
      this.wsdlLocation = wsdlLocationFromMetadata;
      this.wsdlPublishLoc = wsdlPublishLocationFromMetadata;
      this.config = config;
   }
   
   public Document getDocumentForPath(URL reqURL, String resPath) throws IOException
   {
      String wsdlHost = reqURL.getHost();
      boolean rewriteUsingCalledURL = ServerConfig.UNDEFINED_HOSTNAME.equals(config.getWebServiceHost());

      if (!rewriteUsingCalledURL)
      {
         wsdlHost = config.getWebServiceHost();
      }

      if (log.isDebugEnabled())
         log.debug("WSDL request, using host: " + wsdlHost);
      
      return getDocumentForPath(reqURL, wsdlHost, rewriteUsingCalledURL, resPath);
   }
   
   protected InputStream openStreamToWSDL() throws IOException
   {
      return wsdlLocation.openStream();
   }

   /**
    * Get the WSDL resource for a given resource path
    * <p/>
    * Use path value of null to get the root document
    *
    * @param reqURL   The full request url
    * @param wsdlHost The host to be used for address rewrite in the wsdl
    * @param rewriteUsingCalledURL True if the called url is being used to get the wsdlHost (and the port to use for relative addresses in import/include elements) 
    * @param resPath The wsdl resource to get, can be null for the top level wsdl
    * @return A wsdl document, or null if it cannot be found
    */
   private Document getDocumentForPath(URL reqURL, String wsdlHost, boolean rewriteUsingCalledURL, String resPath) throws IOException
   {
      Document wsdlDoc;
      // get the root wsdl
      if (resPath == null)
      {
         InputStream is = null;
         try
         {
            is = openStreamToWSDL();
            Element wsdlElement = DOMUtils.parse(is);
            wsdlDoc = wsdlElement.getOwnerDocument();
         }
         finally
         {
            try
            {
               is.close();
            }
            catch (Exception e)
            {
               //ignore
            }
         }
      }

      // get some imported resource
      else
      {
         File wsdlLocFile = new File(wsdlLocation.getPath());
         String impResourcePath = wsdlLocFile.getParent() + File.separatorChar + resPath;
         File impResourceFile = new File(impResourcePath);

         if (log.isDebugEnabled())
            log.debug("Importing resource file: " + impResourceFile.getCanonicalPath());

         String wsdlLocFilePath = wsdlLocFile.getParentFile().getCanonicalPath();
         String wsdlDataLoc = config.getServerDataDir().getCanonicalPath() + File.separatorChar + "wsdl";

         //allow wsdl file's parent or server's data/wsdl or overriden wsdl publish directories only
         String resourceAbsPath = impResourceFile.getCanonicalPath(); 
         if (resourceAbsPath.indexOf(wsdlLocFilePath) >= 0
             || resourceAbsPath.indexOf(wsdlDataLoc) >= 0
             || (wsdlPublishLoc != null 
                  && resourceAbsPath.indexOf(new File(new URL(wsdlPublishLoc).getPath()).getCanonicalPath()) >= 0))
         {
            Element wsdlElement = DOMUtils.parse(impResourceFile.toURL().openStream());
            wsdlDoc = wsdlElement.getOwnerDocument();
         }
         else
         {
            throw NativeMessages.MESSAGES.accessIsNotAllowed(resourceAbsPath);
         }
      }

      modifyAddressReferences(reqURL, wsdlHost, rewriteUsingCalledURL, resPath, wsdlDoc.getDocumentElement());
      return wsdlDoc;
   }

   /**
    * Modify the location of wsdl and schema imports
    */
   private void modifyAddressReferences(URL reqURL, String wsdlHost, boolean rewriteUsingCalledURL, String resPath, Element element) throws IOException
   {
      // map wsdl definition imports
      NodeList nlist = element.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node childNode = nlist.item(i);
         if (childNode.getNodeType() == Node.ELEMENT_NODE)
         {
            Element childElement = (Element)childNode;
            String nodeName = childElement.getLocalName();

            // Replace xsd:import and xsd:include location attributes
            if ("import".equals(nodeName) || "include".equals(nodeName))
            {
               Attr locationAttr = childElement.getAttributeNode("schemaLocation");
               if (locationAttr == null)
                  locationAttr = childElement.getAttributeNode("location");

               if (locationAttr != null)
               {
                  String orgLocation = locationAttr.getNodeValue();
                  
                  while (orgLocation.startsWith("./"))
                     orgLocation = orgLocation.substring(2);
                  
                  boolean isAbsolute = orgLocation.startsWith("http://") || orgLocation.startsWith("https://");
                  if (isAbsolute == false && orgLocation.startsWith(reqURL.getPath()) == false)
                  {
                     String newResourcePath = orgLocation;

                     if (resPath != null && resPath.indexOf("/") > 0)
                     {
                        String resParent = resPath.substring(0, resPath.lastIndexOf("/"));

                        // replace parent traversal, results in resParent == null when successfully executed
                        if (orgLocation.startsWith("../") && resParent != null)
                        {
                           // replace parent traversal, results in resParent == null when successfully executed
                           while (orgLocation.startsWith("../") && resParent != null)
                           {
                              if (resParent.endsWith(".."))
                              {
                                 newResourcePath = resParent + "/" + orgLocation;
                                 resParent = null;
                              }
                              else if (resParent.indexOf("/") > 0)
                              {
                                 resParent = resParent.substring(0, resParent.lastIndexOf("/"));
                                 orgLocation = orgLocation.substring(3);
                                 newResourcePath = resParent + "/" + orgLocation;
                              }
                              else
                              {
                                 orgLocation = orgLocation.substring(3);
                                 newResourcePath = orgLocation;
                                 resParent = null;
                              }
                           }

                        }
                        else
                        {
                           newResourcePath = resParent + "/" + orgLocation;
                        }
                     }

                     String reqPath = reqURL.getPath();
                     String completeHost = wsdlHost;

                     String reqProtocol = reqURL.getProtocol();
                     int reqPort = rewriteUsingCalledURL ? reqURL.getPort() : getPortForProtocol(reqProtocol);
                     String hostAndPort = wsdlHost + (reqPort > 0 ? ":" + reqPort : "");
                     completeHost = reqProtocol + "://" + hostAndPort;

                     String newLocation = completeHost + reqPath + "?wsdl&resource=" + newResourcePath;
                     locationAttr.setNodeValue(newLocation);

                     log.trace("Mapping import from '" + orgLocation + "' to '" + newLocation + "'");
                  }
               }
            }

            // Replace the soap:address location attribute
            else if ("address".equals(nodeName))
            {
               Attr locationAttr = childElement.getAttributeNode("location");
               if (locationAttr != null)
               {
                  String orgLocation = locationAttr.getNodeValue();

                  if (isHttp(orgLocation))
                  {
                     URL orgURL = new URL(orgLocation);
                     String orgProtocol = orgURL.getProtocol();
                     String host = orgURL.getHost();
                     final boolean rewriteLocation =
                        ServerConfig.UNDEFINED_HOSTNAME.equals(host) ||
                        this.config.isModifySOAPAddress();

                     if (rewriteLocation)
                     {
                        //we stick with the original protocol (https) if the transport guarantee is CONFIDENTIAL
                        //(if the original wsdl soap:address uses https we can't overwrite it with http)
                        boolean confidential = "https".equalsIgnoreCase(orgProtocol);
                        String reqProtocol = reqURL.getProtocol();
                        
                        int port;
                        if (rewriteUsingCalledURL)
                        {
                           port = reqURL.getPort();
                        }
                        else
                        {
                           port = confidential ? getPortForProtocol("https") : getPortForProtocol(reqProtocol);
                        }
                        String path = orgURL.getPath();
                        String newLocation = new URL(confidential ? "https" : reqProtocol, wsdlHost, port, path).toString();
                        if (!newLocation.equals(orgLocation))
                        {
                           locationAttr.setNodeValue(newLocation);
                           if (log.isDebugEnabled())
                              log.debug("Mapping address from '" + orgLocation + "' to '" + newLocation + "'");
                        }
                     }
                  }
               }
            }
            else
            {
               modifyAddressReferences(reqURL, wsdlHost, rewriteUsingCalledURL, resPath, childElement);
            }
         }
      }
   }
   
   
   private static boolean isHttp(String orgLocation)
   {
      try
      {
         String scheme = new URI(orgLocation).getScheme();
         if (scheme != null && scheme.startsWith("http"))
         {
            return true;
         }
         else
         {
            log.debug("Skipping rewrite of non-http address: " + orgLocation);
            return false;
         }
      }
      catch (URISyntaxException e)
      {
         log.debug("Skipping rewrite of non-http address: " + orgLocation);
         return false;
      }
   }

   /**
    * Returns real http and https protocol values. Returns -1 for non http(s) protocols.
    *
    * @param protocol to handle
    * @return real http(s) value, or -1 if not http(s) protocol
    */
   private int getPortForProtocol( final String protocol )
   {
      final String lowerCasedProtocol = protocol.toLowerCase();

      if ( "http".equals( lowerCasedProtocol ) )
      {
         return config.getWebServicePort();
      }
      else if ( "https".equals( lowerCasedProtocol ) )
      {
         return config.getWebServiceSecurePort();
      }

      return -1;
   }

}
