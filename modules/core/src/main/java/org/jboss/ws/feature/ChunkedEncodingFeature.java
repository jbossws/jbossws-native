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
package org.jboss.ws.feature;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import org.jboss.ws.common.Constants;

/**
 * This feature represents the use of http chunked encoding
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jul-2009
 */
public final class ChunkedEncodingFeature extends WebServiceFeature
{
   /** 
    * Constant value identifying the FastInfosetFeature
    */
   public static final String ID = Constants.NS_JBOSSWS_URI + "/features/chunkedencoding";

   protected int chunkSize = 1024;
   
   /**
    * Create an <code>ChunkedEncodingFeature</code>.
    * The instance created will be enabled.
    */
   public ChunkedEncodingFeature()
   {
      this.enabled = true;
   }

   /**
    * Creates a <code>ChunkedEncodingFeature</code>.
    * 
    * @param enabled specifies if this feature should be enabled or not
    */
   public ChunkedEncodingFeature(boolean enabled)
   {
      this.enabled = enabled;
      if (!enabled)
      {
         this.chunkSize = 0;
      }
   }
   
   /**
    * Creates a <code>ChunkedEncodingFeature</code> and set the provided chunk size (chunkSize == 0 turns off chunked encoding).
    * 
    * @param chunkSize the chunk size in bytes
    */
   public ChunkedEncodingFeature(int chunkSize)
   {
      if (chunkSize < 0)
      {
         throw new WebServiceException("ChunkedEncodingFeature.chunkSize must be >= 0, actual value: " + chunkSize);
      }
      this.chunkSize = chunkSize;
      this.enabled = (chunkSize > 0);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getID()
   {
      return ID;
   }

   /**
    * Gets the configured chunksize
    * 
    * @return the current chunksize in bytes
    */
   public int getChunkSize()
   {
      return chunkSize;
   }
}
