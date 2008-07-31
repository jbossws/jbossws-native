/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.tools.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * A JAX-RPC 1.1 Enum Type using Doubles.
 *
 * NOTE: This is just a test, in general, it's a really bad idea to use doubles
 * for an enum index
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class PlanetMass
{
   /*
    MERCURY (3.303e+23, 2.4397e6),
    VENUS   (4.869e+24, 6.0518e6),
    EARTH   (5.976e+24, 6.37814e6),
    MARS    (6.421e+23, 3.3972e6),
    JUPITER (1.9e+27,   7.1492e7),
    SATURN  (5.688e+26, 6.0268e7),
    URANUS  (8.686e+25, 2.5559e7),
    NEPTUNE (1.024e+26, 2.4746e7),
    PLUTO   (1.27e+22,  1.137e6);
    */

   private Double value;

   protected PlanetMass(Double value)
   {
      this.value = value;
   }

   private static Map map = new HashMap();

   public static final PlanetMass MERCURY = new PlanetMass(new Double(3.303e+23));
   public static final PlanetMass VENUS = new PlanetMass(new Double(4.869e+24));
   public static final PlanetMass EARTH = new PlanetMass(new Double(5.976e+24));
   public static final PlanetMass MARS = new PlanetMass(new Double(6.421e+23));
   public static final PlanetMass JUPITER = new PlanetMass(new Double(1.9e+27));
   public static final PlanetMass SATURN = new PlanetMass(new Double(5.688e+26));
   public static final PlanetMass URANUS = new PlanetMass(new Double(8.686e+25));
   public static final PlanetMass NEPTUNE = new PlanetMass(new Double(1.024e+26));
   public static final PlanetMass PLUTO = new PlanetMass(new Double(1.27e+22));

   static
   {
      map.put(MERCURY.getValue(), MERCURY);
      map.put(VENUS.getValue(), VENUS);
      map.put(EARTH.getValue(), EARTH);
      map.put(MARS.getValue(), MARS);
      map.put(JUPITER.getValue(), JUPITER);
      map.put(SATURN.getValue(), SATURN);
      map.put(URANUS.getValue(), URANUS);
      map.put(NEPTUNE.getValue(), NEPTUNE);
      map.put(PLUTO.getValue(), PLUTO);
   }

   public Double getValue()
   {
      return value;
   }

   public static PlanetMass fromValue(Double value)
   {
      PlanetMass ret = (PlanetMass) map.get(value);
      if (ret == null)
         throw new IllegalArgumentException("Unknown planet");

      return ret;
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof PlanetMass))
         return false;

      return ((PlanetMass) obj).value.equals(value);
   }

   public int hashCode()
   {
      return value.hashCode();
   }
}