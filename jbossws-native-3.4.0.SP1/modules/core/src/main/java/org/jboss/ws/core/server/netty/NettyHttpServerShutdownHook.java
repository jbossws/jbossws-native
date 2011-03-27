package org.jboss.ws.core.server.netty;

/**
 * Netty server shutdown hook.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyHttpServerShutdownHook implements Runnable
{

   /** Delegee. */
   private final NettyHttpServerImpl server;

   /**
    * Constructor.
    *
    * @param server netty http server
    */
   NettyHttpServerShutdownHook(final NettyHttpServerImpl server)
   {
      super();

      this.server = server;
   }

   /**
    * Terminates server.
    */
   public void run()
   {
      this.server.terminate();
   }

}
