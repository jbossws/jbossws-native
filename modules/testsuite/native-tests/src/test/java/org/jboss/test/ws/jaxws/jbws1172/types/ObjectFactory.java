
package org.jboss.test.ws.jaxws.jbws1172.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.jbws1172.types package. 
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

    private final static QName _PerformTest_QNAME = new QName("http://www.my-company.it/ws/my-test", "performTest");
    private final static QName _PerformTestResponse_QNAME = new QName("http://www.my-company.it/ws/my-test", "performTestResponse");
    private final static QName _MyWSException_QNAME = new QName("http://www.my-company.it/ws/my-test", "MyWSException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.jbws1172.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PerformTestResponse }
     * 
     */
    public PerformTestResponse createPerformTestResponse() {
        return new PerformTestResponse();
    }

    /**
     * Create an instance of {@link PerformTest }
     * 
     */
    public PerformTest createPerformTest() {
        return new PerformTest();
    }

    /**
     * Create an instance of {@link MyWSException }
     * 
     */
    public MyWSException createMyWSException() {
        return new MyWSException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformTest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.my-company.it/ws/my-test", name = "performTest")
    public JAXBElement<PerformTest> createPerformTest(PerformTest value) {
        return new JAXBElement<PerformTest>(_PerformTest_QNAME, PerformTest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformTestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.my-company.it/ws/my-test", name = "performTestResponse")
    public JAXBElement<PerformTestResponse> createPerformTestResponse(PerformTestResponse value) {
        return new JAXBElement<PerformTestResponse>(_PerformTestResponse_QNAME, PerformTestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MyWSException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.my-company.it/ws/my-test", name = "MyWSException")
    public JAXBElement<MyWSException> createMyWSException(MyWSException value) {
        return new JAXBElement<MyWSException>(_MyWSException_QNAME, MyWSException.class, null, value);
    }

}
