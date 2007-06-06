package org.jboss.test.ws.jaxws.jbws1566.b;

import org.jboss.test.ws.jaxws.jbws1566.a.AException;

public class BException extends AException {
	int be;
	
	public int getBe() {
		return be;
	}
	public void setBe(int testInt) {
		this.be = testInt;
	}
}
