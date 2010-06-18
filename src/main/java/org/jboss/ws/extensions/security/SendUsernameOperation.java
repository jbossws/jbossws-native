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
package org.jboss.ws.extensions.security;

//$Id$

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.logging.Logger;
import org.jboss.security.Base64Encoder;
import org.jboss.ws.extensions.security.auth.callback.UsernameTokenCallback;
import org.jboss.ws.extensions.security.auth.callback.UsernameTokenCallbackHandler;
import org.jboss.ws.extensions.security.element.SecurityHeader;
import org.jboss.ws.extensions.security.element.UsernameToken;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.w3c.dom.Document;

public class SendUsernameOperation implements EncodingOperation
{
   private static Logger log = Logger.getLogger(SendUsernameOperation.class);

   private SecurityHeader header;

   private SecurityStore store;
   
   public SendUsernameOperation(SecurityHeader header, SecurityStore store)
   {
      this.header = header;
      this.store = store;
   }

   public void process(Document message, List<Target> targets, String username, String credential, String algorithm, boolean digest, boolean useNonce, boolean useTimestamp) throws WSSecurityException
   {
      String created = useTimestamp ? getCurrentTimestampAsString() : null;
      String nonce = useNonce ? store.getNonceGenerator().generateNonce() : null;
      String password = digest ? createPasswordDigest(nonce, created, credential) : credential;
      header.addToken(new UsernameToken(username, password, message, digest, nonce, created));
   }
   
   private static String getCurrentTimestampAsString()
   {
      Calendar timestamp = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      return SimpleTypeBindings.marshalDateTime(timestamp);
   }
   
   /**
    * Calculate the password digest using a MessageDigest and the UsernameTokenCallback/CallbackHandler
    */
   @SuppressWarnings("unchecked")
   public static String createPasswordDigest(String nonce, String created, String password)
   {
      String passwordHash = null;
      try
      {
         // convert password to byte data
         byte[] passBytes = password.getBytes("UTF-8");
         // prepare the username token digest callback
         UsernameTokenCallback callback = new UsernameTokenCallback();
         Map options = new HashMap();
         callback.init(options);
         // add the username token callback handler to provide the parameters
         CallbackHandler handler = new UsernameTokenCallbackHandler(nonce, created);
         handler.handle((Callback[])options.get("callbacks"));
         // calculate the hash and apply the encoding.
         MessageDigest md = MessageDigest.getInstance("SHA");
         callback.preDigest(md);
         md.update(passBytes);
         callback.postDigest(md);
         byte[] hash = md.digest();
         passwordHash =  Base64Encoder.encode(hash);
      }
      catch(Exception e)
      {
         log.error("Password hash calculation failed ", e);
      }
      return passwordHash;
   }
}
