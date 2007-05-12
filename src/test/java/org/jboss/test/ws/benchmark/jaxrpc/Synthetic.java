/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import java.io.Serializable;

/**
 * A Synthetic.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 * @version $Revision: 1757 $
 */
public class Synthetic implements Serializable
{
   private String s;
   private SimpleUserType sut;
   private byte[] barray;

   public Synthetic()
   {
   }

   
   /**
    * @param s
    * @param sut
    * @param b
    */
   public Synthetic(String s, SimpleUserType sut, byte[] b)
   {
      super();
      this.s = s;
      this.sut = sut;
      this.barray = b;
   }
   
   public byte[] getB()
   {
      return barray;
   }
   public void setB(byte[] b)
   {
      this.barray = b;
   }
   public String getS()
   {
      return s;
   }
   public void setS(String s)
   {
      this.s = s;
   }
   public SimpleUserType getSut()
   {
      return sut;
   }
   public void setSut(SimpleUserType sut)
   {
      this.sut = sut;
   }
}
