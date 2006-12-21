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
package org.jboss.test.ws.jaxws.samples.retail;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.samples.retail.cc.CCVerification;
import org.jboss.test.ws.jaxws.samples.retail.cc.CCVerificationService;
import org.jboss.test.ws.jaxws.samples.retail.cc.VerificationRequest;
import org.jboss.test.ws.jaxws.samples.retail.cc.VerificationResponse;
import org.jboss.test.ws.jaxws.samples.retail.profile.DiscountRequest;
import org.jboss.test.ws.jaxws.samples.retail.profile.DiscountResponse;
import org.jboss.test.ws.jaxws.samples.retail.profile.ProfileMgmt;
import org.jboss.test.ws.jaxws.samples.retail.profile.ProfileMgmtService;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Response;

/**
 * An example order management component
 * that offers access though RMI and SOAP
 */
@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.samples.retail.OrderMgmt", name="OrderMgmt", serviceName = "OrderMgmtService")
@HandlerChain(file = "META-INF/jaxws-handler.xml")
public class OrderMgmtBean implements OrderMgmt {

   private static final Logger log = Logger.getLogger(OrderMgmtBean.class);

   @WebServiceRef(wsdlLocation="META-INF/wsdl/CCVerificationService.wsdl")
   private CCVerificationService verificationService;
   private CCVerification verificationPort;

   @WebServiceRef(wsdlLocation="META-INF/wsdl/ProfileMgmtService.wsdl")
   private ProfileMgmtService profileService;
   private ProfileMgmt profilePort;

   @PostConstruct public void initialize(){
      verificationPort = verificationService.getCCVerificationPort();
      profilePort = profileService.getProfileMgmtPort();
   }

   /**
    * Prepare a customer order.
    * This will verify the billing details (i.e. creditcard)
    * and check if the customer qualifies for a discount
    * (applies to high value customers only)
    *
    * @param order
    * @return OrderStaus
    */
   public OrderStatus prepareOrder(Order order)
   {

      log.info("Preparing order " + order);

      // verify billing details
      String creditCard = order.getCustomer().getCreditCardDetails();
      Response<Boolean> response = verificationPort.verifyAsync( creditCard );

      // high value customer discount
      DiscountRequest discountRequest = new DiscountRequest(order.getCustomer());
      DiscountResponse discount = profilePort.getCustomerDiscount(discountRequest);
      boolean hasDiscount = discount.getDiscount() > 0.00;
      log.info("High value customer ? " + hasDiscount);

      try
      {
         log.info(creditCard + " valid? " + response.get());
      }
      catch (Exception e)
      {
         log.error("Failed to access async results", e);
      }

      // transition to prepared state
      order.setState(Order.OrderState.PREPARED);

      // done
      //return new OrderStatus("Prepared", order.getOrderNum(), discount.getDiscount());
      return new OrderStatus("Prepared", order.getOrderNum(), 0);
   }


}
