
package org.jboss.test.ws.jaxws.serviceref;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * JBossWS Generated Source
 * 
 * Generation Date: Mon Mar 12 15:09:39 CET 2007
 * 
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 * 
 * JAX-WS Version: 2.0
 * 
 */
@WebServiceClient(name = "TestEndpointService", targetNamespace = "http://serviceref.jaxws.ws.test.jboss.org/", wsdlLocation = "http://tddell:8080/jaxws-serviceref?wsdl")
public class TestEndpointService
    extends Service
{

    private final static URL TESTENDPOINTSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://tddell:8080/jaxws-serviceref?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TESTENDPOINTSERVICE_WSDL_LOCATION = url;
    }

    public TestEndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public TestEndpointService() {
        super(TESTENDPOINTSERVICE_WSDL_LOCATION, new QName("http://serviceref.jaxws.ws.test.jboss.org/", "TestEndpointService"));
    }

    /**
     * 
     * @return
     *     returns TestEndpoint
     */
    @WebEndpoint(name = "TestEndpointPort")
    public TestEndpoint getTestEndpointPort() {
        return (TestEndpoint)super.getPort(new QName("http://serviceref.jaxws.ws.test.jboss.org/", "TestEndpointPort"), TestEndpoint.class);
    }

}
