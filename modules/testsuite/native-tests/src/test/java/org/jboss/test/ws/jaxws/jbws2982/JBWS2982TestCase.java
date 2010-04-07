/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2982;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2982] Native ignores user specified MessageContext.HTTP_REQUEST_HEADERS
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class JBWS2982TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2982TestCase.class, "jaxws-jbws2982.war");
   }
   private static final int THREADS_COUNT = 5;
   private static final int REQUESTS_COUNT = 5;
   private static final String ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2982"; 
   private final Endpoint[] proxies = new Endpoint[THREADS_COUNT];
   private final Thread[] threads = new Thread[THREADS_COUNT];
   private final TestJob[] jobs = new TestJob[THREADS_COUNT];
   private final Logger log = Logger.getLogger(this.getClass());
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName("http://jboss.org/jbws2982", "EndpointService");
      URL wsdlURL = new URL(ENDPOINT_ADDRESS + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      for (int i = 0; i < THREADS_COUNT; i++)
         proxies[i] = service.getPort(Endpoint.class);
   }

   public void testEndpointConcurrently() throws Exception
   {
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         log.debug("Creating thread " + (i + 1));
         jobs[i] = new TestJob(proxies[i], REQUESTS_COUNT, "TestJob" + i);
         threads[i] = new Thread(jobs[i]);
      }
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         log.debug("Starting thread " + (i + 1));
         threads[i].start();
      }
      Exception e = null;
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         log.debug("Joining thread " + (i + 1));
         threads[i].join();
         if (e == null)
            e = jobs[i].getException();
      }
      if (e != null) throw e;
   }

   private static final class TestJob implements Runnable
   {
      private final String jobName;
      private final Endpoint proxy; 
      private final int countOfRequests;
      private Exception exception;
      private static final Logger log = Logger.getLogger(TestJob.class);

      TestJob(Endpoint proxy, int countOfRequests, String jobName)
      {
         this.proxy = proxy;
         this.countOfRequests = countOfRequests;
         this.jobName = jobName;
      }
      
      public void run()
      {
         try
         {
            for (int i = 0; i < this.countOfRequests; i++)
            {
               this.setRequestParameter(proxy, i);
               int retVal = proxy.getRequestParameter(jobName);
               log.debug("Thread=" + this.jobName + ", iteration=" + i);
               if (retVal != (i + 1))
                  throw new RuntimeException("Thread=" + this.jobName + ", iteration=" + i + ", received=" + retVal);
               assertNotNull(this.getResponseHeaders(proxy));
            }
         }
         catch (Exception e)
         {
            log.error("Exception caught: " + e.getMessage());
            this.exception = e;
         }
      }
      
      @SuppressWarnings("unchecked")
      private Map<String, List<String>> getResponseHeaders(Endpoint proxy)
      {
         BindingProvider bp = (BindingProvider)proxy;
         
         return (Map<String, List<String>>)bp.getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
      }
      
      @SuppressWarnings("unchecked")
      private void setRequestParameter(Endpoint proxy, int value)
      {
         BindingProvider bp = (BindingProvider)proxy;
         
         Map<String, List<String>> requestHeaders = (Map<String, List<String>>)bp.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
         if (requestHeaders == null)
         {
            requestHeaders = new HashMap<String, List<String>>();
         }
         List<String> encodedValue = new LinkedList<String>();
         encodedValue.add(String.valueOf(value));
         requestHeaders.put("extension-header", encodedValue);
         bp.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
      }
      
      Exception getException()
      {
         return this.exception;
      }
   }
}
