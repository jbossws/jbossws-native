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
package javax.xml.ws.http;

// $Id$

import javax.xml.ws.Binding;
import javax.xml.ws.Binding21;

/** The <code>HTTPBinding</code> interface is an 
 *  abstraction for the XML/HTTP binding.
 * 
 *  @since JAX-WS 2.0
**/
public interface HTTPBinding extends Binding21
{

  /**
   * A constant representing the identity of the XML/HTTP binding.
   */
  public static final String HTTP_BINDING = "http://www.w3.org/2004/08/wsdl/http";
}
