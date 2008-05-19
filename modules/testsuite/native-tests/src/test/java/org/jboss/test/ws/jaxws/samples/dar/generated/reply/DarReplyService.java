
package org.jboss.test.ws.jaxws.samples.dar.generated.reply;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-b03-
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "DarReplyService", targetNamespace = "http://org.jboss.ws/samples/dar", wsdlLocation = "file:/home/alessio/Desktop/Documenti%20asynch/reply.wsdl")
public class DarReplyService
    extends Service
{

    private final static URL DARREPLYSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("file:/home/alessio/Desktop/Documenti%20asynch/reply.wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        DARREPLYSERVICE_WSDL_LOCATION = url;
    }

    public DarReplyService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DarReplyService() {
        super(DARREPLYSERVICE_WSDL_LOCATION, new QName("http://org.jboss.ws/samples/dar", "DarReplyService"));
    }

    /**
     * 
     * @return
     *     returns DarReplyEndpoint
     */
    @WebEndpoint(name = "DarReplyEndpointPort")
    public DarReplyEndpoint getDarReplyEndpointPort() {
        return (DarReplyEndpoint)super.getPort(new QName("http://org.jboss.ws/samples/dar", "DarReplyEndpointPort"), DarReplyEndpoint.class);
    }

}