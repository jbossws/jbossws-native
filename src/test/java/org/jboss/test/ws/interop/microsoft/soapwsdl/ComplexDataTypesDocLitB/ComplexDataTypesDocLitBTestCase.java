package org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesDocLitB;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesProxy;
import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesSEI;
import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesSupport;
import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesDocLitB;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Feb-2006
 */
public class ComplexDataTypesDocLitBTestCase extends ComplexDataTypesSupport {

   IComplexDataTypesDocLitB targetPort;
   ComplexDataTypesSEI proxy;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(ComplexDataTypesDocLitBTestCase.class, "jbossws-interop-ComplexDataTypesDocLitB-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (targetPort == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/interop/ComplexDataTypesDocLitBService");
         this.targetPort = (IComplexDataTypesDocLitB)service.getPort(IComplexDataTypesDocLitB.class);
         this.proxy = (ComplexDataTypesSEI) ComplexDataTypesProxy.newInstance(targetPort);
         configureClient((Stub)targetPort);
      }
   }

   protected ComplexDataTypesSEI getTargetPort() throws Exception {
      return this.proxy;
   }

}

