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
package org.jboss.test.ws.jaxws.jbws2975;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ws.core.binding.SerializationContext;
import org.jboss.ws.core.jaxws.JAXBSerializer;
import org.jboss.ws.core.jaxws.SerializationContextJAXWS;
import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.util.xml.BufferedStreamResult;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;


/**
 * A JAXBSerializerTestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JAXBSerializerTestCase extends junit.framework.TestCase
{
   public void testSerializer() throws Exception
   {
        JAXBSerializer  serializer = new JAXBSerializer();
        SerializationContext context = new SerializationContextJAXWS();
        context.setProperty(SerializationContextJAXWS.JAXB_CONTEXT_TYPES, new Class[]{GetCars.class});
        context.setJavaType(GetCars.class);
        QName qname = new QName("http://jbossws.jboss.org", "GetSedansResponse");
        
        GetCars response = new GetCars();
        List<Car> cars = new ArrayList<Car>();
        Toyota camry = new Toyota();

        camry.setMake("Toyota");
        camry.setModel("Camry");
        camry.setColor("Black");

        cars.add(camry);

        Ford focus = new Ford();

        focus.setMake("Ford");
        focus.setModel("Focus");
        focus.setColor("White");
        cars.add(focus);
        response.setReturn(cars);
        
        SOAPMessageContextJAXWS messageContext = new SOAPMessageContextJAXWS();
        QName portTypeName = new QName("http://jbossws.jboss.org", "GetSedans");
        EndpointMetaData endpointMetaData = new MockEndpointMetaData(portTypeName);
        messageContext.setEndpointMetaData(endpointMetaData);
        MessageContextAssociation.pushMessageContext(messageContext);
        BufferedStreamResult result = (BufferedStreamResult)serializer.serialize(qname, null, response, context, null);
        Element element = DOMUtils.parse(result.toString());
        List<Element> elements = DOMUtils.getChildElementsAsList(element, "return");
        for (Element ele : elements) 
        {  
           String typeValue = DOMUtils.getAttributeValue(ele, new QName("http://www.w3.org/2001/XMLSchema-instance", "type"));
           assertEquals("The namespace prefix is not serialized", true ,typeValue.indexOf(":") > 0);
        }
        //cleanup the mock object 
        MessageContextAssociation.popMessageContext();
   }

}
