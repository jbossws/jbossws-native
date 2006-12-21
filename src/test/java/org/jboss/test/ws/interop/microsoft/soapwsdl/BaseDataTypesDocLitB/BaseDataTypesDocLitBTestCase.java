package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitB;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesProxy;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesSEI;
import org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesSupport;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Feb-2006
 */
public class BaseDataTypesDocLitBTestCase extends BaseDataTypesSupport {

   IBaseDataTypesDocLitB targetPort;
   BaseDataTypesSEI proxy;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(BaseDataTypesDocLitBTestCase.class, "jbossws-interop-BaseDataTypesDocLitB-client.jar");
   }

    protected void setUp() throws Exception
   {
      super.setUp();

      if (targetPort == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/interop/BaseDataTypesDocLitBService");
         this.targetPort = (IBaseDataTypesDocLitB)service.getPort(IBaseDataTypesDocLitB.class);
         this.proxy = (BaseDataTypesSEI)BaseDataTypesProxy.newInstance(targetPort);
         configureClient((Stub)targetPort);
      }
   }

   protected BaseDataTypesSEI getTargetPort() throws Exception {
      return this.proxy;
   }
}
