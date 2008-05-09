
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for languageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="languageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IT"/>
 *     &lt;enumeration value="FR"/>
 *     &lt;enumeration value="DE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "languageType")
@XmlEnum
public enum LanguageType {

    IT,
    FR,
    DE;

    public String value() {
        return name();
    }

    public static LanguageType fromValue(String v) {
        return valueOf(v);
    }

}
