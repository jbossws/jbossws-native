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
package org.jboss.wsf.stack.jbws;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.ServletDelegate;
import org.jboss.wsf.spi.management.EndpointResolver;
import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.ws.common.injection.PreDestroyHolder;
import org.jboss.ws.common.servlet.AbstractEndpointServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Native endpoint servlet that is installed for every web service endpoint
 * @author thomas.diesler@jboss.com
 * @author heiko.braun@jboss.com
 * @author richard.opalka@jboss.com
 */
public final class EndpointServlet extends AbstractEndpointServlet implements ServletDelegate
{
   
   // provide logging
   protected static final Logger log = Logger.getLogger(EndpointServlet.class);

   private List<PreDestroyHolder> preDestroyRegistry = new LinkedList<PreDestroyHolder>();
   private final Object lock = new Object();

   /**
    * Provides Native specific endpoint resolver
    * @param servletContext servlet context
    * @param servletName servlet name
    * @return new Native specific endpoint resolver
    */
   @Override
   protected final EndpointResolver newEndpointResolver(String contextPath, String servletName)
   {
      return new WebAppResolver(contextPath, servletName);
   }

   /**
    * Post init phase hook using template method
    * @param servletConfig servlet config
    */
   @Override
   protected final void postInit(ServletConfig servletConfig)
   {
      ServletConfigHelper.initEndpointConfig(servletConfig, endpoint);
   }
   
   @Override
   protected final void postService()
   {
      registerForPreDestroy(endpoint);
   }

   @Override
   public final void destroy()
   {
      synchronized(this.lock)
      {
         for (final PreDestroyHolder holder : this.preDestroyRegistry)
         {
            try
            {
               final Object targetBean = holder.getObject();
               InjectionHelper.callPreDestroyMethod(targetBean);
            }
            catch (Exception exception)
            {
               log.error(exception.getLocalizedMessage(),  exception);
            }
         }
         this.preDestroyRegistry.clear();
      }
      super.destroy();
   }

   private void registerForPreDestroy(Endpoint ep)
   {
      PreDestroyHolder holder = (PreDestroyHolder)ep.getAttachment(PreDestroyHolder.class);
      if (holder != null)
      {
         synchronized(this.lock)
         {
            if (!this.preDestroyRegistry.contains(holder))
            {
               this.preDestroyRegistry.add(holder);
            }
         }
         ep.removeAttachment(PreDestroyHolder.class);
      }
   }

   @Override
   public void doHead(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doHead(request, response);
   }

   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doGet(request, response);
   }

   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPost(request, response);
   }

   @Override
   public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPut(request, response);
   }

   @Override
   public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doDelete(request, response);
   }

   @Override
   public void service(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.service(request, response);
   }

}
