/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.client.transport;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import org.jboss.ws.common.Constants;

/**
 * The keep-alive cache used for keeping track of idle NettyTransport instances
 * This is inspired from the KeepAliveCache used by HttpConnection.
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Sep-2009
 *
 */
public class KeepAliveCache implements Runnable
{
   private static final long serialVersionUID = 1L;
   
   //the hashtable actually storing the objects
   private Hashtable<KeepAliveKey, TransportHandlerVector> table = new Hashtable<KeepAliveKey, TransportHandlerVector>();

   //number of connections for a given url in the cache
   private static final int DEFAULT_MAX_CONNECTIONS = 5;
   private static int maxConnections = -1;
   //approx lifetime (ms) of a NettyTransport before it is closed (unless a keep-alive timeout is specified)
   private static final int LIFETIME = 5000;

   private Thread keepAliveTimer = null;

   static int getMaxConnections()
   {
      if (maxConnections == -1)
      {
         maxConnections = Integer.parseInt(getSystemProperty(Constants.HTTP_MAX_CONNECTIONS, String.valueOf(DEFAULT_MAX_CONNECTIONS)));
         if (maxConnections <= 0)
            maxConnections = DEFAULT_MAX_CONNECTIONS;
      }
      return maxConnections;
   }
      
   public KeepAliveCache()
   {
      
   }

   /**
    * Register this URL and NettyTransportHandler with the cache
    * @param url  The URL contains info about the host and port
    * @param transport The NettyTransportHandler to be cached
    */
   public synchronized void put(final URL url, NettyTransportHandler transport)
   {
      boolean startThread = (keepAliveTimer == null);
      if (!startThread)
      {
         if (!keepAliveTimer.isAlive())
         {
            startThread = true;
         }
      }
      if (startThread)
      {
         table.clear();
         final KeepAliveCache cache = this;
         AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {
            public Object run()
            {
               // We want to create the JBossWS-Keep-Alive-Timer in the
               // system thread group
               ThreadGroup grp = Thread.currentThread().getThreadGroup();
               ThreadGroup parent = null;
               while ((parent = grp.getParent()) != null)
               {
                  grp = parent;
               }

               keepAliveTimer = new Thread(grp, cache, "JBossWS-Keep-Alive-Timer");
               keepAliveTimer.setDaemon(true);
               keepAliveTimer.setPriority(Thread.MAX_PRIORITY - 2);
               keepAliveTimer.start();
               return null;
            }
         });
      }

      KeepAliveKey key = new KeepAliveKey(url);
      TransportHandlerVector v = (TransportHandlerVector)table.get(key);

      if (v == null)
      {
         int keepAliveTimeout = transport.getKeepAliveTimeout();
         v = new TransportHandlerVector(keepAliveTimeout > 0 ? keepAliveTimeout * 1000 : LIFETIME);
         v.put(transport);
         table.put(key, v);
      }
      else
      {
         v.put(transport);
      }
   }

   /* remove an obsolete NettyTransportHandler from its ClientVector */
   public synchronized void remove(NettyTransportHandler h, Object obj)
   {
      KeepAliveKey key = new KeepAliveKey(h.getUrl());
      TransportHandlerVector v = (TransportHandlerVector)table.get(key);
      if (v != null)
      {
         v.remove(h);
         if (v.empty())
         {
            removeVector(key);
         }
      }
   }

   /* called by a clientVector thread when all its connections have timed out
    * and that vector of connections should be removed.
    */
   synchronized void removeVector(KeepAliveKey k)
   {
      table.remove(k);
   }

   /**
    * Check to see if this URL has a cached NettyTransportHandler
    */
   public synchronized NettyTransportHandler get(URL url)
   {

      KeepAliveKey key = new KeepAliveKey(url);
      TransportHandlerVector v = (TransportHandlerVector)table.get(key);
      if (v == null)
      { // nothing in cache yet
         return null;
      }
      return v.get();
   }

   public void run()
   {
      do
      {
         try
         {
            Thread.sleep(LIFETIME);
         }
         catch (InterruptedException e)
         {
         }
         synchronized (this)
         {
            /* Remove all unused NettyTransportHandler.  Starting from the
             * bottom of the stack (the least-recently used first).
             */
            long currentTime = System.currentTimeMillis();

            ArrayList<KeepAliveKey> keysToRemove = new ArrayList<KeepAliveKey>();

            for (KeepAliveKey key : table.keySet())
            {
               TransportHandlerVector v = (TransportHandlerVector)table.get(key);
               synchronized (v)
               {
                  int i;

                  for (i = 0; i < v.size(); i++)
                  {
                     KeepAliveEntry e = (KeepAliveEntry)v.elementAt(i);
                     if ((currentTime - e.idleStartTime) > v.nap)
                     {
                        NettyTransportHandler h = e.hc;
                        h.end();
                     }
                     else
                     {
                        break;
                     }
                  }
                  v.subList(0, i).clear();

                  if (v.size() == 0)
                  {
                     keysToRemove.add(key);
                  }
               }
            }
            for (KeepAliveKey key : keysToRemove)
            {
               removeVector(key);
            }
         }
      }
      while (table.size() > 0);

      return;
   }

   private void writeObject(java.io.ObjectOutputStream stream) throws IOException
   {
      throw new NotSerializableException();
   }

   private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      throw new NotSerializableException();
   }

   
   
   /**
    * A stack (FILO) for keeping NettyTransportHandler instances
    */
    class TransportHandlerVector extends Stack<KeepAliveEntry>
    {
       private static final long serialVersionUID = 1L;

       // sleep time in milliseconds, before cache clear
       int nap;

       TransportHandlerVector(int nap)
       {
          this.nap = nap;
       }

       synchronized NettyTransportHandler get()
       {
          if (empty())
          {
             return null;
          }
          else
          {
             //Loop until we find a connection that has not timed out
             NettyTransportHandler hc = null;
             long currentTime = System.currentTimeMillis();
             do
             {
                KeepAliveEntry e = (KeepAliveEntry)pop();
                if ((currentTime - e.idleStartTime) > nap)
                {
                   e.hc.end();
                }
                else
                {
                   hc = e.hc;
                }
             }
             while ((hc == null) && (!empty()));
             return hc;
          }
       }

       /**
        * Return a valid NettyTransportHandler back to
        * the stack.
        * 
        * @param h
        */
       synchronized void put(NettyTransportHandler h)
       {
          if (size() > KeepAliveCache.getMaxConnections())
          {
             h.end();
          }
          else
          {
             push(new KeepAliveEntry(h, System.currentTimeMillis()));
          }
       }

       private void writeObject(java.io.ObjectOutputStream stream) throws IOException
       {
          throw new NotSerializableException();
       }

       private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
       {
          throw new NotSerializableException();
       }
    }

    /**
     * This is used for the keys of the cache, basically redefine equals
     * and hashCode to use URL's protocol/host/port only (also preventing
     * network traffic required by URL's equals/hashCode).
     *
     */
    class KeepAliveKey
    {
       private String protocol = null;
       private String host = null;
       private int port = 0;

       /**
        * Constructor
        *
        * @param url the URL containing the protocol, host and port information
        */
       public KeepAliveKey(URL url)
       {
          this.protocol = url.getProtocol();
          this.host = url.getHost();
          this.port = url.getPort();
       }

       /**
        * Determine whether or not two objects of this type are equal
        */
       public boolean equals(Object obj)
       {
          if ((obj instanceof KeepAliveKey) == false)
             return false;
          KeepAliveKey kae = (KeepAliveKey)obj;
          return host.equals(kae.host) && (port == kae.port) && protocol.equals(kae.protocol);
       }

       /**
        * The hashCode() for this object is the string hashCode() of
        * concatenation of the protocol, host name and port.
        */
       public int hashCode()
       {
          String str = protocol + host + port;
          return str.hashCode();
       }
    }

    class KeepAliveEntry
    {
       NettyTransportHandler hc;
       long idleStartTime;

       KeepAliveEntry(NettyTransportHandler hc, long idleStartTime)
       {
          this.hc = hc;
          this.idleStartTime = idleStartTime;
       }
    }
    
    static String getSystemProperty(final String name, final String defaultValue)
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm == null)
       {
          return System.getProperty(name, defaultValue);
       }
       else
       {
          PrivilegedAction<String> action = new PrivilegedAction<String>() {
             public String run()
             {
                return System.getProperty(name, defaultValue);
             }
          };
          return AccessController.doPrivileged(action);
       }
    }

}
