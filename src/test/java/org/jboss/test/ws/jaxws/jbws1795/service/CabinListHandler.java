package org.jboss.test.ws.jaxws.jbws1795.service;

import org.jboss.test.ws.jaxws.jbws1795.handcoded.Cabin;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.ListLocation;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.Location;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.LocationImpl;
import org.jboss.test.ws.jaxws.jbws1795.handcoded.TechCabin;

public class CabinListHandler
{

   public CabinListHandler()
   {
   }
	
   public ListLocation getLocations(ListLocation listLocation)
   {

      for (int i = 0; i < listLocation.getList().size(); i++)
      {
         Location loc = listLocation.getList().get(i);

         if (loc instanceof TechCabin)
         {
            System.out.println("--- TechCabin Argument ---");
         }
         else if (loc instanceof Cabin)
         {
            System.out.println("--- Cabin Argument ---");
         }
         else if (loc instanceof LocationImpl)
         {
            System.out.println("--- LocationImpl Argument ---");
         }
         else
         {
            System.out.println("--- ERROR RECIEVING OBJECT GRAPH ---");
         }
      }
      
      return listLocation;
   }
   
}
