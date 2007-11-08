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
package org.jboss.test.ws.jaxws.wsrm.emulator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.test.ws.jaxws.wsrm.emulator.config.ObjectFactory;
import org.jboss.test.ws.jaxws.wsrm.emulator.config.View;
import org.jboss.test.ws.jaxws.wsrm.emulator.utils.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents Controller part of the MVC architectural pattern
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 24, 2007
 */
public class EndpointEmulator extends HttpServlet
{
   
   private static final String CONFIG_FILE = "config.file";
   private List<View> views;
   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      
      // ensure there's a config.file servlet init parameter specified in web.xml
      String configFile = config.getInitParameter(CONFIG_FILE);
      if (configFile == null)
         throw new RuntimeException(CONFIG_FILE + " init parameter is missing");
      
      // ensure this config.file points to correct resource inside war archive
      InputStream is = config.getServletContext().getResourceAsStream(configFile);
      if (is == null)
         throw new RuntimeException("Resource '" + configFile + "' not found");
      
      Element root = getDocument(is, false).getDocumentElement();
      views = getViews(root, getNamespaces(root)); 
      
      System.out.println(views);
   }
   
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      handleRequest("POST", req, resp);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      handleRequest("GET", req, resp);
   }
   
   private static Document getDocument(InputStream is, boolean nsAware) throws ServletException
   {
      try 
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(nsAware);
         return factory.newDocumentBuilder().parse(is);
      }
      catch (SAXException se)
      {
         throw new ServletException(se);
      }
      catch (ParserConfigurationException pce)
      {
         throw new ServletException(pce);
      }
      catch (IOException ie)
      {
         throw new ServletException(ie);
      }
   }

   private static Map<String, String> getNamespaces(Element root)
   {
      NodeList namespaces = ((Element)root.getElementsByTagName("namespaces").item(0)).getElementsByTagName("namespace");
      Map<String, String> retVal = new HashMap<String, String>();
      for (int i = 0; i < namespaces.getLength(); i++)
      {
         Element namespace = (Element)namespaces.item(i);
         retVal.put(namespace.getAttribute("name"), namespace.getAttribute("value"));
      }
      return retVal;
   }
   
   private static List<View> getViews(Element root, Map<String, String> namespaces)
   {
      List<View> retVal = new LinkedList<View>();
      NodeList views = root.getElementsByTagName("view");
      for (int i = 0; i < views.getLength(); i++)
      {
         retVal.add(ObjectFactory.getView((Element)views.item(i), namespaces));
      }
      return retVal;
   }
   
   private void handleRequest(String httpMethod, HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      String requestMessage = getRequestMessage(req); 
      View view = getView(httpMethod, requestMessage);
      System.out.println("Handling view: " + view.getId());
      Map<String, String> resProperties = view.getResponse().getProperties();
      Map<String, String> reqProperties = view.getRequest().getProperties();
      String responseMessage = getResource(view.getResponse().getResource());
      
      if (resProperties.size() > 0)
      {
         Map<String, String> initializedReqProperties = null;
         if (reqProperties.size() > 0)
         {
            initializedReqProperties = initializeProperties(requestMessage, reqProperties);
         }
         
         Map<String, String> replaceMap = replaceProperties(initializedReqProperties, resProperties);
         responseMessage = modifyResponse(responseMessage, replaceMap);
      }
      
      resp.setContentType(view.getResponse().getContentType());
      PrintWriter writer = resp.getWriter();
      writer.print(responseMessage);
      writer.flush();
      writer.close();
   }
   
   private Map<String, String> initializeProperties(String req, Map<String, String> map)
   throws ServletException, IOException
   {
      Document requestMessage = getDocument(new ByteArrayInputStream(req.getBytes()), true);
      
      Map<String, String> retVal = new HashMap<String, String>();
      for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); )
      {
         String key = i.next();
         String val = map.get(key);
         Element e = getElement(requestMessage, val);
         retVal.put(key, e.getTextContent());
      }
      
      return retVal;
   }
   
   private static Map<String, String> replaceProperties(Map<String, String> reqM, Map<String, String> resM)
   {
      Map<String, String> retVal = new HashMap<String, String>();
      
      for (Iterator<String> i = resM.keySet().iterator(); i.hasNext(); )
      {
         String iKey = i.next();
         String iVal = resM.get(iKey);
         for (Iterator<String> j = reqM.keySet().iterator(); j.hasNext(); )
         {
            String jKey = j.next();
            String jVal = reqM.get(jKey);
            String jRef = "${" + jKey + "}";
            if (iVal.indexOf(jRef) != -1)
            {
               iVal = StringUtil.replace(jRef, jVal, iVal);
            }
         }
         retVal.put(iKey, iVal);
      }
      
      return retVal;
   }
   
   private View getView(String httpMethod, String req) 
   throws ServletException, IOException
   {
      boolean isPost = "POST".equalsIgnoreCase(httpMethod);
      Document requestMessage = isPost ? getDocument(new ByteArrayInputStream(req.getBytes()), true) : null;
      for (View view : views)
      {
         if (httpMethod.equalsIgnoreCase(view.getRequest().getHttpMethod()))
         {
            if (matches(requestMessage, view))
            {
               return view;
            }
         }
      }
      
      return null;
   }
   
   private static boolean matches(Document req, View view)
   {
      List<String> matches = view.getRequest().getMatches();
      if ((matches == null) || (matches.size() == 0))
         return true;
      
      boolean match = true;
      for (String matchString : matches)
      {
         match = match && elementExists(req, matchString);
      }
      
      return match;
   }
   
   private static boolean elementExists(Document req, String match)
   {
      return getElement(req, match) != null;
   }
   
   private static Element getElement(Document req, String match)
   {
      Element e = null;
      
      StringTokenizer st = new StringTokenizer(match, "|");
      while (st.hasMoreTokens())
      {
         String toConvert = st.nextToken();
         QName nodeName = QName.valueOf(toConvert);
         e = getChildElement(e != null ? e : req, nodeName);
         if (e == null) return null;
      }
      
      return e;
   }
   
   private static Element getChildElement(Node e, QName nodeQName)
   {
      NodeList childNodes = e.getChildNodes();
      if (childNodes == null)
         return null;
      
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         Node node = childNodes.item(i);
         if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            Element element = (Element)node;
            boolean namespaceMatches = nodeQName.getNamespaceURI().equals(element.getNamespaceURI());
            boolean nodeNameMatches = nodeQName.getLocalPart().equals(element.getLocalName());
            if (namespaceMatches && nodeNameMatches) return element;
         }
      }
      
      return null;
   }
   
   private String modifyResponse(String response, Map<String, String> replaceMap)
   {
      if ((replaceMap != null) && (replaceMap.size() > 0))
      {
         for (Iterator<String> iterator = replaceMap.keySet().iterator(); iterator.hasNext(); )
         {
            String key = iterator.next();
            String val = replaceMap.get(key);
            response = StringUtil.replace("${" + key + "}", val, response);
         }
      }
      return response;
   }
   
   private String getResource(String resource) throws IOException
   {
      return toString(getServletContext().getResourceAsStream(resource));
   }

   private String getRequestMessage(HttpServletRequest req) throws IOException
   {
      BufferedReader reader = req.getReader();
      String line = null;
      StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null)
      {
         sb.append(line);
      }
      return sb.toString();
   }

   private String toString(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int offset = -1;
      while ((offset = is.read(buffer, 0, buffer.length)) != -1)
      {
         baos.write(buffer, 0, offset);
      }
      return baos.toString();
   }
   
}