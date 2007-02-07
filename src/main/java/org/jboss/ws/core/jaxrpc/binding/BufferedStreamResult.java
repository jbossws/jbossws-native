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
package org.jboss.ws.core.jaxrpc.binding;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 06.02.2007
 */
public class BufferedStreamResult extends StreamResult {

   ByteArrayOutputStream bout = new ByteArrayOutputStream();

   public BufferedStreamResult()
   {
      super();
   }

   public void setOutputStream(OutputStream outputStream)
   {
      throw new IllegalArgumentException("Operation not supported");
   }

   public OutputStream getOutputStream()
   {
      return bout;
   }

   public void setWriter(Writer writer)
   {
      throw new IllegalArgumentException("Operation not supported");
   }

   public Writer getWriter()
   {
     return null;
   }
}
