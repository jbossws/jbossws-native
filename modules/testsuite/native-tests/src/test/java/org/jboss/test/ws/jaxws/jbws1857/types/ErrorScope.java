
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorScope.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorScope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="VICLIENT"/>
 *     &lt;enumeration value="HOST"/>
 *     &lt;enumeration value="VIAPP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorScope")
@XmlEnum
public enum ErrorScope {

    VICLIENT,
    HOST,
    VIAPP;

    public String value() {
        return name();
    }

    public static ErrorScope fromValue(String v) {
        return valueOf(v);
    }

}
