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
package org.jboss.ws.core.soap;

//$Id: $

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.binding.BufferedStreamResult;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.w3c.dom.Element;

/**
 * A XMLFragment represent either a XML {@link Source} or a {@link Result}.<br>
 * The basic idea is that any {@link SOAPContentElement} XML_VALID state
 * (either before unmarshalling or after marshalling) is represented through a single interface.<br>
 *
 * @see SOAPContentElement
 * @see XMLContent
 *
 * @author Heiko.Braun@jboss.org
 * @since 05.02.2007
 */
public class XMLFragment
{

   private Source source;
   private Result result;

   private boolean idempotent = true;

   public XMLFragment(Source source)
   {
      this.source = source;
   }

   public XMLFragment(Result result)
   {
      this.result = result;
   }

   public Source getSource()
   {
      if (null == source)
         throw new IllegalStateException("Source not available");
      return source;
   }

   public Result getResult()
   {
      if (null == source)
         throw new IllegalStateException("Result not available");
      return result;
   }

   public String toStringFragment()
   {
      if (!idempotent)
         throw new IllegalStateException("Trying to call a non-idempotent operation");

      if (this.source != null)
         return sourceToStringFragement(this.source);
      else return resultToStringFragment(this.result);
   }

   /**
    * Note this method <b>is not expected to be idempotent</b>.
    * It depends on the underlying source impl. that backs the implementation.
    */
   public Element toElement()
   {
      if (!idempotent)
         throw new IllegalStateException("Trying to call a non-idempotent operation");

      Element resultingElement = null;

      try
      {
         if (source != null)
         {
            resultingElement = DOMUtils.sourceToElement(source);

            // Any Source besides DOMSource is expected not to be idempotent
            if (!(source instanceof DOMSource))
               idempotent = false;

         }
         else
         {
            resultingElement = DOMUtils.parse(resultToStringFragment(result));

            // Any Result besides DOMResult is expected not to be idempotent
            if (!(result instanceof DOMResult))
               idempotent = false;
         }
      }
      catch (IOException e)
      {
         WSException.rethrow("Failed to convert to org.w3c.dom.Element", e);
      }

      return resultingElement;
   }

   /**
    * Factory method to create XMLFragment from strings.
    * @param fragment
    * @return new XMLFragment
    */
   public static XMLFragment fromStringFragment(String fragment)
   {
      Source source = new StreamSource(new ByteArrayInputStream(fragment.getBytes()));
      return new XMLFragment(source);
   }

   private String resultToStringFragment(Result result)
   {

      if (result instanceof DOMResult)
      {
         return DOMWriter.printNode(((DOMResult)result).getNode(), false);
      }
      else if (result instanceof BufferedStreamResult)
      {
         BufferedStreamResult br = (BufferedStreamResult)result;
         byte[] bytes = ((ByteArrayOutputStream)br.getOutputStream()).toByteArray();
         return new String(bytes);
      }

      throw new IllegalArgumentException("Unable to process javax.xml.transform.Result implementation: " + result);
   }

   private static String sourceToStringFragement(Source source)
   {

      throw new IllegalArgumentException("Source should never be converted to String");

      /*new RuntimeException("sourceToStringFragement").printStackTrace(System.out);

       String xmlFragment = null;

       try {
       TransformerFactory tf = TransformerFactory.newInstance();
       ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
       tf.newTransformer().transform(source, new StreamResult(baos));
       xmlFragment = new String(baos.toByteArray());
       if (xmlFragment.startsWith("<?xml"))
       {
       int index = xmlFragment.indexOf(">");
       xmlFragment = xmlFragment.substring(index + 1);
       }
       } catch (TransformerException e) {
       WSException.rethrow(e);
       }

       return xmlFragment;
       */
   }

   public void writeTo(Writer writer) throws IOException
   {
      if (result != null)
      {
         writeResult(writer);
      }
      else
      {
         writeSource(writer);
      }
   }

   /**
    * Should only be called with <code>jbossws.SOAPMessage==TRACE</code>
    */
   private void writeSource(Writer writer) throws IOException
   {

      //new RuntimeException("writeSource").printStackTrace(System.out);

      if (source instanceof DOMSource)
      {
         DOMSource domSource = (DOMSource)source;
         DOMWriter dw = new DOMWriter(writer).setPrettyprint(false);
         dw.print(domSource.getNode());
      }
      else if (source instanceof StreamSource)
      {
         StreamSource streamSource = (StreamSource)source;
         copyStream(streamSource.getInputStream(), writer);
      }
      else
      {
         throw new IllegalArgumentException("Unable to process javax.xml.transform.Source implementation :" + result);
      }
   }

   private static void copyStream(InputStream ins, Writer writer) throws IOException
   {
      byte[] bytes = new byte[1024];
      int r = ins.read(bytes);
      while (r > 0)
      {
         writer.write(new String(bytes), 0, r);
         r = ins.read(bytes);
      }
   }

   private void writeResult(Writer writer)
   {
      if (result instanceof DOMResult)
      {
         DOMResult domResult = (DOMResult)result;
         DOMWriter dw = new DOMWriter(writer).setPrettyprint(false);
         dw.print(domResult.getNode());
      }
      else if (result instanceof BufferedStreamResult)
      {
         BufferedStreamResult sr = (BufferedStreamResult)result;
         ByteArrayOutputStream out = (ByteArrayOutputStream)sr.getOutputStream();
         try
         {
            byte[] bytes = out.toByteArray();
            writer.write(new String(bytes));
         }
         catch (IOException e)
         {
            throw new WSException("Failed to write XMLFragment to output stream", e);
         }
      }
      else
      {
         throw new IllegalArgumentException("Unable to process javax.xml.transform.Result implementation: " + result);
      }
   }

   public void writeTo(OutputStream out) throws IOException
   {
      writeTo(new PrintWriter(out));
   }

   public String toString()
   {
      String contents = source != null ? "source=" + source : "result=" + result;
      return "XMLFragment {" + contents + ", idempotent=" + idempotent + "}";
   }
}
