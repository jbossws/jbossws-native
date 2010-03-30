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
package org.jboss.ws.core.jaxws.spi.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpMessage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.core.server.MimeHeaderSource;

/**
 * HTTP 1.1 response implementation.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractNettyMessage extends DefaultHttpMessage implements MimeHeaderSource, HttpResponse
{

   /** HTTP response status code. */
   private HttpResponseStatus status;

   /** HTTP request this reponse relates to. */
   private HttpRequest request;

   /** HTTP channel. */
   private Channel channel;

   /**
    * Constructor.
    * 
    * @param version HTTP protocol version
    * @param channel Netty channel
    * @param request original request
    */
   protected AbstractNettyMessage(final HttpVersion version, final Channel channel, final HttpRequest request)
   {
      super(version);

      this.channel = channel;
      this.request = request;
   }

   /**
    * Gets request input stream.
    * 
    * @return request input stream
    */
   public abstract InputStream getInputStream();

   /**
    * Gets response output stream.
    * 
    * @return response output stream.
    */
   public abstract OutputStream getOutputStream();

   /**
    * Returns HTTP request.
    *
    * @return HTTP request
    */
   protected final HttpRequest getRequest()
   {
      return this.request;
   }
   
   /**
    * Returns Netty channel.
    *
    * @return Netty channel
    */
   protected final Channel getChannel()
   {
      return this.channel;
   }
   
   /**
    * Returns response status code.
    * 
    * @return message status code
    */
   public final HttpResponseStatus getStatus()
   {
      return this.status;
   }

   /**
    * Sets response status code.
    * @param sc response status code
    */
   public final void setStatus(final int sc)
   {
      this.status = HttpResponseStatus.valueOf(sc);
   }

   /**
    * Sets response status code.
    * @param sc response status code
    */
   public final void setStatus(final HttpResponseStatus sc)
   {
      this.status = sc;
   }

   /**
    * Sets cookis to response.
    */
   public final void setCookies()
   {
      final String cookieString = this.getRequest().getHeader(HttpHeaders.Names.COOKIE);
      if (cookieString != null)
      {
         final CookieDecoder cookieDecoder = new CookieDecoder();
         final Set<Cookie> cookies = cookieDecoder.decode(cookieString);
         if (!cookies.isEmpty())
         {
            // Reset the cookies if necessary.
            final CookieEncoder cookieEncoder = new CookieEncoder(true);
            for (Cookie cookie : cookies)
            {
               cookieEncoder.addCookie(cookie);
            }
            this.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
         }
      }
   }

   /**
    * String representation of the instance.
    * 
    * @return string
    */
   @Override
   public final String toString()
   {
      return this.getProtocolVersion().getText() + ' ' + this.getStatus();
   }

   /**
    * @see MimeHeaderSource#getMimeHeaders()
    * 
    * @return mime headers
    */
   public final MimeHeaders getMimeHeaders()
   {
      if (this.request.getHeaderNames().size() == 0)
      {
         return null;
      }

      final MimeHeaders headers = new MimeHeaders();

      final Iterator<String> e = this.request.getHeaderNames().iterator();
      String key = null;
      String value = null;
      while (e.hasNext())
      {
         key = e.next();
         value = this.request.getHeader(key);

         headers.addHeader(key, value);
      }

      return headers;
   }

   /**
    * @see MimeHeaderSource#setMimeHeaders(MimeHeaders)
    * 
    * @param headers mime headers
    */
   public final void setMimeHeaders(final MimeHeaders headers)
   {
      final Iterator<?> i = headers.getAllHeaders();
      String key = null;
      while (i.hasNext())
      {
         final MimeHeader header = (MimeHeader) i.next();
         key = header.getName();
         List<String> values = new LinkedList<String>();
         values.add(header.getValue());
         values = this.removeProhibitedCharacters(values);
         this.setHeader(key, values);
      }
   }

   // TODO: https://jira.jboss.org/jira/browse/NETTY-237
   /**
    * Removes prohibited header value characters.
    * 
    * @param values header values before optimization
    * @return optimized header values
    */
   private List<String> removeProhibitedCharacters(final List<String> values)
   {
      final List<String> retVal = new LinkedList<String>();
      for (int i = 0; i < values.size(); i++)
      {
         retVal.add(i, this.removeProhibitedCharacters(values.get(i)));
      }

      return retVal;
   }

   // TODO: https://jira.jboss.org/jira/browse/NETTY-237
   /**
    * Removes prohibited header value characters.
    * 
    * @param value header value before optimization
    * @return optimized header value
    */
   private String removeProhibitedCharacters(final String value)
   {
      String retVal = value;

      retVal = retVal.replace('\r', ' ');
      retVal = retVal.replace('\n', ' ');

      return retVal;
   }

}
