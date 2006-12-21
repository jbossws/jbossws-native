
package org.jboss.test.ws.jaxws.samples.retail.cc;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.Response;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.1-10/21/2006 12:56 AM(vivek)-EA2
 * Generated source version: 2.0
 *
 */
@WebService(name = "CCVerification", targetNamespace = "http://org.jboss.ws/samples/retail/cc")
public interface CCVerification {


    /**
     *
     * @param creditCardNumber
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(name = "verified", targetNamespace = "")
    @RequestWrapper(localName = "verify", targetNamespace = "http://org.jboss.ws/samples/retail/cc", className = "org.jboss.test.ws.jaxws.samples.retail.cc.VerificationRequest")
    @ResponseWrapper(localName = "verifyResponse", targetNamespace = "http://org.jboss.ws/samples/retail/cc", className = "org.jboss.test.ws.jaxws.samples.retail.cc.VerificationResponse")
    public Boolean verify(
        @WebParam(name = "creditCardNumber", targetNamespace = "")
        String creditCardNumber);

    Response<Boolean> verifyAsync(String creditCardNumber);

}
