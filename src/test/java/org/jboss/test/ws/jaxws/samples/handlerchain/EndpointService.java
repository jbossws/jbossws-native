package org.jboss.test.ws.jaxws.samples.handlerchain;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class EndpointService extends Service
{
   public EndpointService(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public Endpoint getEndpointPort()
   {
      return (Endpoint)super.getPort(Endpoint.class);
   }

}
