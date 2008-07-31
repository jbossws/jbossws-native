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
package org.jboss.ws.feature;

import javax.xml.ws.WebServiceFeature;

import org.jboss.ws.Constants;

/**
 * This feature represents the use of JSON encoding
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 29-Feb-2008
 */
public final class JsonEncodingFeature extends WebServiceFeature
{
   /** 
    * Constant value identifying the JSON encoding feature
    */
   public static final String ID = Constants.NS_JBOSSWS_URI + "/features/json";

   /**
    * Create an <code>JsonFeature</code>.
    * The instance created will be enabled.
    */
   public JsonEncodingFeature()
   {
      this.enabled = true;
   }

   /**
    * Creates an <code>JsonFeature</code>.
    * 
    * @param enabled specifies if this feature should be enabled or not
    */
   public JsonEncodingFeature(boolean enabled)
   {
      this.enabled = enabled;
   }

   /**
    * {@inheritDoc}
    */
   public String getID()
   {
      return ID;
   }
}
