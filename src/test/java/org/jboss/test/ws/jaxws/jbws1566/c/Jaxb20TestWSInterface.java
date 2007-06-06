package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;

import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;

@javax.jws.WebService(
		targetNamespace = "http://org.jboss.ws/samples/c"
)
@javax.jws.soap.SOAPBinding(
		style = javax.jws.soap.SOAPBinding.Style.DOCUMENT,
		use = javax.jws.soap.SOAPBinding.Use.LITERAL,
		parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED)
public interface Jaxb20TestWSInterface extends java.rmi.Remote {

	@javax.jws.WebMethod(operationName = "TestMethod")
	public @javax.jws.WebResult(name="result") TestEnumeration testMethod(
			@javax.jws.WebParam(name="input", mode=javax.jws.WebParam.Mode.IN)
			BClass input
	) throws BException, RemoteException;

}