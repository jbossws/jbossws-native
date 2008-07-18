package org.jboss.test.ws.jaxws.jbws2182;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Dto
{
   @XmlElement(namespace="http://org.jboss.ws/jbws2182")
   private String par1;
   @XmlElement(namespace="http://org.jboss.ws/jbws2182")
   private String par2;
   
   public String getPar1()
   {
      return par1;
   }
   public void setPar1(String par1)
   {
      this.par1 = par1;
   }
   public String getPar2()
   {
      return par2;
   }
   public void setPar2(String par2)
   {
      this.par2 = par2;
   }
}
