/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.jaxws.samples.jmstransport;

import java.rmi.RemoteException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.core.transport.jms.JMSTransportSupportEJB3;
import org.jboss.wsf.spi.annotation.WebContext;

/**
 * An example of a MDB acting as a web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Jan-2008
 */
@WebService (targetNamespace = "http://org.jboss.ws/samples/jmstransport")
@WebContext (contextRoot = "/jaxws-samples-jmstransport")
@SOAPBinding(style = SOAPBinding.Style.RPC)

@MessageDriven(activationConfig = { 
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/RequestQueue")
  },
  messageListenerInterface = javax.jms.MessageListener.class
)
public class OrganizationJMSEndpoint extends JMSTransportSupportEJB3
{
   // provide logging
   private static final Logger log = Logger.getLogger(OrganizationJMSEndpoint.class);

   @WebMethod
   public String getContactInfo(String organization) throws RemoteException
   {
      log.info("getContactInfo: " + organization);
      return "The '" + organization + "' boss is currently out of office, please call again.";
   }

   @Override
   public void onMessage(Message message)
   {
      log.info("onMessage: " + message);
      super.onMessage(message);
   }
}
