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
package org.jboss.ws.extensions.security.operation;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.security.SecurityDecoder;
import org.jboss.ws.extensions.security.Target;


public class RequireEncryptionOperation extends RequireTargetableOperation
{
   private List<String> allowedKeyWrapAlgorithms;
   
   private List<String> allowedEncAlgorithms;

   public RequireEncryptionOperation(List<Target> targets) {
      super(targets);
   }
   
   public RequireEncryptionOperation(List<Target> targets, String keyWrapAlgorithms, String algorithms)
   {
      super(targets);
      this.allowedEncAlgorithms = parseStringList(algorithms);
      this.allowedKeyWrapAlgorithms = parseStringList(keyWrapAlgorithms);
   }
   
   public void setupDecoder(SecurityDecoder decoder) {
      if (allowedEncAlgorithms == null) {
         Logger.getLogger(RequireEncryptionOperation.class).warn("No 'algorithms' provided for 'encryption' configuration requirement!");
      }
      decoder.setAllowedEncAlgorithms(allowedEncAlgorithms);
      if (allowedKeyWrapAlgorithms == null) {
         Logger.getLogger(RequireEncryptionOperation.class).warn("No 'keyWrapAlgorithms' provided for 'encryption' configuration requirement!");
      }
      decoder.setAllowedKeyWrapAlgorithms(allowedKeyWrapAlgorithms);
   }
   
   private List<String> parseStringList(String s) {
      List<String> result = null;
      if (s != null && s.trim().length() > 0) {
         StringTokenizer st = new StringTokenizer(s, ", ", false);
         result = new LinkedList<String>();
         while (st.hasMoreTokens()) {
            result.add(st.nextToken());
         }
      }
      return result;
   }
}
