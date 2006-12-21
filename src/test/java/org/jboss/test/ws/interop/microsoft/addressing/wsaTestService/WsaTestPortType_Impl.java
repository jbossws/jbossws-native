package org.jboss.test.ws.interop.microsoft.addressing.wsaTestService;

public class WsaTestPortType_Impl implements org.jboss.test.ws.interop.microsoft.addressing.wsaTestService.WsaTestPortType, java.rmi.Remote {
   public void notify(java.lang.String wsaNotifyMessagePart) throws
       java.rmi.RemoteException {
      System.out.println("notify:" + wsaNotifyMessagePart);
   }
   public java.lang.String echo(java.lang.String wsaEchoInPart) throws
       java.rmi.RemoteException {

      java.lang.String _retVal = wsaEchoInPart;
      System.out.println("echo:" + wsaEchoInPart);
      return _retVal;
   }
   public void echoOut(java.lang.String wsaEchoOutPart) throws
       java.rmi.RemoteException {
      System.out.println("echoOut:" + wsaEchoOutPart);
   }
}
