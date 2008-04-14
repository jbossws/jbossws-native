
package org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "echo", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm")
@XmlType(name = "echo", namespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Echo {

    @XmlElement(name = "arg0", namespace = "")
    private String arg0;

    public String getArg0() {
        return this.arg0;
    }

    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

}
