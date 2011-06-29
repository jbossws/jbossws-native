/*
* JBoss, Home of Professional Open Source.
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.extensions.security.exception.FailedAuthenticationException;
import org.jboss.ws.extensions.security.exception.WSSecurityException;
import org.jboss.ws.metadata.wsse.Authorize;
import org.jboss.ws.metadata.wsse.Role;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.spi.security.SecurityDomainContext;

/**
 * Operation to authenticate and check the authorisation of the
 * current user.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @since December 23rd 2008
 */
public class AuthorizeOperation
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(AuthorizeOperation.class);
   private static final Logger log = Logger.getLogger(AuthorizeOperation.class);

   private Authorize authorize;

   private SecurityAdaptorFactory secAdapterfactory;
   
   private SecurityDomainContext sdc;

   public AuthorizeOperation(Authorize authorize)
   {
      this.authorize = authorize;
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      secAdapterfactory = spiProvider.getSPI(SecurityAdaptorFactory.class, cl);

      Endpoint ep = EndpointAssociation.getEndpoint();
      if (ep != null)
      {
         sdc = ep.getSecurityDomainContext();
      }
   }

   public void process() throws WSSecurityException
   {
      boolean TRACE = log.isTraceEnabled();

      if (TRACE)
         log.trace("About to check authorization, using security domain '" + sdc.getSecurityDomain() + "'");

      // Step 1 - Authenticate using currently associated principals.
      SecurityAdaptor securityAdaptor = secAdapterfactory.newSecurityAdapter();
      Principal principal = securityAdaptor.getPrincipal();
      Object credential = securityAdaptor.getCredential();

      if (principal == null)
      {
         principal = new Principal()
         {
            @Override
            public String getName()
            {
               return null;
            }
         };
      }
      
      Subject subject = new Subject();

      if (sdc.isValid(principal, credential, subject) == false)
      {
         String msg = BundleUtils.getMessage(bundle, "AUTHENTICATION_FAILED", principal);
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new FailedAuthenticationException(e);
      }
      sdc.pushSubjectContext(subject, principal, credential);

      if (TRACE)
         log.trace("Authenticated, principal=" + principal);

      // Step 2 - If unchecked all ok so return.
      if (authorize.isUnchecked())
      {
         if (TRACE)
            log.trace("authorize.isUnchecked()==true skipping roles check.");

         return;
      }

      // Step 3 - If roles specified check user in role. 
      Set<Principal> expectedRoles = expectedRoles();
      if (TRACE)
         log.trace("expectedRoles=" + expectedRoles);

      if (sdc.doesUserHaveRole(principal, expectedRoles) == false)
      {
         Set<Principal> userRoles = sdc.getUserRoles(principal);
         String msg = BundleUtils.getMessage(bundle, "INSUFFICIENT_METHOD_PERMISSIONS", 
               new Object[]{principal, expectedRoles,  userRoles});
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new FailedAuthenticationException(e);
      }

      if (TRACE)
         log.trace("Roles check complete, principal=" + principal + ", requiredRoles=" + expectedRoles);
   }

   private Set<Principal> expectedRoles()
   {
      List<Role> roles = authorize.getRoles();
      int rolesCount = (roles != null) ? roles.size() : 0;
      Set<Principal> expectedRoles = new HashSet<Principal>(rolesCount);

      if (roles != null)
      {
         for (Role current : roles)
         {
            expectedRoles.add(new SimplePrincipal(current.getName()));
         }
      }

      return expectedRoles;
   }

}
