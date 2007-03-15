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
package org.jboss.ws.core.server;

/**
 * Interface to be implemented by property helpers to assist the retrieval
 * of objects added to the property context.
 * 
 * If an instance of MessageContextPropertyHelper is added to the 
 * CommonMessageContext a call to get for the same property will return
 * the result of callig get on the helper instead of returning the helper
 * itself.
 * 
 * @author darran.lofthouse@jboss.com
 * @since Oct 22, 2006
 */
public interface PropertyCallback
{
   Object get();
}
