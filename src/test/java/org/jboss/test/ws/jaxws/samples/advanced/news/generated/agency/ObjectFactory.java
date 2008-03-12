
package org.jboss.test.ws.jaxws.samples.advanced.news.generated.agency;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.samples.news.generated package. 
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

    private final static QName _SubmitPressRelease_QNAME = new QName("http://org.jboss.ws/samples/news", "submitPressRelease");
    private final static QName _SubmitPressReleaseResponse_QNAME = new QName("http://org.jboss.ws/samples/news", "submitPressReleaseResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.samples.news.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SubmitPressReleaseResponse }
     * 
     */
    public SubmitPressReleaseResponse createSubmitPressReleaseResponse() {
        return new SubmitPressReleaseResponse();
    }

    /**
     * Create an instance of {@link PressRelease }
     * 
     */
    public PressRelease createPressRelease() {
        return new PressRelease();
    }

    /**
     * Create an instance of {@link SubmitPressRelease }
     * 
     */
    public SubmitPressRelease createSubmitPressRelease() {
        return new SubmitPressRelease();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitPressRelease }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/news", name = "submitPressRelease")
    public JAXBElement<SubmitPressRelease> createSubmitPressRelease(SubmitPressRelease value) {
        return new JAXBElement<SubmitPressRelease>(_SubmitPressRelease_QNAME, SubmitPressRelease.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitPressReleaseResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/news", name = "submitPressReleaseResponse")
    public JAXBElement<SubmitPressReleaseResponse> createSubmitPressReleaseResponse(SubmitPressReleaseResponse value) {
        return new JAXBElement<SubmitPressReleaseResponse>(_SubmitPressReleaseResponse_QNAME, SubmitPressReleaseResponse.class, null, value);
    }

}
