package org.jboss.test.ws.interop.microsoft.soapwsdl;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.*;
import org.jboss.test.ws.interop.microsoft.InteropConfigFactory;
import org.jboss.test.ws.interop.microsoft.ClientScenario;

import javax.xml.rpc.Stub;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 23-Feb-2006
 */
public abstract class ComplexDataTypesSupport extends JBossWSTest {
   protected abstract ComplexDataTypesSEI getTargetPort() throws Exception;

   protected static final Person PERSON_1 = new Person(1.00, 99.1f, Boolean.FALSE, "Frida Braun");
   protected static final Person PERSON_2 = new Person(32.00, 99.2f, Boolean.TRUE, "Heiko Braun");

   public void testArray1D_SN() throws Exception {
      ArrayOfPerson ap = new ArrayOfPerson(
            new Person[] {
                  new Person(1.00, 99.1f, Boolean.FALSE, "Frida Braun"),
                  new Person(32.00, 99.2f, Boolean.TRUE, "Heiko Braun")
            }
      );
      ArrayOfPerson ret = getTargetPort().retArray1D_SN(ap);
      assertEquals(ap.getPerson().length , ret.getPerson().length);
   }

   public void testArrayAnyType1D() throws Exception {

      /*SOAPFactory factory = SOAPFactory.newInstance();
      SOAPElement el = factory.createElement("int", "ns1", "http://www.w3.org/2001/XMLSchema");
      el.setValue("1");

      ArrayOfanyType a = new ArrayOfanyType(new SOAPElement[] {el});
      ArrayOfanyType ret = getTargetPort().retArrayAnyType1D(a);
      assertEquals(a.getAnyType().length, ret.getAnyType().length);
      */

      System.out.println("FIXME testArrayAnyType1D");
   }

   public void testArrayDateTime1D() throws Exception {
      Calendar c = Calendar.getInstance();
      ArrayOfNullableOfdateTime a = new ArrayOfNullableOfdateTime(new Calendar[] {c});
      ArrayOfNullableOfdateTime ret = getTargetPort().retArrayDateTime1D(a);

      assertEquals(a.getDateTime().length, ret.getDateTime().length);
      assertEquals(c.getTimeInMillis(), ret.getDateTime()[0].getTimeInMillis());
   }

   public void testArrayDecimal1D() throws Exception {
      BigDecimal d = BigDecimal.TEN;
      ArrayOfNullableOfdecimal a = new ArrayOfNullableOfdecimal(
            new BigDecimal[] {d}
      );

      ArrayOfNullableOfdecimal ret = getTargetPort().retArrayDecimal1D(a);
      assertEquals(a.getDecimal().length, ret.getDecimal().length);
      assertEquals(a.getDecimal()[0].longValue(), ret.getDecimal()[0].longValue());
   }

   public void testArrayInt1D() throws Exception {
      ArrayOfint a = new ArrayOfint(new int[]{1,2,3,4,5,6});
      ArrayOfint ret = getTargetPort().retArrayInt1D(a);

      assertEquals(a.get_int().length, ret.get_int().length);
      assertEquals(a.get_int()[0], ret.get_int()[0]);
   }

   public void testArrayString1D() throws Exception {
      ArrayOfstring a = new ArrayOfstring(new String[] {"Hello World"});
      ArrayOfstring ret = getTargetPort().retArrayString1D(a);

      assertEquals(a.getString().length, ret.getString().length);
      assertEquals(a.getString()[0], ret.getString()[0]);
   }

   public void testArrayString2D() throws Exception {
      ArrayOfArrayOfstring a = new ArrayOfArrayOfstring(
            new ArrayOfstring[] { new ArrayOfstring(new String[] {"Hello World"}) }
      );
      ArrayOfArrayOfstring ret = getTargetPort().retArrayString2D(a);

      assertEquals(a.getArrayOfstring().length, ret.getArrayOfstring().length);
      assertEquals(a.getArrayOfstring()[0].getString()[0], ret.getArrayOfstring()[0].getString()[0]);
   }

   public void testDerivedClass() throws Exception {
      Table t = new Table("blue", 12.99f, Integer.MAX_VALUE);
      Table ret = getTargetPort().retDerivedClass(t);

      assertEquals(t.getColor(), ret.getColor());
      assertEquals(t.getPrice(), ret.getPrice());
      assertEquals(t.getSeatingCapacity(), ret.getSeatingCapacity());
   }

   public void testEnumInt() throws Exception {
      IntSet i = IntSet.value1;
      IntSet ret = getTargetPort().retEnumInt(i);

      assertEquals(i, ret);
   }

   public void testEnumString() throws Exception {
      BitMask b = BitMask.BitOne;
      BitMask ret = getTargetPort().retEnumString(b);

      assertEquals(b, ret);
   }

   public void testStructS1() throws Exception {
      Name n = new Name("Heiko Braun");
      Name ret = getTargetPort().retStructS1(n);

      assertEquals(n.getName(), ret.getName());
   }

   public void testStructSN() throws Exception {
      Person ret = getTargetPort().retStructSN(PERSON_1);

      assertEquals(PERSON_1.getName(), ret.getName());
      assertEquals(PERSON_1.getMale(), ret.getMale());
      assertEquals(PERSON_1.getAge(), ret.getAge());
      assertEquals(PERSON_1.getID(), ret.getID());
   }

   public void testStructSNSA() throws Exception {
      Employee e = new Employee(
            PERSON_2, Calendar.getInstance(),
            999l, // TODO: for some reason this is translated to unsignedInt, test with System.currentTimeMillies()
            new ArrayOfshort(new short[] {Short.MIN_VALUE, Short.MAX_VALUE})
      );

      Employee ret = getTargetPort().retStructSNSA(e);
      assertEquals(e.getBaseDetails().getName(), ret.getBaseDetails().getName());
   }

   public void testStructSNSAS() throws Exception {
      Group g = new Group(
            new ArrayOfPerson(new Person[] {PERSON_1, PERSON_2}),
            "Family"
      );

      Group ret = getTargetPort().retStructSNSAS(g);

      assertEquals(g.getName(), ret.getName());
      assertEquals(g.getMembers().getPerson().length, ret.getMembers().getPerson().length);
   }

    protected void configureClient(Stub port) {
      InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         //System.out.println("Using scenario: " + scenario);
         port._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
   }
}
