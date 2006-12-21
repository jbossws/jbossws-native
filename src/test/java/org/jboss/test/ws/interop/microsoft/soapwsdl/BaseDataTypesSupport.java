package org.jboss.test.ws.interop.microsoft.soapwsdl;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.interop.microsoft.InteropConfigFactory;
import org.jboss.test.ws.interop.microsoft.ClientScenario;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 20-Feb-2006
 */
public abstract class BaseDataTypesSupport extends JBossWSTest {
   protected abstract BaseDataTypesSEI getTargetPort() throws Exception;

   public static String getTargetAddress(Stub stub) {
      return (String)stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);      
   }

   public void testBool() throws Exception {
      boolean ret = getTargetPort().retBool(true);
      assertTrue(ret);
   }

   public void testByte() throws Exception {
      Byte b = Byte.MAX_VALUE;
      short ret = getTargetPort().retByte(b.shortValue());
      assertEquals(b.shortValue(), ret);
   }

   public void testSByte() throws Exception {
      Short s = Short.MAX_VALUE;
      byte ret = getTargetPort().retSByte(s.byteValue());
      assertEquals(s.byteValue(), ret);
   }

   public void testByteArray() throws Exception {
      String s = "HelloWorld";
      byte[] ret = getTargetPort().retByteArray(s.getBytes());
      assertEquals(s.getBytes(), ret);
   }

   public void testChar() throws Exception {
      int i = Character.digit('s', Character.MAX_RADIX);
      int ret = getTargetPort().retChar(i);
      assertEquals(i, ret);
   }

   public void testDateTime() throws Exception {
      Calendar c = Calendar.getInstance();
      Calendar ret = getTargetPort().retDateTime(c);
      assertEquals(c.getTimeInMillis(), ret.getTimeInMillis());
   }

   public void testBigDecimal() throws Exception {
      BigDecimal b = BigDecimal.TEN;
      BigDecimal ret = getTargetPort().retDecimal(b);
      assertEquals(b.longValue(), ret.longValue());
   }

   public void testDouble() throws Exception {
      double d = 12.00;
      double ret = getTargetPort().retDouble(d);
      assertEquals(d, ret);
   }

   public void testFloat() throws Exception {
      float f = 12.000f;
      float ret = getTargetPort().retFloat(f);
      assertEquals(f, ret);
   }

   public void testGUID() throws Exception {
      System.out.println("FIXME testGUID");
   }

   public void testInt() throws Exception {
      int i = 99;
      int ret = getTargetPort().retInt(i);
      assertEquals(i, ret);
   }

   public void testLong() throws Exception {
      long l = System.currentTimeMillis();
      long ret = getTargetPort().retLong(l);
      assertEquals(l, ret);
   }

   public void testObject() throws Exception{
      /*
      SOAPFactory factory = SOAPFactory.newInstance();
      SOAPElement el = factory.createElement("inObject", "ns1", "http://jboss.com/interop");
      el.setValue("objectTest");
      SOAPElement ret = getTargetPort().retObject(el);
      assertEquals(el, ret);
      */

      System.out.println("FIXME testObject");
   }

   public void testQName() throws Exception {
      QName qname = new QName("http://jboss.com", "testQName");
      QName ret = getTargetPort().retQName(qname);
      assertEquals(qname, ret);
   }

   public void testShort() throws Exception {
      Short s = Short.MAX_VALUE;
      short ret = getTargetPort().retShort(s.shortValue());
      assertEquals(s.shortValue(), ret);
   }

   public void testSingle() throws Exception {
      System.out.println("FIXME testSingle");
   }

   public void testString() throws Exception {
      String s = "HelloWorld";
      String ret = getTargetPort().retString(s);
      assertEquals(s, ret);
   }

   public void testTimeSpan() throws Exception {
      System.out.println("FIXME testTimeSpan");
   }

   public void testUInt() throws Exception {
      System.out.println("FIXME testUInt");
   }

   public void testULong() throws Exception {
      System.out.println("FIXME testULong");
   }

   public void testUShort() throws Exception {
      System.out.println("FIXME testUShort");
   }

   public void testURI() throws Exception {
      URI u = new URI("http://jboss.com/interop");
      URI ret = getTargetPort().retUri(u);
      assertEquals(u.toString(), ret.toString());
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
