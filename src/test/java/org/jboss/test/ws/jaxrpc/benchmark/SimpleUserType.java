/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.jaxrpc.benchmark;

import java.io.Serializable;

/**
 * A SimpleUserType.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 * @version $Revision$
 */
public class SimpleUserType implements Serializable
{

   private int i;
   private float f;
   private String s;
   
   public SimpleUserType()
   {
   }

   /**
    * @param i
    * @param f
    * @param s
    */
   public SimpleUserType(int i, float f, String s)
   {
      super();
      this.i = i;
      this.f = f;
      this.s = s;
   }
   
   public float getF()
   {
      return f;
   }
   public void setF(float f)
   {
      this.f = f;
   }
   public int getI()
   {
      return i;
   }
   public void setI(int i)
   {
      this.i = i;
   }
   public String getS()
   {
      return s;
   }
   public void setS(String s)
   {
      this.s = s;
   }
}
