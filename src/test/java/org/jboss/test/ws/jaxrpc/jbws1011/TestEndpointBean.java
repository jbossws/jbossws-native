package org.jboss.test.ws.jaxrpc.jbws1011;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class TestEndpointBean implements SessionBean
{

   /**  */
   private static final long serialVersionUID = 4273529627421651843L;

   private SessionContext context;

   public void ejbCreate()
   {
   }

   public String echoString(final String message)
   {
      return message;
   }

   public void setSessionContext(final SessionContext context)
   {
      this.context = context;
   }

   public void ejbRemove()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

}
