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
package org.jboss.ws.metadata.wsse;


/**
 * <code>Sign</code> represents the sign tag, which declares that a message
 * should be signed.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class RequireEncryption extends Targetable
{
   private static final long serialVersionUID = 3765798680988205648L;
   
   private boolean includeFaults;
   
   private String keyWrapAlgorithms;
   
   private String algorithms;

   public RequireEncryption(boolean includeFaults, String keyWrapAlgorithms, String algorithms)
   {
      this.includeFaults = includeFaults;
      this.algorithms = algorithms;
      this.keyWrapAlgorithms = keyWrapAlgorithms;
   }

   public boolean isIncludeFaults()
   {
      return includeFaults;
   }

   public void setIncludeFaults(boolean includeFaults)
   {
      this.includeFaults = includeFaults;
   }
   
   public String getdKeyWrapAlgorithms()
   {
      return keyWrapAlgorithms;
   }

   public void setKeyWrapAlgorithms(String keyWrapAlgorithms)
   {
      this.keyWrapAlgorithms = keyWrapAlgorithms;
   }

   public String getAlgorithms()
   {
      return algorithms;
   }

   public void setAlgorithms(String algorithms)
   {
      this.algorithms = algorithms;
   }

}
