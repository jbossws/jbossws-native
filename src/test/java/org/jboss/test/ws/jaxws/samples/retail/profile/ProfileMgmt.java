
package org.jboss.test.ws.jaxws.samples.retail.profile;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


@Remote
@WebService(name = "ProfileMgmt", targetNamespace = "http://org.jboss.ws/samples/retail/profile")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ProfileMgmt {

    @WebMethod
    public DiscountResponse getCustomerDiscount(DiscountRequest request);

}
