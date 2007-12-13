package org.jboss.test.ws.jaxws.jbws1904;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

@WebFault(name="TestExceptionFault", targetNamespace="http://org.jboss.ws/jbws1904/faults")
@XmlType(name = "TestException", namespace = "http://org.jboss.ws/jbws1904/exceptions", propOrder = {"message"})
public class TestException extends Exception
{
   private String message;

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }
}
