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
package org.jboss.test.ws.jaxws.jbws1795;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws1795.handcoded.Cabin;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.ListLocation;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.LocationImpl;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.TechCabin;
import org.jboss.test.ws.jaxws.jbws1795.service.TravelAgentEndpoint;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1795] Type Substitution doesn't work with Schema2Java Client approach
 * These test sources were provided by our users. 
 *
 * http://jira.jboss.org/jira/browse/JBWS-1795
 *
 * @author richard.opalka@jboss.com
 * @since 3-Oct-2007
 */
public class HandcodedSourcesTestCase extends JBossWSTest
{
   
   private TravelAgentEndpoint tae = null;

   public static Test suite()
   {
      return new JBossWSTestSetup(HandcodedSourcesTestCase.class, "jaxws-jbws1795.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1795/JBWS1795Service?wsdl");
      QName qname = new QName("http://service.jbws1795.jaxws.ws.test.jboss.org/","TravelAgentBeanService");
      Service service = Service.create(wsdlURL,qname);      
      tae = service.getPort(TravelAgentEndpoint.class);
   }

   public void testInvocation() throws Exception
   {
      ListLocation locListIn = new ListLocation(); 
      ListLocation locListOut = new ListLocation(); 
      List<LocationImpl> list = new ArrayList<LocationImpl>();

      Cabin cabin_1 = new Cabin();
      cabin_1.setId(1);
      cabin_1.setLocName("deck");
      cabin_1.setName("Master Suite");
      cabin_1.setDeckLevel(2);
      cabin_1.setShipId(3);
      cabin_1.setBedCount(4);
      list.add(cabin_1);

      TechCabin cabin_2 = new TechCabin();
      cabin_2.setId(2);
      cabin_2.setLocName("hulk");
      cabin_2.setName("Heating Cabin");
      cabin_2.setDeckLevel(7);
      cabin_2.setShipId(3);
      cabin_2.setPurpose("heating");
      list.add(cabin_2);
            
      locListIn.setList(list);
            
      locListOut = tae.passLocation(locListIn);
            
      for (int i=0; i < locListOut.getList().size(); i++){
               	
         LocationImpl tmp = locListOut.getList().get(i);
            	
         System.out.println("--- Output ListElement #: " + i + " ---");
         System.out.println("ID:\t " + tmp.getId());
         System.out.println("LocName: " + tmp.getLocName());
                
         if (tmp instanceof Cabin)
         {
            Cabin cabinTmp = (Cabin)tmp;
            System.out.println("Class:\t " + tmp.getClass());               	
            System.out.println("Name:\t " + cabinTmp.getName());
            System.out.println("Level:\t " + cabinTmp.getDeckLevel());
            System.out.println("BedCount:" + cabinTmp.getBedCount());
         }
         else if (tmp instanceof TechCabin)
         {
            TechCabin cabinTmp = (TechCabin)tmp;
            System.out.println("Class:\t " + tmp.getClass());               	
            System.out.println("Name:\t " + cabinTmp.getName());
            System.out.println("Level:\t " + cabinTmp.getDeckLevel());
            System.out.println("Purpose: " + cabinTmp.getPurpose());
         }
         else
         {
            fail("!!ERROR casting CABIN!!");
         }
      }
   }
   
}
