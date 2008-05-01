
package org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "echoResponse", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm")
@XmlType(name = "echoResponse", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm")
@XmlAccessorType(XmlAccessType.FIELD)
public class EchoResponse {

    @XmlElement(name = "return", namespace = "")
    private String _return;

    public String getReturn() {
        return this._return;
    }

    public void setReturn(String _return) {
        this._return = _return;
    }

}
