
package org.jboss.test.ws.jaxws.samples.wssecurity;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * The SEI
 *
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 * @since 15-Aug-2007
 * @version $Revision$ 
 */
@WebService(name = "UsernameEndpoint", targetNamespace = "http://org.jboss.ws/samples/wssecurity")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface UsernameEndpoint {

    /**
     * 
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(partName = "return")
    public String getUsernameToken();

}
