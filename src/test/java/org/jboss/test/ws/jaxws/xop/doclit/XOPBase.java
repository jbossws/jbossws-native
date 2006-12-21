package org.jboss.test.ws.jaxws.xop.doclit;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.jaxrpc.xop.XOPTestSupport;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: hbraun
 * Date: 08.12.2006
 * Time: 16:02:37
 * To change this template use File | Settings | File Templates.
 */
public abstract class XOPBase extends JBossWSTest {

   protected MTOMEndpoint port;
   protected SOAPBinding binding;

   protected abstract MTOMEndpoint getPort();
   protected abstract SOAPBinding getBinding();

   public void testDHRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      URL url = new URL("file:resources/jaxws/xop/shared/attach.jpeg");
      DataHandler dh = new DataHandler(url);
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals(response.getDataHandler().getContentType(), "image/jpeg");
      assertTrue(response.getDataHandler().getContent() instanceof BufferedImage);
   }

   public void testDHResponseOptimzed() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      URL url = new URL("file:resources/jaxws/xop/shared/attach.jpeg");
      DataHandler dh = new DataHandler(url);
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals(response.getDataHandler().getContentType(), "application/octet-stream");
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }

   public void testImgRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      Image img = XOPTestSupport.createTestImage();
      if(img!=null) // might fail on unix
      {
         ImageRequest request = new ImageRequest();
         request.setData(img);

         ImageResponse response = getPort().echoImage(request);

         assertNotNull(response);
         assertTrue(response.getData() instanceof Image);
      }
   }

   public void testImgResponseOptimized() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      Image img = XOPTestSupport.createTestImage();

      if(img!=null) // might fail on unix
      {
         ImageRequest request = new ImageRequest();
         request.setData(img);

         ImageResponse response = getPort().echoImage(request);

         assertNotNull(response);
         assertTrue(response.getData() instanceof Image);
      }
   }

   public void testSrcRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      Source src = XOPTestSupport.createTestSource();
      SourceRequest request = new SourceRequest();
      request.setData(src);

      SourceResponse response = getPort().echoSource(request);

      assertNotNull(response);
      assertTrue(response.getData() instanceof Source);
   }

   public void testSrcResponseOptimized() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      Source src = XOPTestSupport.createTestSource();
      SourceRequest request = new SourceRequest();
      request.setData(src);

      SourceResponse response = getPort().echoSource(request);

      assertNotNull(response);
      assertTrue(response.getData() instanceof Source);
   }
}
