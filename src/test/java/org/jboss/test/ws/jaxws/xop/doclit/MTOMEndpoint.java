package org.jboss.test.ws.jaxws.xop.doclit;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

@Remote
@WebService(targetNamespace = "http://org.jboss.ws/xop/doclit")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
@BindingType(value="http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public interface MTOMEndpoint {

   public DHResponse echoDataHandler(DHRequest request);
   public ImageResponse echoImage(ImageRequest request);
   public SourceResponse echoSource(SourceRequest request);   
}
