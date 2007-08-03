/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.jaxrpc.samples.jmstransport;

// $Id$

import java.rmi.RemoteException;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.transport.jms.JMSTransportSupport;

/**
 * An example of a MDB acting as a web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Oct-2004
 */
public class OrganizationJMSEndpoint extends JMSTransportSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(OrganizationJMSEndpoint.class);

   /** Get the contact info */
   public String getContactInfo(String organization) throws RemoteException
   {
      log.info("getContactInfo: " + organization);
      return "The '" + organization + "' boss is currently out of office, please call again.";
   }
}
