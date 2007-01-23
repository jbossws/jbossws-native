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
package org.jboss.ws.core.jaxws;

// $Id$

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * A WebServiceContext implementation that delegates to the EJBContext if available.
 * Otherwise it uses jbosssx.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Jan-2007
 */
public class WebServiceContextImpl extends AbstractWebServiceContext implements Serializable
{
   // provide logging
   private Logger log = Logger.getLogger(WebServiceContextImpl.class);

   private transient RealmMapping realmMapping;

   public WebServiceContextImpl(MessageContext messageContext)
   {
      super(messageContext);
   }

   @Override
   public Principal getUserPrincipal()
   {
      Principal principal = SecurityAssociation.getCallerPrincipal();
      return principal;
   }

   @Override
   public boolean isUserInRole(String role)
   {
      boolean isUserInRole = false;
      Principal principal = SecurityAssociation.getCallerPrincipal();
      RealmMapping realmMapping = getRealmMapping();
      if (realmMapping != null && principal != null)
      {
         Set<Principal> roles = new HashSet<Principal>();
         roles.add(new SimplePrincipal(role));
         isUserInRole = realmMapping.doesUserHaveRole(principal, roles);
      }
      return isUserInRole;
   }

   private RealmMapping getRealmMapping()
   {
      if (realmMapping == null)
      {
         String lookupName = "java:comp/env/security/realmMapping";
         try
         {
            InitialContext iniCtx = new InitialContext();
            realmMapping = (RealmMapping)iniCtx.lookup(lookupName);
         }
         catch (NamingException e)
         {
            log.debug("Cannot obtain realm mapping from: " + lookupName);
         }
      }
      return realmMapping;
   }
}
