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
package org.jboss.ws.metadata.config;

import java.net.URI;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 14.12.2006
 */

/**
 * Refactor this to use features 
 */
@Deprecated
public class EndpointProperty
{
   public final static String MTOM_THRESHOLD = "http://org.jboss.ws/mtom#threshold";

   /**
    * Set to 0 in order to disable chunked encoding
    */
   public final static String CHUNKED_ENCODING_SIZE = "http://org.jboss.ws/http#chunksize";
   
   public URI name;
   public String value;

}