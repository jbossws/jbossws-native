/**
 * 
 */
package org.jboss.test.ws.jaxws.jbws2901;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import org.jboss.test.ws.jaxws.jbws2901.Endpoint;
/**
 * [JBWS-2901] Disable XML external entity resolver
 *  
 * @author <a href="mailto:bmaxwell@redhat.com">Brad Maxwell</a>
 *
 */
public class JBWS2901TestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-jbws2901/TestService";
   private String targetNS = "http://jbws2901.jaxws.ws.test.jboss.org/";
   private String passwdContents = "root:x:0:0:root:/root:/bin/bash";
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2901TestCase.class, "jaxws-jbws2901.war");
   }

   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }
   
   public void testSOAPMessage() throws Exception
   {
      String response = getResponse("jaxws/jbws2901/message.xml");
      assertTrue(response.contains("HTTP/1.1 200 OK"));
      assertTrue(response.contains("<return>Hello</return>"));
   }
   
   public void testSOAPMessageAttack1() throws Exception
   {
      String response = getResponse("jaxws/jbws2901/attack-message-1.xml");
      assertFalse(response.contains(passwdContents));
   }
   
   private String getResponse(String requestFile) throws Exception
   {
      final String CRNL = "\r\n";
      String content = getContent(new FileInputStream(getResourceFile(requestFile)));
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(getServerHost(), 8080));
      OutputStream out = socket.getOutputStream();

      // send an HTTP request to the endpoint
      out.write(("POST /jaxws-jbws2901/TestService HTTP/1.0" + CRNL).getBytes());
      out.write(("Host: " + getServerHost() + ":8080" + CRNL).getBytes());
      out.write(("Content-Type: text/xml" + CRNL).getBytes());
      out.write(("Content-Length: " + content.length() + CRNL).getBytes());
      out.write((CRNL).getBytes());
      out.write((content).getBytes());

      // read the response
      String response = getContent(socket.getInputStream());
      socket.close();
      System.out.println("---");
      System.out.println(response);
      System.out.println("---");
      return response;
   }
   
   private static String getContent(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, is);
      return new String(baos.toByteArray());
   }
}
