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
package org.jboss.ws.core.utils;

// $Id$

import java.security.PublicKey;
import java.util.List;
import java.util.Stack;

import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.extensions.security.SecurityStore;

/**
 * Maintain thread locals at a single point.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @author Thomas.Diesler@jboss.com
 * @since 10-Apr-2006
 */
public class ThreadLocalAssociation
{
   /**
    * SOAP message context
    * @see org.jboss.ws.core.soap.MessageContextAssociation
    */
   private static ThreadLocal<Stack<CommonMessageContext>> msgContextAssoc = new ThreadLocal<Stack<CommonMessageContext>>();

   /**
    * @see org.jboss.ws.extensions.security.STRTransform
    */
   private static ThreadLocal<SecurityStore> strTransformAssoc = new ThreadLocal<SecurityStore>();
   
   /**
    * Public keys used to sign incoming message
    */
   private static ThreadLocal<List<PublicKey>> signatureKeysAssoc = new ThreadLocal<List<PublicKey>>();

   public static ThreadLocal<Stack<CommonMessageContext>> localMsgContextAssoc()
   {
      return msgContextAssoc;
   }

   public static ThreadLocal<SecurityStore> localStrTransformAssoc()
   {
      return strTransformAssoc;
   }
   
   public static ThreadLocal<List<PublicKey>> localSignatureKeysAssoc()
   {
      return signatureKeysAssoc;
   }

   public static void clear()
   {
      msgContextAssoc.remove();
      strTransformAssoc.remove();
      signatureKeysAssoc.remove();
   }
}
