package org.jboss.test.ws.jaxws.wspolicy;

import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.annotation.Policy;
import org.jboss.ws.extensions.policy.annotation.PolicyAttachment;

@PolicyAttachment({@Policy( policyFileLocation="AnnotationPortPolicy.xml",
                            scope = PolicyScopeLevel.WSDL_PORT ),
                   @Policy( policyFileLocation="AnnotationPortPolicy2.xml",
                            scope = PolicyScopeLevel.WSDL_PORT ),
                   @Policy( policyFileLocation="AnnotationBindingPolicy.xml",
                            scope = PolicyScopeLevel.WSDL_BINDING ),
                   @Policy( policyFileLocation="AnnotationBindingPolicy2.xml",
                            scope = PolicyScopeLevel.WSDL_BINDING ),
                   @Policy( policyFileLocation="AnnotationPortTypePolicy.xml",
                            scope = PolicyScopeLevel.WSDL_PORT_TYPE ),
                   @Policy( policyFileLocation="AnnotationPortTypePolicy2.xml",
                            scope = PolicyScopeLevel.WSDL_PORT_TYPE )})
public class TestMultipleEndpointPolicy
{

}
