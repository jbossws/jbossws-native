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
package org.jboss.test.ws.jaxws.jbpapp3389;

import java.io.FileOutputStream;
import java.io.InputStream;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test Endpoint implementation.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 12th January 2010
 */
@WebService(name = "Endpoint", targetNamespace = "http://ws.jboss.org/jbpapp3389", endpointInterface = "org.jboss.test.ws.jaxws.jbpapp3389.Endpoint")
public class EndpointImpl implements Endpoint
{

   public Result echo(final String message)
   {
	  Result result = new Result();
	  AnyWrapper wrapper = new AnyWrapper();
	  result.setAnyWrapper(wrapper);
	
	  QName qname1 = new QName("http://jboss.org/support", "extension", "ns3");
      Element element1 = DOMUtils.createElement(qname1);     
      element1.setAttribute("number", "1234");
                  
      wrapper.setAny(element1);

      try {
    	  JAXBContext ctx = JAXBContext.newInstance(Result.class);
    	  ClassLoader loader = Thread.currentThread().getContextClassLoader();
          InputStream inStream = loader.getResourceAsStream("response.xml");
          
          Unmarshaller unmarshaller = ctx.createUnmarshaller();
          result = (Result) unmarshaller.unmarshal(inStream);                    
      } catch (Exception e)
      {
    	  throw new RuntimeException("Oops",e);
      }
      
      return result;
   }

}
