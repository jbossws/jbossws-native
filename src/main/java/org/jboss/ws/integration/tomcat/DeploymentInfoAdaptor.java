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
package org.jboss.ws.integration.tomcat;

// $Id: WebServiceDeploymentAdaptor.java 317 2006-05-14 17:16:59Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.jboss.ws.WSException;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebSecurityMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebSecurityMetaData.UnifiedWebResourceCollection;
import org.w3c.dom.Element;

/**
 * Build container independent deployment info. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class DeploymentInfoAdaptor
{
   public static UnifiedDeploymentInfo buildDeploymentInfo(UnifiedDeploymentInfo udi, URLClassLoader loader, ServletContext ctx)
   {
      URL warURL = null;
      try
      {
         warURL = new File(ctx.getRealPath("/")).toURL();
      }
      catch (MalformedURLException e)
      {
         // ignore
      }

      String shortName = getContextRoot(warURL);
      shortName = shortName.substring(1) + ".war";

      udi.simpleName = shortName;
      //udi.url = warURL;
      udi.metaData = buildWebMetaData(udi, ctx);
      udi.classLoader = loader;

      return udi;
   }

   private static UnifiedWebMetaData buildWebMetaData(UnifiedDeploymentInfo udi, ServletContext ctx)
   {
      Element webXML = getWebXMLDocument(ctx);

      UnifiedWebMetaData wmd = new UnifiedWebMetaData();
      wmd.setServletMappings(getServetMappings(webXML));
      wmd.setServletClassNames(getServetClassMap(webXML));
      //wmd.setContextRoot(getContextRoot(udi.url));
      wmd.setConfigName(ctx.getInitParameter("jbossws-config-name"));
      wmd.setConfigFile(ctx.getInitParameter("jbossws-config-file"));
      wmd.setSecurityMetaData(getSecurityMetaData(webXML));

      return wmd;
   }

   private static String getContextRoot(URL warURL)
   {
      String contextRoot = warURL.toExternalForm();
      if (contextRoot.endsWith("/"))
      {
         contextRoot = contextRoot.substring(0, contextRoot.length() - 1);
      }
      contextRoot = contextRoot.substring(contextRoot.lastIndexOf("/"));
      if (contextRoot.endsWith(".war"))
      {
         contextRoot = contextRoot.substring(0, contextRoot.length() - 4);
      }
      return contextRoot;
   }

   private static Map<String, String> getServetMappings(Element root)
   {
      Map<String, String> servletMappings = new HashMap<String, String>();

      Iterator itMapping = DOMUtils.getChildElements(root, "servlet-mapping");
      while (itMapping.hasNext())
      {
         Element smel = (Element)itMapping.next();
         String servletName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(smel, "servlet-name"));
         String urlPattern = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(smel, "url-pattern"));
         servletMappings.put(servletName, urlPattern);
      }

      return servletMappings;
   }

   private static Map<String, String> getServetClassMap(Element root)
   {
      Map<String, String> servletClassMap = new HashMap<String, String>();

      Iterator itServlet = DOMUtils.getChildElements(root, "servlet");
      while (itServlet.hasNext())
      {
         Element sel = (Element)itServlet.next();
         String servletName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(sel, "servlet-name"));

         Iterator itParams = DOMUtils.getChildElements(sel, "init-param");
         while (itParams.hasNext())
         {
            Element ipel = (Element)itParams.next();
            String paramName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(ipel, "param-name"));
            String paramValue = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(ipel, "param-value"));
            if (ServiceEndpointPublisher.INIT_PARAM_SERVICE_ENDPOINT_IMPL.equals(paramName))
            {
               servletClassMap.put(servletName, paramValue);
            }
         }
      }

      return servletClassMap;
   }

   private static List<UnifiedWebSecurityMetaData> getSecurityMetaData(final Element root)
   {
      ArrayList<UnifiedWebSecurityMetaData> securityMetaData = new ArrayList<UnifiedWebSecurityMetaData>();

      Iterator itSecurityConstraint = DOMUtils.getChildElements(root, "security-constraint");
      while (itSecurityConstraint.hasNext())
      {
         UnifiedWebSecurityMetaData uwsmd = new UnifiedWebSecurityMetaData();
         securityMetaData.add(uwsmd);

         Element securityConstraint = (Element)itSecurityConstraint.next();

         Iterator itWebResCol = DOMUtils.getChildElements(securityConstraint, "web-resource-collection");
         while (itWebResCol.hasNext())
         {
            Element webResCol = (Element)itWebResCol.next();
            String webResName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(webResCol, "web-resource-name"));
            UnifiedWebResourceCollection uwrc = uwsmd.addWebResource(webResName);

            Iterator itUrlPatters = DOMUtils.getChildElements(webResCol, "url-pattern");
            while (itUrlPatters.hasNext())
            {
               Element urlPattern = (Element)itUrlPatters.next();
               uwrc.addPattern(DOMUtils.getTextContent(urlPattern));
            }
         }

         Element userData = DOMUtils.getFirstChildElement(securityConstraint, "user-data-constraint");
         if (userData != null)
         {
            String transportGuarantee = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(userData, "transport-guarantee"));
            uwsmd.setTransportGuarantee(transportGuarantee);
         }

      }

      return securityMetaData;
   }

   private static Element getWebXMLDocument(ServletContext ctx)
   {
      URL webXML = null;
      try
      {
         webXML = new File(ctx.getRealPath("/WEB-INF/web.xml")).toURL();
         return DOMUtils.parse(webXML.openStream());
      }
      catch (IOException e)
      {
         throw new WSException("Cannot parse: " + webXML);
      }
   }

}
