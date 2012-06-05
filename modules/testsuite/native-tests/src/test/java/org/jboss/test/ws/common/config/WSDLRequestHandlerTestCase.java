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
package org.jboss.test.ws.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ws.core.server.WSDLRequestHandler;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Comprehensive testcase for WSDLRequestHandler's address rewrite rules
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Nov-2009
 */
public class WSDLRequestHandlerTestCase extends JBossWSTest
{
   private static final String wsdlPublishLocation = "/foo/bar";
   
   public void testNoAlwaysModifyWithValidSoapAddress() throws Exception
   {
      File wsdl = getResourceFile("common/config/test.wsdl");
      ServerConfig config = new TestConfig(false, "myHost", 8080, 8443);
      
      URL wsdlLocation = new URL("http://www.foo.org/c/d");
      WSDLRequestHandler handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      Document doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://blah/blah", getSoapAddressLocation(doc)); //no rewrite because the modifySoapAddress is false (and the soap:address in the wsdl is "valid")
      assertEquals("http://myHost:8080/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports locations are always rewritten

      //test with rewrite based on called uri (for multiple virtual host support, see JBWS-1178 and subsequent jira issues)
      config = new TestConfig(false, ServerConfig.UNDEFINED_HOSTNAME, 8080, 8443);
      handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://blah/blah", getSoapAddressLocation(doc)); //no rewrite because the modifySoapAddress is false (and the soap:address in the wsdl is "valid")
      assertEquals("http://localhost:80/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports rewritten with caller data because UNDEFINED_HOSTNAME
                                                                                                          //was specified (required for multiple virtual host support)
   }
   
   public void testAlwaysModifyValidSoapAddress() throws Exception
   {
      File wsdl = getResourceFile("common/config/test.wsdl");
      ServerConfig config = new TestConfig(true, "myHost", 8080, 8443);
      
      URL wsdlLocation = new URL("http://www.foo.org");
      WSDLRequestHandler handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      Document doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://myHost:8080/blah", getSoapAddressLocation(doc)); //rewrite with provided host and port (always modify is true)
      assertEquals("http://myHost:8080/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports locations are always rewritten
      
      //test with rewrite based on called uri (for multiple virtual host support, see JBWS-1178 and subsequent jira issues)
      config = new TestConfig(true, ServerConfig.UNDEFINED_HOSTNAME, 8080, 8443);
      
      handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://localhost:80/blah", getSoapAddressLocation(doc)); //rewrite with called host and port (always modify is true)
      assertEquals("http://localhost:80/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports rewritten with all caller data because UNDEFINED_HOSTNAME
                                                                                                          //was specified (required for multiple virtual host support)
   }
   
   public void testNoAlwaysModifyWithUndefinedSoapAddress() throws Exception
   {
      File wsdl = getResourceFile("common/config/testUndefinedHost.wsdl");
      ServerConfig config = new TestConfig(false, "myHost", 8080, 8443);
      
      URL wsdlLocation = new URL("http://www.foo.org/c/d");
      WSDLRequestHandler handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      Document doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://myHost:8080/zzzz", getSoapAddressLocation(doc)); //rewrite because the soap:address in the wsdl has "jbossws.undefined.host" host
      assertEquals("http://myHost:8080/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports locations are always rewritten

      //test with rewrite based on called uri (for multiple virtual host support, see JBWS-1178 and subsequent jira issues)
      config = new TestConfig(false, ServerConfig.UNDEFINED_HOSTNAME, 8080, 8443);
      
      handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://localhost:80/zzzz", getSoapAddressLocation(doc)); //rewrite because the soap:address in the wsdl has "jbossws.undefined.host" host
      assertEquals("http://localhost:80/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports rewritten with caller data because UNDEFINED_HOSTNAME
                                                                                                          //was specified (required for multiple virtual host support)
   }
   
   public void testAlwaysModifyUndefinedSoapAddress() throws Exception
   {
      File wsdl = getResourceFile("common/config/testUndefinedHost.wsdl");
      ServerConfig config = new TestConfig(true, "myHost", 8080, 8443);
      
      URL wsdlLocation = new URL("http://www.foo.org");
      WSDLRequestHandler handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      Document doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://myHost:8080/zzzz", getSoapAddressLocation(doc)); //rewrite with provided host and port (always modify is true)
      assertEquals("http://myHost:8080/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports locations are always rewritten
      
      //test with rewrite based on called uri (for multiple virtual host support, see JBWS-1178 and subsequent jira issues)
      config = new TestConfig(true, ServerConfig.UNDEFINED_HOSTNAME, 8080, 8443);
      
      handler = new TestWSDLRequestHandler(wsdlLocation, wsdl, config);
      doc = handler.getDocumentForPath(new URL("http://localhost:80/a/b"), null); //null resPath for getting the wsdl document
      assertEquals("http://localhost:80/zzzz", getSoapAddressLocation(doc)); //rewrite with called host and port (always modify is true)
      assertEquals("http://localhost:80/a/b?wsdl&resource=schema1.xsd", getXsdImportSchemaLocation(doc)); //relative imports rewritten with caller data because UNDEFINED_HOSTNAME
                                                                                                          //was specified (required for multiple virtual host support)
   }
   
   // --------- Helper methods --------
   
   private static String getSoapAddressLocation(Document doc)
   {
      Element docElement = doc.getDocumentElement();
      Element el = (Element)DOMUtils.getChildElements(docElement, new QName("http://schemas.xmlsoap.org/wsdl/soap/","address"), true).next();
      return el.getAttribute("location");
   }
   
   private static String getXsdImportSchemaLocation(Document doc)
   {
      Element docElement = doc.getDocumentElement();
      Element el = (Element)DOMUtils.getChildElements(docElement, new QName("http://www.w3.org/2001/XMLSchema","import"), true).next();
      return el.getAttribute("schemaLocation");
   }
   
   // --------- Test helper classes --------
   
   private class TestWSDLRequestHandler extends WSDLRequestHandler
   {
      private File testWsdl;
      
      public TestWSDLRequestHandler(URL wsdlLocationFromMetadata, File testWsdl, ServerConfig config)
      {
         super(wsdlLocationFromMetadata, wsdlPublishLocation, config);
         this.testWsdl = testWsdl;
      }
      
      @Override
      protected InputStream openStreamToWSDL() throws IOException
      {
         return testWsdl.toURI().toURL().openStream();
      }
      
   }
   
   private class TestConfig implements ServerConfig
   {
      private boolean modifySoapAddress;
      private String webServiceHost;
      private int webServicePort;
      private int webServiceSecurePort;
      
      public TestConfig(boolean modifySoapAddress, String webServiceHost, int webServicePort, int webServiceSecurePort)
      {
         super();
         this.modifySoapAddress = modifySoapAddress;
         this.webServiceHost = webServiceHost;
         this.webServicePort = webServicePort;
         this.webServiceSecurePort = webServiceSecurePort;
      }
      
	  public void addClientConfig(ClientConfig config)
      {
         // does nothing
	  }

	  public List<ClientConfig> getClientConfigs()
      {
         return null;
	  }

	  public String getWebServiceHost()
      {
         return webServiceHost;
      }

      public int getWebServicePort()
      {
         return webServicePort;
      }

      public int getWebServiceSecurePort()
      {
         return webServiceSecurePort;
      }

      public boolean isModifySOAPAddress()
      {
         return modifySoapAddress;
      }

      public void setModifySOAPAddress(boolean flag)
      {
         this.modifySoapAddress = flag;
      }

      public void setWebServiceHost(String host) throws UnknownHostException
      {
         this.webServiceHost = host;
      }

      public void setWebServicePort(int port)
      {
         this.webServicePort = port;
      }

      public void setWebServiceSecurePort(int port)
      {
         this.webServiceSecurePort = port;
      }

      public File getHomeDir()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getImplementationTitle()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getImplementationVersion()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public File getServerDataDir()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public File getServerTempDir()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public void addEndpointConfig(EndpointConfig config)
      {
         // TODO Auto-generated method stub
      }

      @Override
      public List<EndpointConfig> getEndpointConfigs()
      {
         // TODO Auto-generated method stub
         return null;
      }

   }
   
}
