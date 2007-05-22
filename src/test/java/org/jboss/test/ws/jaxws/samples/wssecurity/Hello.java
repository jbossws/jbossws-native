
package org.jboss.test.ws.jaxws.samples.wssecurity;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


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
@WebService(name = "Hello", targetNamespace = "http://org.jboss.ws/samples/wssecurity")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Hello {


    /**
     * 
     * @param user
     * @return
     *     returns org.jboss.test.ws.jaxws.samples.wssecurity.UserType
     */
    @WebMethod
    @WebResult(partName = "return")
    public UserType echoUserType(
        @WebParam(name = "user", partName = "user")
        UserType user);

}
