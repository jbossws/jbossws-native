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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.util.Base64;

/**
 * A deployer that deploys to Tomcat
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-May-2006
 */
public class TestDeployerTomcat implements TestDeployer
{
   private String username, password;

   // Map<String,String> of URL to context path
   private static Map pathMap = new HashMap();

   public TestDeployerTomcat(String username, String password)
   {
      this.username = username;
      this.password = password;
   }

   public void deploy(URL url) throws Exception
   {
      File destDir = new File(new File(url.getFile()).getParent() + "/wspublish");
      destDir.mkdirs();

      // Use reflection to invoke wspublish.process() from the tomcat integration layer
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class wspublishClass = loader.loadClass("org.jboss.ws.integration.tomcat.wspublish");
      Method process = wspublishClass.getMethod("process", new Class[]{URL.class, File.class, String.class});
      URL warURL = (URL)process.invoke(wspublishClass.newInstance(), new Object[]{url, destDir, null});

      String path = warURL.toExternalForm();
      path = path.substring(path.lastIndexOf("/"));
      if (path.endsWith(".war"))
         path = path.substring(0, path.length() - 4);

      URL managerURL = new URL(getManagerPath() + "/deploy?path=" + path + "&war=" + warURL.toExternalForm());
      HttpURLConnection con = getURLConnection(managerURL);

      con.connect();

      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String status = br.readLine();

      if (status == null)
         throw new IllegalStateException("Cannot obtain deploy status");

      if (status.startsWith("OK") == false)
         throw new IllegalStateException("Cannot deploy application: " + status);

      path = status.substring(status.indexOf("/"));
      pathMap.put(url.toExternalForm(), path);
   }

   public void undeploy(URL url) throws Exception
   {
      String path = (String) pathMap.get(url.toExternalForm());
      if (path != null)
      {
         URL managerURL = new URL(getManagerPath() + "/undeploy?path=" + path);
         HttpURLConnection con = getURLConnection(managerURL);

         con.connect();

         BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
         String status = br.readLine();

         if (status == null)
            throw new IllegalStateException("Cannot obtain undeploy status");

         if (status.startsWith("OK") == false)
            throw new IllegalStateException("Cannot undeploy application: " + status);
      }
   }

   private String getManagerPath() throws MalformedURLException
   {
      String hostName = System.getProperty("jboss.bind.address", "localhost");
      return "http://" + hostName + ":8080/manager";
   }

   private HttpURLConnection getURLConnection(URL managerURL) throws IOException, ProtocolException
   {
      HttpURLConnection con = (HttpURLConnection)managerURL.openConnection();
      con.setRequestMethod("GET");
      con.setDoInput(true);

      String authorization = username + ":" + password;
      authorization = Base64.encodeBytes(authorization.getBytes());
      con.setRequestProperty("Authorization", "Basic " + authorization);
      return con;
   }
}
