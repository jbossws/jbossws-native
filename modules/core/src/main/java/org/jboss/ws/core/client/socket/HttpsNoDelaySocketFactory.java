package org.jboss.ws.core.client.socket;

import org.jboss.remoting.transport.http.ssl.HTTPSSocketFactory;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * {@link javax.net.SocketFactory} class that will make a call on {@link java.net.Socket#setTcpNoDelay(boolean)}
 * when createSocket() is called.
 */
public class HttpsNoDelaySocketFactory extends HTTPSSocketFactory {

   public HttpsNoDelaySocketFactory(SSLSocketFactory socketFactory, HandshakeCompletedListener listener) {
      super(socketFactory, listener);
   }

   public Socket createSocket(Socket socket, String string, int i, boolean b) throws IOException {
      Socket toReturn = super.createSocket(socket, string, i, b);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   public Socket createSocket(String string, int i) throws IOException {
      Socket toReturn = super.createSocket(string, i);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
      Socket toReturn = super.createSocket(inetAddress, i);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   public Socket createSocket(String string, int i, InetAddress inetAddress, int i1) throws IOException,
         UnknownHostException {
      Socket toReturn = super.createSocket(string, i, inetAddress, i1);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }


}
