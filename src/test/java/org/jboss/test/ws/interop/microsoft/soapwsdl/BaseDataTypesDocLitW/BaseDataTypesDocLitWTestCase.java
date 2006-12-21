package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesSEI;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesSupport;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesProxy;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Feb-2006
 */
public class BaseDataTypesDocLitWTestCase extends BaseDataTypesSupport {

   IBaseDataTypesDocLitW targetPort;
   BaseDataTypesSEI proxy;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(BaseDataTypesDocLitWTestCase.class, "jbossws-interop-BaseDataTypesDocLitW-client.jar");
   }

    protected void setUp() throws Exception
   {
      super.setUp();

      if (targetPort == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/interop/BaseDataTypesDocLitWService");
         this.targetPort = (IBaseDataTypesDocLitW)service.getPort(IBaseDataTypesDocLitW.class);
         this.proxy = (BaseDataTypesSEI)BaseDataTypesProxy.newInstance(targetPort);
         configureClient((Stub)targetPort);
      }
   }

   protected BaseDataTypesSEI getTargetPort() throws Exception {
      return this.proxy;
   }

   public void testBool() throws Exception {
      Boolean ret = targetPort.retBool(Boolean.TRUE);
      assertEquals(Boolean.TRUE, ret);
   }

   public void testShort() throws Exception {
      Short s = Short.MAX_VALUE;
      Short ret = targetPort.retShort(s);
      assertEquals(s, ret);
   }

   public void testSByte() throws Exception {
      Byte b = Byte.MAX_VALUE;
      Byte ret = targetPort.retSByte(b);
      assertEquals(b, ret);      
   }

   public void testChar() throws Exception {
      Integer i = Character.digit('s', Character.MAX_RADIX);
      Integer ret = targetPort.retChar(i);
      assertEquals(i, ret);
   }

   public void testDouble() throws Exception {
      Double d = Double.MAX_VALUE;
      Double ret = targetPort.retDouble(d);
      assertEquals(d, ret);
   }

   public void testFloat() throws Exception {
      Float f = Float.MAX_VALUE;
      float ret = targetPort.retFloat(f);
      assertEquals(f, ret);
   }

   public void testInt() throws Exception {
      Integer i = Integer.MAX_VALUE;
      Integer ret = targetPort.retInt(i);
      assertEquals(i, ret);
   }

   public void testLong() throws Exception {
      Long l = Long.MAX_VALUE;
      Long ret = targetPort.retLong(l);
      assertEquals(l, ret);
   }
}

