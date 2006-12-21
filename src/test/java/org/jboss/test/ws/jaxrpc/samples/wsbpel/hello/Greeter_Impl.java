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

import java.rmi.RemoteException;

/*
 * Method bodies in this class were intentionally left empty.
 * The BPEL process {http://jbpm.org/examples/hello}HelloWorld 
 * specifies the behavior instead.
 */

/**
 * Service implementation bean of the Greeter endpoint.
 *  
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class Greeter_Impl implements Greeter
{
   public String sayHello(String name) throws RemoteException
   {
      return null;
   }
}