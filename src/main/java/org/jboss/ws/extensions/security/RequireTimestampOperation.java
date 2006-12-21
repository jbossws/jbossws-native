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

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.jboss.ws.extensions.security.element.SecurityHeader;
import org.jboss.ws.extensions.security.element.Timestamp;
import org.w3c.dom.Document;


public class RequireTimestampOperation implements RequireOperation
{
   private SecurityHeader header;

   public RequireTimestampOperation(SecurityHeader header, SecurityStore store) throws WSSecurityException
   {
      this.header = header;
   }

   public void process(Document message, List<Target> targets, String maxAge, String credential, Collection<String> processedIds) throws WSSecurityException
   {
      Timestamp stamp = header.getTimestamp();
      if (stamp == null)
         throw new FailedCheckException("Required timestamp not present.");

      // If there is no maxAge specified then we are done
      if (maxAge == null)
         return;

      int max = Integer.parseInt(maxAge);

      Calendar expired = (Calendar)stamp.getCreated().clone();
      expired.add(Calendar.SECOND, max);

      if (! Calendar.getInstance().before(expired))
         throw new FailedCheckException("Timestamp of message is too old.");
   }
}
