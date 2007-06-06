package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;

import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;
import org.jboss.wsf.spi.annotation.WebContext;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(
		endpointInterface = "org.jboss.test.ws.jaxws.jbws1566.c.Jaxb20TestWSInterface",
		targetNamespace = "http://org.jboss.ws/samples/c",
		serviceName = "WebServiceTestService",
		portName = "WebServiceTestPort"
)
@WebContext(
		contextRoot = "/jaxwstest"
		, urlPattern="/Jaxb20StatelessTestBean/*"
		, secureWSDLAccess=false
)
public class Jaxb20StatelessTestBean  implements Jaxb20TestWSInterface {

	public TestEnumeration testMethod(BClass input) throws BException, RemoteException {
		System.out.println("Got input: "+input +": a="+input.getA()+", b="+input.getB());
		if (input.getA()==0) {
			BException ex = new BException();
			ex.setAe(11);
			ex.setBe(13);
			throw ex;
		}
		return TestEnumeration.A;
	}
}
