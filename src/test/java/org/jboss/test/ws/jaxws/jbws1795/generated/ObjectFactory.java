
package org.jboss.test.ws.jaxws.jbws1795.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.jbws1795.generated package. 
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

    private final static QName _PassLocation_QNAME = new QName("http://service.jbws1795.jaxws.ws.test.jboss.org/", "passLocation");
    private final static QName _PassLocationResponse_QNAME = new QName("http://service.jbws1795.jaxws.ws.test.jboss.org/", "passLocationResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.jbws1795.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ListLocation }
     * 
     */
    public ListLocation createListLocation() {
        return new ListLocation();
    }

    /**
     * Create an instance of {@link PassLocationResponse }
     * 
     */
    public PassLocationResponse createPassLocationResponse() {
        return new PassLocationResponse();
    }

    /**
     * Create an instance of {@link LocationImpl }
     * 
     */
    public LocationImpl createLocationImpl() {
        return new LocationImpl();
    }

    /**
     * Create an instance of {@link TechCabin }
     * 
     */
    public TechCabin createTechCabin() {
        return new TechCabin();
    }

    /**
     * Create an instance of {@link Cabin }
     * 
     */
    public Cabin createCabin() {
        return new Cabin();
    }

    /**
     * Create an instance of {@link PassLocation }
     * 
     */
    public PassLocation createPassLocation() {
        return new PassLocation();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PassLocation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.jbws1795.jaxws.ws.test.jboss.org/", name = "passLocation")
    public JAXBElement<PassLocation> createPassLocation(PassLocation value) {
        return new JAXBElement<PassLocation>(_PassLocation_QNAME, PassLocation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PassLocationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.jbws1795.jaxws.ws.test.jboss.org/", name = "passLocationResponse")
    public JAXBElement<PassLocationResponse> createPassLocationResponse(PassLocationResponse value) {
        return new JAXBElement<PassLocationResponse>(_PassLocationResponse_QNAME, PassLocationResponse.class, null, value);
    }

}
