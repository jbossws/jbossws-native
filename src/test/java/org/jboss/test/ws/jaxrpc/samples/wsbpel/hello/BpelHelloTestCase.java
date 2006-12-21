/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the JBPM BPEL PUBLIC LICENSE AGREEMENT as
 * published by JBoss Inc.; either version 1.0 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.jboss.test.ws.jaxrpc.samples.wsbpel.hello;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;

import org.jboss.test.ws.JBossWSTest;

/**
 * Test business process behavior based on web services.
 * 
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class BpelHelloTestCase extends JBossWSTest
{
   private HelloWorldService service;

   /*
    public static Test suite()
    {
    return JBossWSTestSetup.newTestSetup(BpelHelloTestCase.class, "jaxrpc-samples-wsbpel-hello.war, jaxrpc-samples-wsbpel-hello-client.jar");
    }

    protected void setUp() throws Exception
    {
    if (service == null)
    {
    if (isTargetServerJBoss())
    {
    InitialContext iniCtx = getInitialContext();
    service = (HelloWorldService)iniCtx.lookup("java:comp/env/service/BpelHello");
    }
    else
    {
    throw new IllegalStateException("Unsupported target server");
    }
    }
    }
    */

   public void testSayHelloProxy() throws Exception
   {
      System.out.println("FIXME: [JBWS-868] BPEL in jbossws-samples");
      if (true) return;

      Greeter proxy = service.getGreeterPort();

      String greeting = proxy.sayHello("Popeye");
      assertEquals("Hello, Popeye!", greeting);
   }

   public void testSayHelloDII() throws Exception
   {
      System.out.println("FIXME: [JBWS-868] BPEL in jbossws-samples");
      if (true) return;
      
      String portTypeNS = "http://jbpm.org/examples/hello";
      Call call = service.createCall(new QName(portTypeNS, "GreeterPort"));
      call.setOperationName(new QName(portTypeNS, "sayHello"));

      String greeting = (String)call.invoke(new Object[] { "Olive" });
      assertEquals("Hello, Olive!", greeting);
   }
}