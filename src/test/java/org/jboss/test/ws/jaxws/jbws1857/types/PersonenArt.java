
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for personenArt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="personenArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NATUERLICH"/>
 *     &lt;enumeration value="JURISTISCH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "personenArt")
@XmlEnum
public enum PersonenArt {

    NATUERLICH,
    JURISTISCH;

    public String value() {
        return name();
    }

    public static PersonenArt fromValue(String v) {
        return valueOf(v);
    }

}
