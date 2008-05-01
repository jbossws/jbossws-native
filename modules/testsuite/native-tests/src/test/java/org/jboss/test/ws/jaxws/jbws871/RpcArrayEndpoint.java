/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Mon May 15 00:46:03 CDT 2006
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jaxws.jbws871;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@SOAPBinding(style = Style.RPC)
@WebService(name = "RpcArrayEndpoint")
public interface  RpcArrayEndpoint extends java.rmi.Remote
{
  public java.lang.Integer[]  intArr(java.lang.String string_1,java.lang.Integer[] integer_1) throws  java.rmi.RemoteException;
}
