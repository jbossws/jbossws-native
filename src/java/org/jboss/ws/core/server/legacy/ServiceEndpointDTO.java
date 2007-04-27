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
package org.jboss.ws.core.server.legacy;

import javax.management.ObjectName;

import org.jboss.ws.core.server.ServiceEndpointMetrics;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 02.02.2007
 */
public class ServiceEndpointDTO {

   private ServiceEndpointMetrics seMetrics;
   private ServiceEndpoint.State state;
   private ObjectName sepID;
   private String address;

   public ServiceEndpointMetrics getSeMetrics() {
      return seMetrics;
   }

   public void setSeMetrics(ServiceEndpointMetrics seMetrics) {
      this.seMetrics = seMetrics;
   }

   public ServiceEndpoint.State getState() {
      return state;
   }

   public void setState(ServiceEndpoint.State state) {
      this.state = state;
   }

   public ObjectName getSepID() {
      return sepID;
   }

   public void setSepID(ObjectName sepID) {
      this.sepID = sepID;
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

}
