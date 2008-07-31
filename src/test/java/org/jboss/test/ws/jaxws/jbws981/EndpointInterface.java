/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Thu Nov 02 21:17:37 CET 2006
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jaxws.jbws981;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.wsf.spi.annotation.WebContext;

@WebService(targetNamespace = "http://www.jboss.org/test/ws/jaxws/jbws981")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface EndpointInterface
{
   String hello(String msg);
}
