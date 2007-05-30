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
package org.jboss.test.ws.jaxws.wspolicy;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.policy.Policy;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.Constants;
import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.deployer.PolicyDeployer;
import org.jboss.ws.extensions.policy.deployer.domainAssertion.NopAssertionDeployer;
import org.jboss.ws.extensions.policy.metadata.PolicyMetaDataBuilder;
import org.jboss.ws.extensions.policy.metadata.PolicyMetaExtension;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * @author Alessio Soldano, <mailto:alessio.soldano@javalinux.it>
 *
 * since 29-May-2007
 */
public class PolicyMetaDataBuilderTestCase extends JBossWSTest
{
   
   private WSDLDefinitions readWsdl(String filename) throws Exception
   {
      File wsdlFile = new File(filename);
      assertTrue(wsdlFile.exists());
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());
      assertNotNull(wsdlDefinitions);
      return wsdlDefinitions;
   }
   
   public void testEndpointScopePolicies() throws Exception
   {
      WSDLDefinitions wsdlDefinitions = readWsdl("resources/jaxws/wspolicy/TestService.wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/endpoint", "TestService");
      ServiceMetaData serviceMetaData = new ServiceMetaData(null, serviceName);
      QName portName = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointInterfacePort");
      QName portTypeName = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointInterface");
      EndpointMetaData epMetaData = new ServerEndpointMetaData(serviceMetaData,portName,portTypeName,Type.JAXWS);
      
      Map<String,Class> map = new HashMap<String,Class>();
      map.put("http://schemas.xmlsoap.org/ws/2005/02/rm/policy", NopAssertionDeployer.class);
      map.put("http://www.fabrikam123.example.com/stock", NopAssertionDeployer.class);
      map.put("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", NopAssertionDeployer.class);
      PolicyDeployer deployer = PolicyDeployer.newInstance(map);
      PolicyMetaDataBuilder builder = new PolicyMetaDataBuilder(deployer);
      
      builder.processPolicyExtensions(epMetaData, wsdlDefinitions);
      
      PolicyMetaExtension policyExt = (PolicyMetaExtension)epMetaData.getExtension(Constants.URI_WS_POLICY);
      Collection<Policy> bindingPolicies = policyExt.getPolicies(PolicyScopeLevel.WSDL_BINDING);
      assertNotNull(bindingPolicies);
      assertEquals(2, bindingPolicies.size());
      Iterator<Policy> bindingPoliciesIterator = bindingPolicies.iterator();
      String id1 = bindingPoliciesIterator.next().getId();
      String id2 = bindingPoliciesIterator.next().getId();
      assertTrue(("RmPolicy".equalsIgnoreCase(id1) && "X509EndpointPolicy".equalsIgnoreCase(id2)) ||
            ("RmPolicy".equalsIgnoreCase(id2) && "X509EndpointPolicy".equalsIgnoreCase(id1)));
      
      Collection<Policy> portPolicies = policyExt.getPolicies(PolicyScopeLevel.WSDL_PORT);
      assertNotNull(portPolicies);
      assertEquals(1, portPolicies.size());
      assertEquals("uselessPortPolicy", portPolicies.iterator().next().getId());
      
      Collection<Policy> portTypePolicies = policyExt.getPolicies(PolicyScopeLevel.WSDL_PORT_TYPE);
      assertNotNull(portTypePolicies);
      assertEquals(2, portTypePolicies.size());
      Iterator<Policy> portTypePoliciesIterator = portTypePolicies.iterator();
      String id3 = portTypePoliciesIterator.next().getId();
      String id4 = portTypePoliciesIterator.next().getId();
      assertTrue(("uselessPortTypePolicy".equalsIgnoreCase(id3) && "uselessPortTypePolicy2".equalsIgnoreCase(id4)) ||
            ("uselessPortTypePolicy".equalsIgnoreCase(id4) && "uselessPortTypePolicy2".equalsIgnoreCase(id3)));
   }
   
}
