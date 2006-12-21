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
/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 9, 2006
 * @version $Id$
 */
package org.jboss.test.ws.jaxws.samples.retail.handler;

import java.util.Stack;

public class Timer {

   private Stack<Long> timings = new Stack<Long>();

   private static Timer ourInstance = new Timer();

   public static Timer getInstance() {
      return ourInstance;
   }

   private Timer() {
   }

   public synchronized void push(long t)
   {
      timings.push(new Long(t));
   }

   public synchronized long pop()
   {
      if(timings.isEmpty())
         return 0;

      Long aLong = timings.pop();
      return System.currentTimeMillis() - aLong.longValue();
   }
}
