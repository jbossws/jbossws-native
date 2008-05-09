
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="VALIDATION_EXCEPTION"/>
 *     &lt;enumeration value="BUSINESS_WARNING"/>
 *     &lt;enumeration value="HOST_BUSINESS_EXCEPTION_1"/>
 *     &lt;enumeration value="DEFAULT_TECHNICAL_EXCEPTION"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorCode")
@XmlEnum
public enum ErrorCode {

    VALIDATION_EXCEPTION,
    BUSINESS_WARNING,
    HOST_BUSINESS_EXCEPTION_1,
    DEFAULT_TECHNICAL_EXCEPTION;

    public String value() {
        return name();
    }

    public static ErrorCode fromValue(String v) {
        return valueOf(v);
    }

}
