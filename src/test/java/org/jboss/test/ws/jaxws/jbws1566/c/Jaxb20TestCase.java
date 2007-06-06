package org.jboss.test.ws.jaxws.jbws1566.c;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

import org.jboss.test.ws.jaxws.jbws1566.a.*;
import org.jboss.test.ws.jaxws.jbws1566.b.*;

public class Jaxb20TestCase extends JBossWSTest
{
	public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxwstest/Jaxb20StatelessTestBean";

	public static Test suite()
	{
		return JBossWSTestSetup.newTestSetup(Jaxb20TestCase.class, "jaxws-jbws1566.jar");
	}

	public void testWebService() throws Exception
	{
		URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS+"?wsdl");
		System.out.println("wsdl URL:"+wsdlURL);

		QName serviceName = new QName("http://org.jboss.ws/samples/c", "WebServiceTestService");
		Service service = Service.create(wsdlURL, serviceName);
		Jaxb20TestWSInterface port = service.getPort(Jaxb20TestWSInterface.class);

		BindingProvider bindingProvider = (BindingProvider)port;
		Map<String, Object> reqContext = bindingProvider.getRequestContext();
		reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, TARGET_ENDPOINT_ADDRESS);

		TestEnumeration res=null;
		BClass input = new BClass();
		input.setA(1);
		input.setB("hello service");
		try {
			res = port.testMethod(input);
			assertEquals(res, TestEnumeration.A);
		} catch (BException e) {
			fail("Caught unexpeced TestException: "+e);
		} catch (RemoteException e) {
			fail("Caught unexpeced RemoteException: "+e);
		}
		assertNotNull(res);
	}
}
