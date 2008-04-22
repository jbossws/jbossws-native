
package org.jboss.test.ws.jaxws.samples.wsa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "sayHelloResponse", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsaddressing")
@XmlType(name = "sayHelloResponse", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsaddressing")
@XmlAccessorType(XmlAccessType.FIELD)
public class SayHelloResponse {

    @XmlElement(name = "return", namespace = "")
    private String _return;

    public String getReturn() {
        return this._return;
    }

    public void setReturn(String _return) {
        this._return = _return;
    }

}
