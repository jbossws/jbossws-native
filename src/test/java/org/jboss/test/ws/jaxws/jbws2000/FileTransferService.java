package org.jboss.test.ws.jaxws.jbws2000;


import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

@WebService(name = "FileTransferService", targetNamespace = "http://service.mtom.test.net/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public interface FileTransferService {
    @WebMethod
    boolean transferFile(String fileName, DataHandler contents);

}
