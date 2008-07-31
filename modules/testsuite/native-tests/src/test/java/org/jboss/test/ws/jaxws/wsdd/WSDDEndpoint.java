package org.jboss.test.ws.jaxws.wsdd;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Heiko.Braun@jboss.org
 * @since Mar 12, 2007
 */
@WebService
public interface WSDDEndpoint {
   @WebMethod
   ResponseMessage echo(Message message);

   @WebMethod
   Boolean checkMTOMEnabled();
}
