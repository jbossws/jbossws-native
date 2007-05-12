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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.XOPTestSupport;

/**
 * User: hbraun
 * Date: 08.12.2006
 */
public abstract class XOPBase extends JBossWSTest
{
   private File imgFile = new File("resources/jaxws/samples/xop/shared/attach.jpeg");

   protected MTOMEndpoint port;
   protected SOAPBinding binding;

   protected MTOMEndpoint getPort()
   {
      return port;
   }

   protected SOAPBinding getBinding()
   {
      return binding;
   }

   public void testDHRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      DataHandler dh = new DataHandler(imgFile.toURL());
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals(response.getDataHandler().getContentType(), "image/jpeg");
      assertTrue(response.getDataHandler().getContent() instanceof BufferedImage);
   }

   public void testDHResponseOptimzed() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      DataHandler dh = new DataHandler(imgFile.toURL());
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals(response.getDataHandler().getContentType(), "application/octet-stream");
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }

   public void testImgRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      Image img = XOPTestSupport.createTestImage(imgFile);
      if (img != null) // might fail on unix
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

      Image img = XOPTestSupport.createTestImage(imgFile);

      if (img != null) // might fail on unix
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
