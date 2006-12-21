package org.jboss.test.ws.jaxrpc.xop.doclit;

import org.jboss.test.ws.jaxrpc.xop.doclit.XOPPing;
import org.jboss.test.ws.jaxrpc.xop.shared.*;

import java.rmi.RemoteException;

/**
 * MTOM test service impl.
 * The 'message' param value determines wether or not the response should be XOP encoded.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 11-Apr-2006
 */
public class XOPPingImpl extends MTOMServiceBase implements XOPPing {

   public PingMsgResponse ping(PingMsg pingMsg) throws RemoteException {
      toggleXOP(pingMsg.getMessage());
      return new PingMsgResponse(pingMsg.getXopContent());
   }

   public PingImageResponse pingImage(PingImage pingImage) throws RemoteException {
      toggleXOP(pingImage.getMessage());
      return new PingImageResponse(pingImage.getXopContent());
   }

   public PingSourceResponse pingSource(PingSource pingSource) throws RemoteException {
      toggleXOP(pingSource.getMessage());
      return new PingSourceResponse(pingSource.getXopContent());
   }

   public PingDataHandlerResponse pingDataHandler(PingDataHandler pingDataHandler) throws RemoteException {
      toggleXOP(pingDataHandler.getMessage());
      return new PingDataHandlerResponse(pingDataHandler.getXopContent());
   }
}
