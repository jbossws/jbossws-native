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
package org.jboss.ws.core.jaxws.client;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.jboss.ws.api.util.BundleUtils;

/**
 * The Response interface provides methods used to obtain the payload and context of a 
 * message sent in response to an operation invocation.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 15-Sep-2006
 */
public class ResponseImpl implements Response
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ResponseImpl.class);
   private Future delegate;
   private Object result;
   private Exception exception;
   private Map<String, Object> context = new HashMap<String, Object>();

   public void setException(Exception ex)
   {
      this.exception = ex;
   }

   public Future getFuture()
   {
      if (delegate == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "FUTURE_NOT_AVAILABLE"));

      if (exception != null)
      {
         if (exception instanceof WebServiceException)
         {
            throw (WebServiceException)exception;
         }
         else
         {
            throw new WebServiceException(exception);
         }
      }

      return delegate;
   }

   private Future getFutureInternal()
   {
      if (delegate == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "FUTURE_NOT_AVAILABLE"));

      return delegate;
   }

   public void setFuture(Future delegate)
   {
      this.delegate = delegate;
   }

   public Map<String, Object> getContext()
   {
      return context;
   }

   void set(Object result)
   {
      this.result = result;
   }

   public boolean cancel(boolean mayInterruptIfRunning)
   {
      return getFutureInternal().cancel(mayInterruptIfRunning);
   }

   public Object get() throws InterruptedException, ExecutionException
   {
      Object response = getResult();
      if (response != null)
      {
         return response;
      }

      getFutureInternal().get();
      response = getResult();

      return response;
   }

   public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
   {
      Object response = getResult();
      if (response != null)
      {
         return response;
      }

      getFutureInternal().get(timeout, unit);
      response = getResult();

      return response;
   }

   private Object getResult() throws ExecutionException
   {
      if (exception != null)
         throw new ExecutionException(exception);

      return result;
   }

   public boolean isCancelled()
   {
      return getFutureInternal().isCancelled();
   }

   public boolean isDone()
   {
      return getFutureInternal().isDone();
   }
}
