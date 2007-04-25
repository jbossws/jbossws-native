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
package org.jboss.ws.core.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.core.utils.IOUtils;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.integration.Endpoint;
import org.w3c.dom.Element;

/**
 * The publisher for web service endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public abstract class AbstractServiceEndpointPublisher
{
   // logging support
   private static Logger log = Logger.getLogger(AbstractServiceEndpointPublisher.class);

   // The default bean name
   public static final String BEAN_NAME = "ServiceEndpointPublisher";

   // The configured service endpoint servlet
   private String servletClass;

   // The results of the URL rewriting
   public class RewriteResults
   {
      // The URL to the rewrittn web.xml
      public URL webXML;
      // Map<servlet-name, servlet-class> the servlet-class enties are the implementation beans 
      public Map<String, String> sepTargetMap = new HashMap<String, String>();
   }

   public String getServletClass()
   {
      return servletClass;
   }

   public void setServletClass(String servletClass)
   {
      this.servletClass = servletClass;
   }

   public String publishServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      throw new NotImplementedException("Subclass should overwrite this method");
   }

   public String destroyServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      throw new NotImplementedException("Subclass should overwrite this method");
   }

   public RewriteResults rewriteWebXml(UnifiedDeploymentInfo udi)
   {
      URL warURL = udi.webappURL;
      File warFile = new File(warURL.getFile());
      if (warFile.isDirectory() == false)
         throw new WSException("Expected a war directory: " + warURL);

      File webXML = new File(warURL.getFile() + "/WEB-INF/web.xml");
      if (webXML.isFile() == false)
         throw new WSException("Cannot find web.xml: " + webXML);

      try
      {
         // After redeployment there might be a stale copy of the original web.xml.org, we delete it
         File orgWebXML = new File(webXML.getCanonicalPath() + ".org");
         orgWebXML.delete();

         // Rename the web.xml
         if (webXML.renameTo(orgWebXML) == false)
            throw new WSException("Cannot rename web.xml: " + orgWebXML);

         FileInputStream stream = new FileInputStream(orgWebXML);
         return rewriteWebXml(stream, webXML, udi.classLoader);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSException(e);
      }
   }

   public RewriteResults rewriteWebXml(InputStream source, File dest, ClassLoader loader) throws Exception
   {
      if (dest == null)
      {
         dest = File.createTempFile("jbossws-alt-web", "xml", IOUtils.createTempDirectory());
         dest.deleteOnExit();
      }

      Element root = DOMUtils.parse(source);
      RewriteResults results = modifyServletConfig(root, loader);
      results.webXML = dest.toURL();

      FileOutputStream fos = new FileOutputStream(dest);
      new DOMWriter(fos).setPrettyprint(true).print(root);
      fos.flush();
      fos.close();

      return results;
   }

   private RewriteResults modifyServletConfig(Element root, ClassLoader loader) throws ClassNotFoundException
   {
      RewriteResults results = new RewriteResults();
      Iterator itServlets = DOMUtils.getChildElements(root, "servlet");
      while (itServlets.hasNext())
      {
         Element servletElement = (Element)itServlets.next();
         String linkName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(servletElement, "servlet-name"));

         // find the servlet-class
         Element classElement = DOMUtils.getFirstChildElement(servletElement, "servlet-class");

         // JSP
         if (classElement == null)
            continue;

         String orgServletClassName = DOMUtils.getTextContent(classElement).trim();

         // Get the servlet class
         Class orgServletClass = null;
         if (loader != null)
         {
            try
            {
               orgServletClass = loader.loadClass(orgServletClassName);
            }
            catch (ClassNotFoundException ex)
            {
               log.warn("Cannot load servlet class: " + orgServletClassName);
            }
         }

         String targetBeanName = null;

         // Nothing to do if we have an <init-param>
         if (isAlreadyModified(servletElement))
         {
            Iterator itParams = DOMUtils.getChildElements(servletElement, "init-param");
            while (itParams.hasNext())
            {
               Element elParam = (Element)itParams.next();
               Element elParamName = DOMUtils.getFirstChildElement(elParam, "param-name");
               Element elParamValue = DOMUtils.getFirstChildElement(elParam, "param-value");
               if (Endpoint.SEPID_DOMAIN_ENDPOINT.equals(DOMUtils.getTextContent(elParamName)))
               {
                  targetBeanName = DOMUtils.getTextContent(elParamValue);
               }
            }
         }
         else
         {
            // Check if it is a real servlet that we can ignore
            if (orgServletClass != null && JavaUtils.isAssignableFrom(Servlet.class, orgServletClass))
            {
               log.info("Ignore servlet: " + orgServletClassName);
               continue;
            }
            else if (orgServletClassName.endsWith("Servlet"))
            {
               log.info("Ignore <servlet-class> that ends with 'Servlet': " + orgServletClassName);
               continue;
            }

            // build a list of detached elements that come after <servlet-class>
            boolean startDetach = false;
            List<Element> detachedElements = new ArrayList<Element>();
            Iterator itDetached = DOMUtils.getChildElements(servletElement);
            while (itDetached.hasNext())
            {
               Element el = (Element)itDetached.next();
               if (startDetach == true)
               {
                  detachedElements.add(el);
                  servletElement.removeChild(el);
               }
               if (el.equals(classElement))
               {
                  servletElement.removeChild(el);
                  startDetach = true;
               }
            }

            // replace the class name
            classElement = (Element)DOMUtils.createElement("servlet-class");
            classElement.appendChild(DOMUtils.createTextNode(servletClass));
            classElement = (Element)servletElement.getOwnerDocument().importNode(classElement, true);
            servletElement.appendChild(classElement);

            // add additional init params
            if (orgServletClassName.equals(servletClass) == false)
            {
               Element paramElement = DOMUtils.createElement("init-param");
               paramElement.appendChild(DOMUtils.createElement("param-name")).appendChild(DOMUtils.createTextNode(Endpoint.SEPID_DOMAIN_ENDPOINT));
               paramElement.appendChild(DOMUtils.createElement("param-value")).appendChild(DOMUtils.createTextNode(orgServletClassName));
               paramElement = (Element)servletElement.getOwnerDocument().importNode(paramElement, true);
               servletElement.appendChild(paramElement);
               targetBeanName = orgServletClassName;
            }

            // reattach the elements
            itDetached = detachedElements.iterator();
            while (itDetached.hasNext())
            {
               Element el = (Element)itDetached.next();
               servletElement.appendChild(el);
            }
         }

         if (targetBeanName == null)
            throw new IllegalStateException("Cannot obtain service endpoint bean for: " + linkName);

         // remember the target bean name
         results.sepTargetMap.put(linkName, targetBeanName.trim());
      }

      return results;
   }

   // Return true if the web.xml is already modified
   private boolean isAlreadyModified(Element servletElement)
   {
      Iterator itParams = DOMUtils.getChildElements(servletElement, "init-param");
      while (itParams.hasNext())
      {
         Element elParam = (Element)itParams.next();
         Element elParamName = DOMUtils.getFirstChildElement(elParam, "param-name");
         if (Endpoint.SEPID_DOMAIN_ENDPOINT.equals(DOMUtils.getTextContent(elParamName)))
            return true;
      }
      return false;
   }
}