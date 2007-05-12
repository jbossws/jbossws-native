
package org.jboss.test.ws.jaxws.serviceref;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


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
@WebService(name = "TestEndpoint", targetNamespace = "http://serviceref.jaxws.ws.test.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestEndpoint {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(partName = "return")
    public String echo(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0);

}
