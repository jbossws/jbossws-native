package org.jboss.test.ws.jaxws.jbws1795.handcoded;

public class TechCabin extends LocationImpl implements java.io.Serializable 
{
   
   private static final long serialVersionUID = 1L;
   private String name;
   private int deckLevel;
   private int shipId;
   private String purpose;

   public String getName()
   {
      return name;
   }

   public void setName(String str)
   {
      name = str;
   }

   public int getDeckLevel() 
   {
      return deckLevel;
   }

   public void setDeckLevel(int level)
   {
      deckLevel = level;
   }

   public int getShipId()
   {
      return shipId;
   }

   public void setShipId(int sid)
   {
      shipId = sid;
   }
    
   public String getPurpose()
   {
      return purpose;
   }

   public void setPurpose(String purpose)
   {
      this.purpose = purpose;
   }

}
