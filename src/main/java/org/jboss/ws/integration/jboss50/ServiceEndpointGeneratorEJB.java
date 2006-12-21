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
package org.jboss.ws.integration.jboss50;

//$Id$

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.ServerConfig;
import org.jboss.ws.core.server.ServerConfigFactory;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.w3c.dom.Element;

/**
 * Generate a web deployment for EJB endpoints 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public abstract class ServiceEndpointGeneratorEJB
{
   // logging support
   protected Logger log = Logger.getLogger(ServiceEndpointGeneratorEJB.class);

   public URL generatWebDeployment(UnifiedMetaData wsMetaData, DeploymentUnit unit)
   {
      // Collect the list of EndpointMetaData
      List<EndpointMetaData> epMetaDataList = new ArrayList<EndpointMetaData>();
      for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
      {
         for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
         {
            epMetaDataList.add(epMetaData);
         }
      }

      Element webDoc = createWebAppDescriptor(epMetaDataList, unit);
      Element jbossDoc = createJBossWebAppDescriptor(epMetaDataList, unit);

      File tmpWar = null;
      try
      {
         ServerConfigFactory factory = ServerConfigFactory.getInstance();
         ServerConfig config = factory.getServerConfig();
         File tmpdir = new File(config.getServerTempDir().getCanonicalPath() + "/deploy");

         String deploymentName = wsMetaData.getDeploymentName().replace('/', '-');
         tmpWar = File.createTempFile(deploymentName, ".war", tmpdir);
         tmpWar.delete();
         
         File webInf = new File(tmpWar, "WEB-INF");
         webInf.mkdirs();

         File webXml = new File(webInf, "web.xml");
         FileWriter fw = new FileWriter(webXml);
         new DOMWriter(fw).setPrettyprint(true).print(webDoc);
         fw.close();

         File jbossWebXml = new File(webInf, "jboss-web.xml");
         fw = new FileWriter(jbossWebXml);
         new DOMWriter(fw).setPrettyprint(true).print(jbossDoc);
         fw.close();
         
         return tmpWar.toURL();
      }
      catch (IOException e)
      {
         throw new WSException("Failed to create webservice.war", e);
      }
   }

   private Element createWebAppDescriptor(List<EndpointMetaData> epMetaDataList, DeploymentUnit unit)
   {
      Element webApp = DOMUtils.createElement("web-app");

      /*
       <servlet>
       <servlet-name>
       <servlet-class>
       </servlet>
       */
      for (EndpointMetaData epMetaData : epMetaDataList)
      {
         ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
         String ejbName = sepMetaData.getLinkName();
         Element servlet = (Element)webApp.appendChild(DOMUtils.createElement("servlet"));
         Element servletName = (Element)servlet.appendChild(DOMUtils.createElement("servlet-name"));
         servletName.appendChild(DOMUtils.createTextNode(ejbName));
         Element servletClass = (Element)servlet.appendChild(DOMUtils.createElement("servlet-class"));
         String targetBean = sepMetaData.getServiceEndpointImplName();
         String seiName = sepMetaData.getServiceEndpointInterfaceName();
         String servletClassName = (targetBean != null ? targetBean : seiName);
         servletClass.appendChild(DOMUtils.createTextNode(servletClassName));
      }

      /*
       <servlet-mapping>
       <servlet-name>
       <url-pattern>
       </servlet-mapping>
       */
      ArrayList urlPatters = new ArrayList();
      for (EndpointMetaData epMetaData : epMetaDataList)
      {
         ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
         String ejbName = sepMetaData.getLinkName();
         Element servletMapping = (Element)webApp.appendChild(DOMUtils.createElement("servlet-mapping"));
         Element servletName = (Element)servletMapping.appendChild(DOMUtils.createElement("servlet-name"));
         servletName.appendChild(DOMUtils.createTextNode(ejbName));
         Element urlPatternElement = (Element)servletMapping.appendChild(DOMUtils.createElement("url-pattern"));

         String urlPattern = "/*";
         if (sepMetaData.getURLPattern() != null)
         {
            urlPattern = sepMetaData.getURLPattern();
         }

         if (urlPatters.contains(urlPattern))
            throw new IllegalArgumentException("Cannot use the same url-pattern with different endpoints, " + "check your <port-component-uri> in jboss.xml");

         urlPatternElement.appendChild(DOMUtils.createTextNode(urlPattern));
         urlPatters.add(urlPattern);
      }

      String authMethod = null;

      // Add web-app/security-constraint for each port component
      for (EndpointMetaData epMetaData : epMetaDataList)
      {
         ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
         String ejbName = sepMetaData.getLinkName();
         if (sepMetaData.getAuthMethod() != null || sepMetaData.getTransportGuarantee() != null)
         {
            /*
             <security-constraint>
             <web-resource-collection>
             <web-resource-name>TestUnAuthPort</web-resource-name>
             <url-pattern>/HSTestRoot/TestUnAuth/*</url-pattern>
             </web-resource-collection>
             <auth-constraint>
             <role-name>*</role-name>
             </auth-constraint>
             <user-data-constraint>
             <transport-guarantee>NONE</transport-guarantee>
             </user-data-constraint>
             </security-constraint>
             */
            Element securityConstraint = (Element)webApp.appendChild(DOMUtils.createElement("security-constraint"));
            Element wrc = (Element)securityConstraint.appendChild(DOMUtils.createElement("web-resource-collection"));
            Element wrName = (Element)wrc.appendChild(DOMUtils.createElement("web-resource-name"));
            wrName.appendChild(DOMUtils.createTextNode(ejbName));
            Element pattern = (Element)wrc.appendChild(DOMUtils.createElement("url-pattern"));
            String uri = sepMetaData.getURLPattern();
            pattern.appendChild(DOMUtils.createTextNode(uri));
            Element method = (Element)wrc.appendChild(DOMUtils.createElement("http-method"));
            method.appendChild(DOMUtils.createTextNode("GET"));
            method = (Element)wrc.appendChild(DOMUtils.createElement("http-method"));
            method.appendChild(DOMUtils.createTextNode("POST"));

            // Optional auth-constraint
            if (sepMetaData.getAuthMethod() != null)
            {
               // Only the first auth-method gives the war login-config/auth-method
               if (authMethod == null)
                  authMethod = sepMetaData.getAuthMethod();

               Element authConstraint = (Element)securityConstraint.appendChild(DOMUtils.createElement("auth-constraint"));
               Element roleName = (Element)authConstraint.appendChild(DOMUtils.createElement("role-name"));
               roleName.appendChild(DOMUtils.createTextNode("*"));
            }
            // Optional user-data-constraint
            if (sepMetaData.getTransportGuarantee() != null)
            {
               Element userData = (Element)securityConstraint.appendChild(DOMUtils.createElement("user-data-constraint"));
               Element transport = (Element)userData.appendChild(DOMUtils.createElement("transport-guarantee"));
               transport.appendChild(DOMUtils.createTextNode(sepMetaData.getTransportGuarantee()));
            }
         }
      }

      // Optional login-config/auth-method
      if (authMethod != null)
      {
         Element loginConfig = (Element)webApp.appendChild(DOMUtils.createElement("login-config"));
         Element method = (Element)loginConfig.appendChild(DOMUtils.createElement("auth-method"));
         method.appendChild(DOMUtils.createTextNode(authMethod));
         Element realm = (Element)loginConfig.appendChild(DOMUtils.createElement("realm-name"));
         realm.appendChild(DOMUtils.createTextNode("EJBServiceEndpointServlet Realm"));

         addEJBSecurityRoles(webApp, unit);
      }

      return webApp;
   }

   private Element createJBossWebAppDescriptor(List<EndpointMetaData> epMetaDataList, DeploymentUnit unit)
   {
      /* Create a jboss-web
       <jboss-web>
       <security-domain>java:/jaas/cts</security-domain>
       <context-root>/ws/ejbN/</context-root>
       <virtual-host>some.domain.com</virtual-host>
       </jboss-web>
       */
      Element jbossWeb = DOMUtils.createElement("jboss-web");

      UnifiedMetaData wsMetaData = epMetaDataList.get(0).getServiceMetaData().getUnifiedMetaData();
      String securityDomain = wsMetaData.getSecurityDomain();
      if (securityDomain != null)
      {
         Element secDomain = (Element)jbossWeb.appendChild(DOMUtils.createElement("security-domain"));
         secDomain.appendChild(DOMUtils.createTextNode("java:/jaas/" + securityDomain));
      }

      // Get the context root for this deployment
      String contextRoot = null;
      for (EndpointMetaData epMetaData : epMetaDataList)
      {
         ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
         String next = sepMetaData.getContextRoot();
         if (next != null)
         {
            if (contextRoot == null)
            {
               contextRoot = next;
            }
            else if (contextRoot.equals(next) == false)
            {
               throw new WSException("Multiple context root not supported");
            }
         }
      }
      if (contextRoot == null)
         throw new WSException("Cannot obtain context root");

      Element root = (Element)jbossWeb.appendChild(DOMUtils.createElement("context-root"));
      root.appendChild(DOMUtils.createTextNode(contextRoot));

      String[] virtualHosts = null;
      for (EndpointMetaData epMetaData : epMetaDataList)
      {
         ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
         String[] next = sepMetaData.getVirtualHosts();
         if (next != null && next.length > 0)
         {
            Arrays.sort(next);
            if (virtualHosts == null)
            {
               virtualHosts = next;
            }
            else
            {
               if (Arrays.equals(virtualHosts, next) == false)
               {
                  throw new WSException("All endpoints must define the same virtual hosts");
               }
            }
         }
      }

      if (virtualHosts != null)
      {
         for (String current : virtualHosts)
         {
            Element virtualHost = (Element)jbossWeb.appendChild(DOMUtils.createElement("virtual-host"));
            virtualHost.appendChild(DOMUtils.createTextNode(current));
         }
      }      
      
      
      return jbossWeb;
   }

   /** Add the roles from ejb-jar.xml to the security roles
    */
   protected abstract void addEJBSecurityRoles(Element webApp, DeploymentUnit unit);
}
