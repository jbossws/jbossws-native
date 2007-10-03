package org.jboss.test.ws.jaxws.jbws1795.handcoded;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Cabin.class, TechCabin.class})
public class LocationImpl implements Serializable, Location
{

   private static final long serialVersionUID = 1L;
   private int id;
   private String locName;

   public int getId()
   {
      return id;
   }

   public void setId(int pk) 
   {
      id = pk;
   }

   public String getLocName()
   {
      return locName;
   }
   
   public void setLocName(String str)
   {
      locName = str;
   }
	
}
