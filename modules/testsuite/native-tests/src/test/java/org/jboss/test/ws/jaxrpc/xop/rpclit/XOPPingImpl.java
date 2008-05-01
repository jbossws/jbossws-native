package org.jboss.test.ws.jaxrpc.xop.rpclit;

import java.rmi.RemoteException;

import org.jboss.test.ws.jaxrpc.xop.shared.MTOMServiceBase;
import org.jboss.test.ws.jaxrpc.xop.shared.PingDataHandler;
import org.jboss.test.ws.jaxrpc.xop.shared.PingDataHandlerResponse;
import org.jboss.test.ws.jaxrpc.xop.shared.PingImage;
import org.jboss.test.ws.jaxrpc.xop.shared.PingImageResponse;
import org.jboss.test.ws.jaxrpc.xop.shared.PingMsg;
import org.jboss.test.ws.jaxrpc.xop.shared.PingMsgResponse;
import org.jboss.test.ws.jaxrpc.xop.shared.PingSource;
import org.jboss.test.ws.jaxrpc.xop.shared.PingSourceResponse;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 11-Apr-2006
 */
public class XOPPingImpl extends MTOMServiceBase implements XOPPing {

   public PingMsgResponse ping(PingMsg pingMsg) throws RemoteException {

      String message = pingMsg.getMessage();
      toggleXOP(message);

      return new PingMsgResponse(pingMsg.getXopContent());
   }

   public byte[] pingSimple(String parameters, byte[] xopContent) throws RemoteException {
      toggleXOP(parameters);
      return xopContent;
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
