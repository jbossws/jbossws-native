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
package org.jboss.ws.metadata.wsse;

import java.io.Serializable;

/**
 * <code>SecurityDomain</code> allows for getting ws-security configuration
 * from a centralized location being a JAAS security domain.
 *
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
public class SecurityDomain implements Serializable
{
   private static final long serialVersionUID = -3366230588863800636L;

   private String jndi;
   private String authToken;
   private boolean useSecurityDomainAliases;

   public SecurityDomain(String jndi, String authToken, boolean useSecurityDomainAliases)
   {
      this.jndi = jndi;
      this.authToken = authToken;
      this.useSecurityDomainAliases = useSecurityDomainAliases;
   }

   public String getJndi()
   {
      return jndi;
   }

   public void setJndi(String jndi)
   {
      this.jndi = jndi;
   }

   public String getAuthToken()
   {
      return authToken;
   }

   public void setAuthToken(String authToken)
   {
      this.authToken = authToken;
   }

   public boolean isUseSecurityDomainAliases()
   {
      return useSecurityDomainAliases;
   }

   public void setUseSecurityDomainAliases(boolean useSecurityDomainAliases)
   {
      this.useSecurityDomainAliases = useSecurityDomainAliases;
   }
}
