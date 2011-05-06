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
package org.jboss.ws.core.jaxws;

import org.jboss.ws.common.utils.HashCodeUtil;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

import javax.xml.bind.JAXBContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache JAXBContext's.
 *
 * @author Heiko.Braun@jboss.org
 * @since 26.01.2007
 */
public class JAXBContextCache
{
    private Map<Integer, JAXBContext> cache = new ConcurrentHashMap<Integer, JAXBContext>();

    public JAXBContext get(Class[] clazzes)
    {
        Integer id = buildId(clazzes);
        return get(id);
    }

    public void add(Class[] clazzes, JAXBContext context)
    {
        Integer id = buildId(clazzes);
        add(id, context);
    }

    private JAXBContext get(Integer id)
    {
        return cache.get(id);
    }

    private void add(Integer id, JAXBContext context)
    {
        cache.put(id, context);
    }

    private static Integer buildId(Class[] classes)
    {
        int sum = HashCodeUtil.SEED;
        for (Class cls : classes)
        {
            sum = HashCodeUtil.hash(sum, cls.getName());
        }
        return sum;
    }

    /**
     * Access the JAXBContext cache through the message context.
     * The actual instance is assiciated with the EndpointMetaData.
     * @return JAXBContextCache
     */
    public static JAXBContextCache getContextCache()
    {
        CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
        EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
        return epMetaData.getJaxbCache();
    }
}

