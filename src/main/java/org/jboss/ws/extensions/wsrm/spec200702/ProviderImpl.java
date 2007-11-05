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
package org.jboss.ws.extensions.wsrm.spec200702;

import org.jboss.ws.extensions.wsrm.common.ConstantsImpl;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.MessageFactory;
import org.jboss.ws.extensions.wsrm.spi.Provider;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.Provider
 */
public final class ProviderImpl extends Provider
{
   
   private static final String IMPLEMENTATION_VERSION = "http://docs.oasis-open.org/ws-rx/wsrm/200702";
   private static final Constants CONSTANTS = new ConstantsImpl("wsrm11",IMPLEMENTATION_VERSION);
   private static final Provider INSTANCE = new ProviderImpl();
   
   private ProviderImpl()
   {
      // forbidden inheritance
   }
   
   public static Provider getInstance()
   {
      return INSTANCE; 
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.Provider#getMessageFactory()
    */
   @Override
   public MessageFactory getMessageFactory()
   {
      return MessageFactoryImpl.getInstance();
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.Provider#getConstants()
    */
   @Override
   public Constants getConstants()
   {
      return CONSTANTS;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.Provider#getNamespaceURI()
    */
   @Override
   public String getNamespaceURI()
   {
      return IMPLEMENTATION_VERSION;
   }
   
}
