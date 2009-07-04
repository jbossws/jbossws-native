/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Takes a marshalled byte array and converts to a Java data object.
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Jun-2009
 *
 */
public interface UnMarshaller
{
   /**
    * Will read from the inputstream and convert contents to java Object.
    *
    * @param inputStream stream to read data from to do conversion.
    * @param metadata can be any transport specific metadata (such as headers from http transport).
    *        This can be null, depending on if transport supports metadata.
    *
    * @return
    * @throws IOException all specific i/o exceptions need to be thrown as this.
    */
   Object read(InputStream inputStream, Map<String, Object> metadata) throws IOException;
}
