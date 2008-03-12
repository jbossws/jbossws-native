
package org.jboss.test.ws.jaxws.samples.advanced.news.generated.printer.swa;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.samples.news.generated.printer.swa package. 
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

    private final static QName _EditionSWA_QNAME = new QName("http://org.jboss.ws/samples/news", "editionSWA");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.samples.news.generated.printer.swa
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EditionSWA }
     * 
     */
    public EditionSWA createEditionSWA() {
        return new EditionSWA();
    }

    /**
     * Create an instance of {@link StringArray }
     * 
     */
    public StringArray createStringArray() {
        return new StringArray();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EditionSWA }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/news", name = "editionSWA")
    public JAXBElement<EditionSWA> createEditionSWA(EditionSWA value) {
        return new JAXBElement<EditionSWA>(_EditionSWA_QNAME, EditionSWA.class, null, value);
    }

}
