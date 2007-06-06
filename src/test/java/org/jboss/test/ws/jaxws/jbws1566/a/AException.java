package org.jboss.test.ws.jaxws.jbws1566.a;

public class AException extends Exception {
	int ae;
	
	public AException() {
		super();
	}
	public int getAe() {
		return ae;
	}

	public void setAe(int parentInt) {
		this.ae = parentInt;
	}
	
}
