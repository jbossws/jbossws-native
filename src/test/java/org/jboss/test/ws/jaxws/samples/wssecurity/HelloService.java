
package org.jboss.test.ws.jaxws.samples.wssecurity;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * JBossWS Generated Source
 * 
 * Generation Date: Mon May 21 19:38:54 CEST 2007
 * 
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 * 
 * JAX-WS Version: 2.0
 * 
 */
@WebServiceClient(name = "HelloService", targetNamespace = "http://org.jboss.ws/samples/wssecurity", wsdlLocation = "file:/home/tdiesler/svn/jbossws/trunk/jbossws-core/src/test/resources/jaxws/samples/wssecurity/META-INF/wsdl/HelloService.wsdl")
public class HelloService
    extends Service
{

    private final static URL HELLOSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("file:/home/tdiesler/svn/jbossws/trunk/jbossws-core/src/test/resources/jaxws/samples/wssecurity/META-INF/wsdl/HelloService.wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HELLOSERVICE_WSDL_LOCATION = url;
    }

    public HelloService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HelloService() {
        super(HELLOSERVICE_WSDL_LOCATION, new QName("http://org.jboss.ws/samples/wssecurity", "HelloService"));
    }

    /**
     * 
     * @return
     *     returns Hello
     */
    @WebEndpoint(name = "HelloPort")
    public Hello getHelloPort() {
        return (Hello)super.getPort(new QName("http://org.jboss.ws/samples/wssecurity", "HelloPort"), Hello.class);
    }

}
