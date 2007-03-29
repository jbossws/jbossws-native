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

// $Id: $

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
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
 * @author Thomas.Diesler@jboss.org
 * @since 05-Feb-2007
 */
public class XMLFragment
{
   // provide logging
   private static Logger log = Logger.getLogger(XMLFragment.class);

   private Source source;
   private Result result;

   // An exception that is created when a client 
   // accesses a StreamSource that can only be read once
   private RuntimeException streamSourceAccessMarker;

   /** Factory method to create XMLFragment from strings.
    */
   public static XMLFragment fromStringFragment(String fragment)
   {
      Source source = new StreamSource(new ByteArrayInputStream(fragment.getBytes()));
      return new XMLFragment(source);
   }

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

      source = beginStreamSourceAccess(source);
      endStreamSourceAccess();
      
      return source;
   }

   public String resultToString()
   {
      if (source != null)
         throw new IllegalStateException("Source should never be converted to String");

      return resultToString(this.result);
   }

   /**
    * Transform the Source or Result to an Element
    */
   public Element toElement()
   {
      Element resultingElement = null;

      try
      {
         if (source != null)
         {
            try
            {
               source = beginStreamSourceAccess(source);
               resultingElement = DOMUtils.sourceToElement(source);
               endStreamSourceAccess();
            }
            catch (IOException ex)
            {
               handleStreamSourceAccessException(ex);
            }
         }
         else
         {
            resultingElement = DOMUtils.parse(resultToString(result));
         }
      }
      catch (IOException e)
      {
         WSException.rethrow("Failed to convert to org.w3c.dom.Element", e);
      }

      return resultingElement;
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

   public void writeTo(OutputStream out) throws IOException
   {
      writeTo(new PrintWriter(out));
   }

   /**
    * Should only be called with <code>jbossws.SOAPMessage==TRACE</code>
    */
   private void writeSource(Writer writer) throws IOException
   {
      if (source instanceof DOMSource)
      {
         DOMSource domSource = (DOMSource)source;
         DOMWriter domWriter = new DOMWriter(writer).setPrettyprint(false);
         domWriter.print(domSource.getNode());
      }
      else if (source instanceof StreamSource)
      {
         try
         {
            source = beginStreamSourceAccess(source);

            StreamSource streamSource = (StreamSource)source;

            Reader reader = streamSource.getReader();
            if (reader == null)
               reader = new InputStreamReader(streamSource.getInputStream());

            char[] cbuf = new char[1024];
            int r = reader.read(cbuf);
            while (r > 0)
            {
               writer.write(cbuf, 0, r);
               r = reader.read(cbuf);
            }

            endStreamSourceAccess();
         }
         catch (IOException ex)
         {
            handleStreamSourceAccessException(ex);
         }
      }
      else
      {
         throw new IllegalArgumentException("Unable to process source: " + source);
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
         throw new IllegalArgumentException("Unable to process result: " + result);
      }
   }

   private String resultToString(Result result)
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

   private Source beginStreamSourceAccess(Source source)
   {
      if (source instanceof StreamSource)
      {
         /* Do some brute force buffering
         try
         {
            Element element = DOMUtils.sourceToElement(source);
            source = new DOMSource(element);
         }
         catch (IOException ex)
         {
            throw new WSException("Cannot create DOMSource", ex);
         }
         */
      }
      return source;
   }

   private void endStreamSourceAccess()
   {
      // Create the marker exception
      if (source instanceof StreamSource)
      {
         streamSourceAccessMarker = new RuntimeException();
      }
   }

   private void handleStreamSourceAccessException(IOException ex) throws IOException
   {
      if (source instanceof StreamSource && streamSourceAccessMarker != null)
      {
         log.error("StreamSource was previously accessed from", streamSourceAccessMarker);
      }
      throw ex;
   }

   public String toString()
   {
      String contents = source != null ? "source=" + source : "result=" + result;
      return "XMLFragment {" + contents + "}";
   }
}
