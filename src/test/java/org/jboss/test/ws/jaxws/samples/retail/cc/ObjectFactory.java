
package org.jboss.test.ws.jaxws.samples.retail.cc;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.samples.retail.cc package. 
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

    private final static QName _VerifyResponse_QNAME = new QName("http://org.jboss.ws/samples/retail/cc", "verifyResponse");
    private final static QName _Verify_QNAME = new QName("http://org.jboss.ws/samples/retail/cc", "verify");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.samples.retail.cc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link VerificationRequest }
     * 
     */
    public VerificationRequest createVerificationRequest() {
        return new VerificationRequest();
    }

    /**
     * Create an instance of {@link VerificationResponse }
     * 
     */
    public VerificationResponse createVerificationResponse() {
        return new VerificationResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/cc", name = "verifyResponse")
    public JAXBElement<VerificationResponse> createVerifyResponse(VerificationResponse value) {
        return new JAXBElement<VerificationResponse>(_VerifyResponse_QNAME, VerificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificationRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/cc", name = "verify")
    public JAXBElement<VerificationRequest> createVerify(VerificationRequest value) {
        return new JAXBElement<VerificationRequest>(_Verify_QNAME, VerificationRequest.class, null, value);
    }

}
