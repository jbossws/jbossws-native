/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.extensions.addressing.policy;

import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.AddressingFeature.Responses;

import org.apache.ws.policy.PrimitiveAssertion;
import org.jboss.ws.extensions.policy.deployer.domainAssertion.AssertionDeployer;
import org.jboss.ws.extensions.policy.deployer.exceptions.UnsupportedAssertion;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ExtensibleMetaData;
import org.jboss.ws.api.addressing.AddressingConstants;

/**
 * Associates AddressingFeature with endpoint meta data if not set yet.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class AddressingPolicyAssertionDeployer implements AssertionDeployer
{
   
   @Override
   public void deployClientSide(final PrimitiveAssertion assertion, final ExtensibleMetaData extensibleMD)
         throws UnsupportedAssertion
   {
      this.deploy(assertion, extensibleMD);
   }

   @Override
   public void deployServerSide(final PrimitiveAssertion assertion, final ExtensibleMetaData extensibleMD)
         throws UnsupportedAssertion
   {
      this.deploy(assertion, extensibleMD);
   }

   private static void deploy(final PrimitiveAssertion assertion, final ExtensibleMetaData extensibleMD)
   throws UnsupportedAssertion
   {
      if (extensibleMD instanceof EndpointMetaData)
      {
         final EndpointMetaData endpointMD = (EndpointMetaData) extensibleMD;
         final AddressingFeature addressingFeature = endpointMD.getFeature(AddressingFeature.class);
         
         if (addressingFeature == null)
         {
            final boolean enabled = true;
            final boolean required = isRequired(assertion);
            final Responses responses = getResponses(assertion);
            
            endpointMD.addFeature(new AddressingFeature(enabled, required, responses));
         }
      }
   }
   
   private static boolean isRequired(final PrimitiveAssertion assertion)
   {
      Object optionalValue = assertion.getAttribute(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "Optional"));
      
      if (optionalValue == null)
         return true;
      
      return !Boolean.parseBoolean(optionalValue.toString());
   }
   
   @SuppressWarnings("unchecked")
   private static Responses getResponses(final PrimitiveAssertion assertion)
   {
      final QName assertionQName = assertion.getName();
      
      if (AddressingConstants.Metadata.Elements.ANONYMOUSRESPONSES_QNAME.equals(assertionQName))
      {
         return AddressingFeature.Responses.ANONYMOUS;
      }
      if (AddressingConstants.Metadata.Elements.NONANONYMOUSRESPONSES_QNAME.equals(assertionQName))
      {
         return AddressingFeature.Responses.NON_ANONYMOUS;
      }
      
      List terms = assertion.getTerms();
      if (terms != null)
      {
         for (final Object term : terms)
         {
            if (term instanceof PrimitiveAssertion)
            {
               return getResponses((PrimitiveAssertion) term);
            }
         }
      }
      
      return AddressingFeature.Responses.ALL;
   }
   
}
