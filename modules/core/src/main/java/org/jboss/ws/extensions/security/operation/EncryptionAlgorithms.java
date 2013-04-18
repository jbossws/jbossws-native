/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xml.security.encryption.XMLCipher;

public final class EncryptionAlgorithms
{
   /** --- Keep private for security reasons --- **/
   private static class Algorithm
   {
      Algorithm(String jceName, String xmlName, int size)
      {
         this.jceName = jceName;
         this.xmlName = xmlName;
         this.size = size;
      }

      public String jceName;
      public String xmlName;
      public int size;
   }
   private static Map<String, Algorithm> algorithms;
   private static Map<String, String> algorithmsID;
   /** ----------------------------------------- **/

   public static final String DEFAULT_ALGORITHM = "aes-128";

   static
   {
      algorithms = new HashMap<String, Algorithm>(4);
      algorithms.put("aes-128", new Algorithm("AES", XMLCipher.AES_128, 128));
      algorithms.put("aes-192", new Algorithm("AES", XMLCipher.AES_192, 192));
      algorithms.put("aes-256", new Algorithm("AES", XMLCipher.AES_256, 256));
      algorithms.put("aes-128-gcm", new Algorithm("AES", XMLCipher.AES_128_GCM, 128));
      algorithms.put("aes-192-gcm", new Algorithm("AES", XMLCipher.AES_192_GCM, 192));
      algorithms.put("aes-256-gcm", new Algorithm("AES", XMLCipher.AES_256_GCM, 256));
      algorithms.put("tripledes", new Algorithm("TripleDes", XMLCipher.TRIPLEDES, 168));
      algorithms = Collections.unmodifiableMap(algorithms);
      
      algorithmsID = new HashMap<String, String>(4);
      algorithmsID.put(XMLCipher.AES_128, "aes-128");
      algorithmsID.put(XMLCipher.AES_192, "aes-192");
      algorithmsID.put(XMLCipher.AES_256, "aes-256");
      algorithmsID.put(XMLCipher.TRIPLEDES, "tripledes");
      algorithmsID = Collections.unmodifiableMap(algorithmsID);
   }
   
   public static boolean hasAlgorithm(String id) {
      return algorithms.containsKey(id);
   }
   
   public static String getAlgorithm(String id) {
      Algorithm alg = algorithms.get(id);
      return alg == null ? null : alg.xmlName;
   }
   
   public static String getAlgorithmJceName(String id) {
      Algorithm alg = algorithms.get(id);
      return alg == null ? null : alg.jceName;
   }
   
   public static int getAlgorithmSize(String id) {
      Algorithm alg = algorithms.get(id);
      return alg == null ? null : alg.size;
   }
   
   public static String getAlgorithmID(String xmlName) {
      return algorithmsID.get(xmlName);
   }
}
