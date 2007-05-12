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
package org.jboss.test.ws;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.ws.utils.ObjectNameFactory;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossWSTestHelper
{
   // provide logging
   private static Logger log = Logger.getLogger(JBossWSTestHelper.class);
   
   private static MBeanServerConnection server;
   private static String integrationTarget;
   
   /** Deploy the given archive
    */
   public void deploy(String archive) throws Exception
   {
      URL url = getArchiveURL(archive);
      getDeployer().deploy(url);
   }

   /** Undeploy the given archive
    */
   public void undeploy(String archive) throws Exception
   {
      URL url = getArchiveURL(archive);
      getDeployer().undeploy(url);
   }

   /** True, if -Djbossws.integration.target=tomcat */
   public static boolean isTargetTomcat()
   {
      String target = getIntegrationTarget();
      return "tomcat".equals(target);
   }

   /** True, if -Djbossws.integration.target=jboss?? */
   public static boolean isTargetJBoss()
   {
      String target = getIntegrationTarget();
      return target != null && target.startsWith("jboss");
   }

   /** True, if -Djbossws.integration.target=jboss50 */
   public static boolean isTargetJBoss50()
   {
      String target = getIntegrationTarget();
      return "jboss50".equals(target);
   }

   /** True, if -Djbossws.integration.target=jboss42 */
   public static boolean isTargetJBoss42()
   {
      String target = getIntegrationTarget();
      return "jboss42".equals(target);
   }

   /** True, if -Djbossws.integration.target=jboss40 */
   public static boolean isTargetJBoss40()
   {
      String target = getIntegrationTarget();
      return "jboss40".equals(target);
   }
   
   /**
    * Get the JBoss server host from system property "jbosstest.host.name"
    * This defaults to "localhost"
    */
   public static String getServerHost()
   {
      String hostName = System.getProperty("jbosstest.host.name", "localhost");
      return hostName;
   }

   public static MBeanServerConnection getServer() 
   {
      if (server == null)
      {
         try
         {
            InitialContext iniCtx = new InitialContext();
            server = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
         }
         catch (NamingException ex)
         {
            throw new RuntimeException("Cannot obtain MBeanServerConnection", ex);
         }
      }
      return server;
   }

   private TestDeployer getDeployer()
   {
      if (isTargetJBoss())
      {
         return new TestDeployerJBoss(getServer());
      }
      else if (isTargetTomcat())
      {
         String username = System.getProperty("tomcat.manager.username");
         String password = System.getProperty("tomcat.manager.password");
         return new TestDeployerTomcat(username, password);
      }
      else
      {
         throw new IllegalStateException("Unsupported integration target: " + getIntegrationTarget());
      }
   }

   private static String getIntegrationTarget()
   {
      if (integrationTarget == null)
      {
         integrationTarget = System.getProperty("jbossws.integration.target");
         
         // Read the JBoss SpecificationVersion
         try
         {
            ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
            String jbossVersion = (String)getServer().getAttribute(oname, "SpecificationVersion");
            if (jbossVersion.startsWith("5.0"))
               jbossVersion = "jboss50";
            else if (jbossVersion.startsWith("4.2"))
               jbossVersion = "jboss42";
            else if (jbossVersion.startsWith("4.0"))
               jbossVersion = "jboss40";
            else
               throw new RuntimeException("Unsupported jboss version: " + jbossVersion);
            
            if (jbossVersion.equals(integrationTarget) == false)
            {
               log.warn("Integration target mismatch, using: " + jbossVersion);
               integrationTarget = jbossVersion;
            }
         }
         catch (Throwable th)
         {
            // ignore, we are not running on jboss-4.2 or greater
         }

         if (integrationTarget == null)
         {
            log.warn("Cannot obtain jbossws.integration.target, using default: tomcat");
            integrationTarget = "tomcat";
         }
      }
      return integrationTarget;
   }

   /** Try to discover the URL for the deployment archive */
   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      URL url = null;
      try
      {
         url = new URL(archive);
      }
      catch (MalformedURLException ignore)
      {
         // ignore
      }

      if (url == null)
      {
         File file = new File(archive);
         if (file.exists())
            url = file.toURL();
      }

      if (url == null)
      {
         File file = new File("libs/" + archive);
         if (file.exists())
            url = file.toURL();
      }

      if (url == null)
         throw new IllegalArgumentException("Cannot obtain URL for: " + archive);

      return url;
   }
}
