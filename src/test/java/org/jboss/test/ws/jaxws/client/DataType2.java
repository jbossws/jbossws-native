
package org.jboss.test.ws.jaxws.client;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import javax.xml.transform.Source;
import java.awt.*;


/**
 * <p>Java class for DataType2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataType2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="docName1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docName2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docName3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docName4" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl4" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl11" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl12" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl13" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docUrl14" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="doc1" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="doc2" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="doc3" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="doc4" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="result" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataType2", propOrder = {
    "docName1",
    "docName2",
    "docName3",
    "docName4",
    "docUrl1",
    "docUrl2",
    "docUrl3",
    "docUrl4",
    "docUrl11",
    "docUrl12",
    "docUrl13",
    "docUrl14",
    "doc1",
    "doc2",
    "doc3",
    "doc4",
    "result"
})
public class DataType2 {

    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docName1;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docName2;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docName3;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docName4;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl1;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl2;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl3;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl4;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl11;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl12;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl13;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String docUrl14;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    @XmlMimeType("text/xml")
    protected Source doc1;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    @XmlMimeType("application/xml")
    protected Source doc2;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    @XmlMimeType("text/html")
    protected DataHandler doc3;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    @XmlMimeType("image/jpeg")
    protected Image doc4;
    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    protected String result;

    /**
     * Gets the value of the docName1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocName1() {
        return docName1;
    }

    /**
     * Sets the value of the docName1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocName1(String value) {
        this.docName1 = value;
    }

    /**
     * Gets the value of the docName2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocName2() {
        return docName2;
    }

    /**
     * Sets the value of the docName2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocName2(String value) {
        this.docName2 = value;
    }

    /**
     * Gets the value of the docName3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocName3() {
        return docName3;
    }

    /**
     * Sets the value of the docName3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocName3(String value) {
        this.docName3 = value;
    }

    /**
     * Gets the value of the docName4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocName4() {
        return docName4;
    }

    /**
     * Sets the value of the docName4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocName4(String value) {
        this.docName4 = value;
    }

    /**
     * Gets the value of the docUrl1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl1() {
        return docUrl1;
    }

    /**
     * Sets the value of the docUrl1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl1(String value) {
        this.docUrl1 = value;
    }

    /**
     * Gets the value of the docUrl2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl2() {
        return docUrl2;
    }

    /**
     * Sets the value of the docUrl2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl2(String value) {
        this.docUrl2 = value;
    }

    /**
     * Gets the value of the docUrl3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl3() {
        return docUrl3;
    }

    /**
     * Sets the value of the docUrl3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl3(String value) {
        this.docUrl3 = value;
    }

    /**
     * Gets the value of the docUrl4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl4() {
        return docUrl4;
    }

    /**
     * Sets the value of the docUrl4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl4(String value) {
        this.docUrl4 = value;
    }

    /**
     * Gets the value of the docUrl11 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl11() {
        return docUrl11;
    }

    /**
     * Sets the value of the docUrl11 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl11(String value) {
        this.docUrl11 = value;
    }

    /**
     * Gets the value of the docUrl12 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl12() {
        return docUrl12;
    }

    /**
     * Sets the value of the docUrl12 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl12(String value) {
        this.docUrl12 = value;
    }

    /**
     * Gets the value of the docUrl13 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl13() {
        return docUrl13;
    }

    /**
     * Sets the value of the docUrl13 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl13(String value) {
        this.docUrl13 = value;
    }

    /**
     * Gets the value of the docUrl14 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocUrl14() {
        return docUrl14;
    }

    /**
     * Sets the value of the docUrl14 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocUrl14(String value) {
        this.docUrl14 = value;
    }

    /**
     * Gets the value of the doc1 property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getDoc1() {
        return doc1;
    }

    /**
     * Sets the value of the doc1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setDoc1(Source value) {
        this.doc1 = value;
    }

    /**
     * Gets the value of the doc2 property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getDoc2() {
        return doc2;
    }

    /**
     * Sets the value of the doc2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setDoc2(Source value) {
        this.doc2 = value;
    }

    /**
     * Gets the value of the doc3 property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getDoc3() {
        return doc3;
    }

    /**
     * Sets the value of the doc3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setDoc3(DataHandler value) {
        this.doc3 = value;
    }

    /**
     * Gets the value of the doc4 property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getDoc4() {
        return doc4;
    }

    /**
     * Sets the value of the doc4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setDoc4(Image value) {
        this.doc4 = value;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
    }

}
