package org.jboss.test.ws.jaxws.jbws1795.service;

import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1795.handcoded.ListLocation;

@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws1795.service.TravelAgentEndpoint")
public class TravelAgentBean implements TravelAgentEndpoint
{
	
   public ListLocation passLocation(ListLocation listLocation)
   {
      CabinListHandler clh = new CabinListHandler();
      ListLocation list = new ListLocation();
      list = clh.getLocations(listLocation);
      return list;
   }

}
