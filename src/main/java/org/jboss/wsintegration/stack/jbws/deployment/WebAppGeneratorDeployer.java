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
package org.jboss.wsintegration.stack.jbws.deployment;

//$Id$

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.ws.WSException;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsintegration.spi.deployment.AbstractDeployer;
import org.jboss.wsintegration.spi.deployment.Deployment;
import org.jboss.wsintegration.spi.deployment.SecurityRolesHandler;
import org.jboss.wsintegration.spi.deployment.UnifiedDeploymentInfo;
import org.jboss.wsintegration.spi.management.ServerConfig;
import org.jboss.wsintegration.spi.management.ServerConfigFactory;
import org.jboss.wsintegration.spi.utils.DOMUtils;
import org.jboss.wsintegration.spi.utils.DOMWriter;
import org.w3c.dom.Element;

/**
 * A deployer that generates a webapp for an EJB endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class WebAppGeneratorDeployer extends AbstractDeployer
{
   private SecurityRolesHandler securityRolesHandlerEJB21;
   private SecurityRolesHandler securityRolesHandlerEJB3;

   public void setSecurityRolesHandlerEJB21(SecurityRolesHandler securityRolesHandlerEJB21)
   {
      this.securityRolesHandlerEJB21 = securityRolesHandlerEJB21;
   }

   public void setSecurityRolesHandlerEJB3(SecurityRolesHandler securityRolesHandlerEJB3)
   {
      this.securityRolesHandlerEJB3 = securityRolesHandlerEJB3;
   }

   @Override
   public void create(Deployment dep)
   {
      UnifiedDeploymentInfo udi = dep.getContext().getAttachment(UnifiedDeploymentInfo.class);
      if (udi == null)
         throw new IllegalStateException("Cannot obtain unified deployement info");

      UnifiedMetaData umd = dep.getContext().getAttachment(UnifiedMetaData.class);
      if (umd == null)
         throw new IllegalStateException("Cannot obtain unified meta data");

      if (dep.getType().toString().endsWith("EJB21"))
      {
         udi.webappURL = generatWebDeployment(umd, udi, securityRolesHandlerEJB21);
      }
      else if (dep.getType().toString().endsWith("EJB3"))
      {
         udi.webappURL = generatWebDeployment(umd, udi, securityRolesHandlerEJB3);
      }
   }

   private URL generatWebDeployment(UnifiedMetaData wsMetaData, UnifiedDeploymentInfo udi, SecurityRolesHandler securityHandler)
   {
      // Collect the list of ServerEndpointMetaData
      List<ServerEndpointMetaData> sepMetaDataList = new ArrayList<ServerEndpointMetaData>();
      for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
      {
         for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
         {
            sepMetaDataList.add((ServerEndpointMetaData)epMetaData);
         }
      }

      Element webDoc = createWebAppDescriptor(sepMetaDataList, udi, securityHandler);
      Element jbossDoc = createJBossWebAppDescriptor(sepMetaDataList);

      File tmpWar = null;
      try
      {
         ServerConfig config = ServerConfigFactory.getInstance().getServerConfig();
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

   private Element createWebAppDescriptor(List<ServerEndpointMetaData> sepMetaDataList, UnifiedDeploymentInfo udi, SecurityRolesHandler securityHandler)
   {
      Element webApp = DOMUtils.createElement("web-app");

      /*
       <servlet>
       <servlet-name>
       <servlet-class>
       </servlet>
       */
      for (ServerEndpointMetaData sepMetaData : sepMetaDataList)
      {
         String ejbName = sepMetaData.getLinkName();
         Element servlet = (Element)webApp.appendChild(DOMUtils.createElement("servlet"));
         Element servletName = (Element)servlet.appendChild(DOMUtils.createElement("servlet-name"));
         servletName.appendChild(DOMUtils.createTextNode(ejbName));

         Element servletClass = (Element)servlet.appendChild(DOMUtils.createElement("servlet-class"));
         String implName = sepMetaData.getServiceEndpointImplName();
         String seiName = sepMetaData.getServiceEndpointInterfaceName();
         String endpointTarget = (implName != null ? implName : seiName);
         servletClass.appendChild(DOMUtils.createTextNode(endpointTarget));
      }

      /*
       <servlet-mapping>
       <servlet-name>
       <url-pattern>
       </servlet-mapping>
       */
      ArrayList urlPatters = new ArrayList();
      for (ServerEndpointMetaData sepMetaData : sepMetaDataList)
      {
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
            throw new IllegalArgumentException("Cannot use the same url-pattern with different endpoints, check your <port-component-uri> in jboss.xml");

         urlPatternElement.appendChild(DOMUtils.createTextNode(urlPattern));
         urlPatters.add(urlPattern);
      }

      String authMethod = null;

      // Add web-app/security-constraint for each port component
      for (ServerEndpointMetaData sepMetaData : sepMetaDataList)
      {
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
            if (sepMetaData.isSecureWSDLAccess())
            {
               Element method = (Element)wrc.appendChild(DOMUtils.createElement("http-method"));
               method.appendChild(DOMUtils.createTextNode("GET"));
            }
            Element method = (Element)wrc.appendChild(DOMUtils.createElement("http-method"));
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

         securityHandler.addSecurityRoles(webApp, udi);
      }

      return webApp;
   }

   private Element createJBossWebAppDescriptor(List<ServerEndpointMetaData> sepMetaDataList)
   {
      /* Create a jboss-web
       <jboss-web>
       <security-domain>java:/jaas/cts</security-domain>
       <context-root>/ws/ejbN/</context-root>
       <virtual-host>some.domain.com</virtual-host>
       </jboss-web>
       */
      Element jbossWeb = DOMUtils.createElement("jboss-web");

      UnifiedMetaData wsMetaData = sepMetaDataList.get(0).getServiceMetaData().getUnifiedMetaData();
      String securityDomain = wsMetaData.getSecurityDomain();
      if (securityDomain != null)
      {
         Element secDomain = (Element)jbossWeb.appendChild(DOMUtils.createElement("security-domain"));
         secDomain.appendChild(DOMUtils.createTextNode("java:/jaas/" + securityDomain));
      }

      // Get the context root for this deployment
      String contextRoot = null;
      for (ServerEndpointMetaData sepMetaData : sepMetaDataList)
      {
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
      for (ServerEndpointMetaData sepMetaData : sepMetaDataList)
      {
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
}