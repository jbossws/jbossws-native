package org.jboss.test.ws.jaxws.jbws2014;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;

public class TestDto
{
   private DataHandler content;
   private String id;
   
   @XmlMimeType("text/plain")
   public DataHandler getContent()
   {
      return content;
   }
   public void setContent(DataHandler content)
   {
      this.content = content;
   }
   public String getId()
   {
      return id;
   }
   public void setId(String id)
   {
      this.id = id;
   }
}
