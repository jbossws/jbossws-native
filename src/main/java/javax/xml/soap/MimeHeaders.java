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
package javax.xml.soap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/** A container for MimeHeader objects, which represent the MIME headers present
 * in a MIME part of a message. 
 * 
 * This class is used primarily when an application wants to retrieve specific
 * attachments based on certain MIME headers and values. This class will most
 * likely be used by implementations of AttachmentPart and other MIME dependent
 * parts of the SAAJ API. 

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class MimeHeaders
{
   private LinkedList headers = new LinkedList();

   public MimeHeaders()
   {
   }

   /**
    * 
    * @param name
    * @param value
    * @throws IllegalArgumentException - if name is null or empty.
    */
   public void addHeader(String name, String value) throws IllegalArgumentException
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Invalid null or empty header name");
      MimeHeader header = new MimeHeader(name, value);
      headers.add(header);
   }

   public Iterator getAllHeaders()
   {
      return headers.iterator();
   }

   /**
    * 
    * @param name
    * @return All matching header values if found, null otherwise
    */
   public String[] getHeader(String name)
   {
      ArrayList tmp = new ArrayList();
      for (int n = 0; n < headers.size(); n++)
      {
         MimeHeader mh = (MimeHeader)headers.get(n);
         if (mh.getName().equalsIgnoreCase(name))
            tmp.add(mh.getValue());
      }
      String[] values = null;
      if (tmp.size() > 0)
      {
         values = new String[tmp.size()];
         tmp.toArray(values);
      }
      return values;
   }

   public Iterator getMatchingHeaders(String[] names)
   {
      MatchingIter iter = new MatchingIter(headers, names, true);
      return iter;
   }

   public Iterator getNonMatchingHeaders(String[] names)
   {
      MatchingIter iter = new MatchingIter(headers, names, false);
      return iter;
   }

   public void removeAllHeaders()
   {
      headers.clear();
   }

   public void removeHeader(String name)
   {
      Iterator iter = headers.iterator();
      while (iter.hasNext())
      {
         MimeHeader mh = (MimeHeader)iter.next();
         if (mh.getName().equalsIgnoreCase(name))
            iter.remove();
      }
   }

   /** Replaces the current value of the first header entry whose name matches
    * the given name with the given value, adding a new header if no existing
    * header name matches. This method also removes all matching headers after
    * the first one.
    * 
    */
   public void setHeader(String name, String value)
   {
      boolean didSet = false;
      for (int n = 0; n < headers.size(); n++)
      {
         MimeHeader mh = (MimeHeader)headers.get(n);
         if (mh.getName().equalsIgnoreCase(name))
         {
            if (didSet == true)
            {
               headers.remove(n);
               n--;
            }
            else
            {
               mh = new MimeHeader(name, value);
               headers.set(n, mh);
               didSet = true;
            }
         }
      }

      if (didSet == false)
      {
         this.addHeader(name, value);
      }
   }

   private static class MatchingIter implements Iterator
   {
      private LinkedList headers;
      private HashSet names;
      private boolean match;
      private int index;
      private MimeHeader mh;

      MatchingIter(LinkedList headers, String[] names, boolean match)
      {
         this.headers = headers;
         this.index = 0;
         this.names = new HashSet();
         for (int n = 0; n < names.length; n++)
            this.names.add(names[n].toLowerCase());
         this.match = match;
      }

      public boolean hasNext()
      {
         boolean hasNext = index < headers.size();
         while (hasNext == true)
         {
            mh = (MimeHeader)headers.get(index);
            index++;
            String name = mh.getName().toLowerCase();
            if (names.contains(name) == match)
               break;
            hasNext = index < headers.size();
         }
         return hasNext;
      }

      public Object next()
      {
         return mh;
      }

      public void remove()
      {
         headers.remove(index - 1);
      }
   }
}
