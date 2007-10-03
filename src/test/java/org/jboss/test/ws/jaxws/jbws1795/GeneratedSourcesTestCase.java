package org.jboss.test.ws.jaxws.jbws1795;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws1795.generated.TravelAgentBeanService;
import org.jboss.test.ws.jaxws.jbws1795.generated.TravelAgentEndpoint;
import org.jboss.test.ws.jaxws.jbws1795.generated.ListLocation;
import org.jboss.test.ws.jaxws.jbws1795.generated.LocationImpl;
import org.jboss.test.ws.jaxws.jbws1795.generated.Cabin;
import org.jboss.test.ws.jaxws.jbws1795.generated.TechCabin;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class GeneratedSourcesTestCase extends JBossWSTest
{
   
   private TravelAgentEndpoint tae = null;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(GeneratedSourcesTestCase.class, "jaxws-jbws1795.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      TravelAgentBeanService travelAgentBeanService = new TravelAgentBeanService();
      tae = travelAgentBeanService.getTravelAgentBeanPort();
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
        
      for (int i = 0; i < locListOut.getList().size(); i++)
      {
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
