
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for haftungArt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="haftungArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SOLIDARHAFTUNG"/>
 *     &lt;enumeration value="EINZELHAFTUNG"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "haftungArt")
@XmlEnum
public enum HaftungArt {

    SOLIDARHAFTUNG,
    EINZELHAFTUNG,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static HaftungArt fromValue(String v) {
        return valueOf(v);
    }

}
