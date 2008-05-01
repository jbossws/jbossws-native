
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for adressTyp.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="adressTyp">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SCHULDNER"/>
 *     &lt;enumeration value="AUSSTELLENDE_BEHOERDE"/>
 *     &lt;enumeration value="BETREIBUNGS_KONKURSAMT"/>
 *     &lt;enumeration value="GLAEUBIGER_VERTRETER"/>
 *     &lt;enumeration value="GLAEUBIGER"/>
 *     &lt;enumeration value="MANDANT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "adressTyp")
@XmlEnum
public enum AdressTyp {

    SCHULDNER,
    AUSSTELLENDE_BEHOERDE,
    BETREIBUNGS_KONKURSAMT,
    GLAEUBIGER_VERTRETER,
    GLAEUBIGER,
    MANDANT;

    public String value() {
        return name();
    }

    public static AdressTyp fromValue(String v) {
        return valueOf(v);
    }

}
