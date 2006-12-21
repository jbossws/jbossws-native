
package org.jboss.test.ws.jaxws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.jboss.test.ws.jaxws.client.DataType;
import org.jboss.test.ws.jaxws.client.DataType2;
import org.jboss.test.ws.jaxws.client.DataType3;
import org.jboss.test.ws.jaxws.client.ObjectFactory;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MTOMOut_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMOut");
    private final static QName _MTOMOut2Response_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMOut2Response");
    private final static QName _MTOMIn2_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMIn2");
    private final static QName _MTOMIn_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMIn");
    private final static QName _MTOMInOutResponse_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMInOutResponse");
    private final static QName _MTOMInResponse_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMInResponse");
    private final static QName _MTOMOutResponse_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMOutResponse");
    private final static QName _MTOMIn2Response_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMIn2Response");
    private final static QName _MTOMInOut_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMInOut");
    private final static QName _MTOMOut2_QNAME = new QName("http://mtomtestservice.org/xsd", "MTOMOut2");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DataType3 }
     * 
     */
    public DataType3 createDataType3() {
        return new DataType3();
    }

    /**
     * Create an instance of {@link DataType }
     * 
     */
    public DataType createDataType() {
        return new DataType();
    }

    /**
     * Create an instance of {@link DataType2 }
     * 
     */
    public DataType2 createDataType2() {
        return new DataType2();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMOut")
    public JAXBElement<String> createMTOMOut(String value) {
        return new JAXBElement<String>(_MTOMOut_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType3 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMOut2Response")
    public JAXBElement<DataType3> createMTOMOut2Response(DataType3 value) {
        return new JAXBElement<DataType3>(_MTOMOut2Response_QNAME, DataType3 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType3 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMIn2")
    public JAXBElement<DataType3> createMTOMIn2(DataType3 value) {
        return new JAXBElement<DataType3>(_MTOMIn2_QNAME, DataType3 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMIn")
    public JAXBElement<DataType> createMTOMIn(DataType value) {
        return new JAXBElement<DataType>(_MTOMIn_QNAME, DataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType2 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMInOutResponse")
    public JAXBElement<DataType2> createMTOMInOutResponse(DataType2 value) {
        return new JAXBElement<DataType2>(_MTOMInOutResponse_QNAME, DataType2 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMInResponse")
    public JAXBElement<String> createMTOMInResponse(String value) {
        return new JAXBElement<String>(_MTOMInResponse_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMOutResponse")
    public JAXBElement<DataType> createMTOMOutResponse(DataType value) {
        return new JAXBElement<DataType>(_MTOMOutResponse_QNAME, DataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMIn2Response")
    public JAXBElement<String> createMTOMIn2Response(String value) {
        return new JAXBElement<String>(_MTOMIn2Response_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataType2 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMInOut")
    public JAXBElement<DataType2> createMTOMInOut(DataType2 value) {
        return new JAXBElement<DataType2>(_MTOMInOut_QNAME, DataType2 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mtomtestservice.org/xsd", name = "MTOMOut2")
    public JAXBElement<String> createMTOMOut2(String value) {
        return new JAXBElement<String>(_MTOMOut2_QNAME, String.class, null, value);
    }

}
