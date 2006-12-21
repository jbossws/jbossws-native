package org.jboss.test.ws.interop.microsoft.addressing.wsa10;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.ClientScenario;
import org.jboss.test.ws.interop.microsoft.InteropConfigFactory;
import org.jboss.ws.utils.DOMUtils;
import org.jboss.ws.addressing.AddressingClientUtil;
import org.jboss.ws.jaxrpc.StubExt;
import org.w3c.dom.Element;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.addressing.*;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;

/**
 * A test client for the W3C addressing test suite.
 * See <code>http://www.w3.org/2002/ws/addr/testsuite/</code>.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 02-Mar-2006
 */
public class AddressingTestCase extends JBossWSTest {

   private Echo echoPort;
   private Notify notifyPort;

   final static String WSA_FROM = "http://example.org/node/A";
   final static String WSA_TO = "http://example.org/node/B";

   private static AddressingBuilder BUILDER;
   private static AddressingConstants CONSTANTS;

   static
   {
      BUILDER = AddressingBuilder.getAddressingBuilder();
      CONSTANTS = BUILDER.newAddressingConstants();
   }

   private Element customerParam;
   private Element faultParam;
   private Element extraStuff;
   private Element wsdl1Param;
   private Element wsdl2Param;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AddressingTestCase.class, "jbossws-interop-wsa10-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (echoPort == null || notifyPort==null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/interop/WSAddressingService");
         echoPort = (Echo)service.getPort(Echo.class);
         notifyPort= (Notify)service.getPort(Notify.class);
         configureClient();
      }

      customerParam = DOMUtils.parse("<customer:CustomerKey xmlns:customer=\"http://example.org/customer\">Key#123456789</customer:CustomerKey>");
      faultParam = DOMUtils.parse("<customer:CustomerKey xmlns:customer=\"http://example.org/customer\">Fault#123456789</customer:CustomerKey>");
      wsdl1Param = DOMUtils.parse("<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\">insert WSDL 1.1 here!</definitions>");
      wsdl2Param = DOMUtils.parse("<description xmlns=\"http://www.w3.org/2006/01/wsdl\">insert WSDL 2.0 here!</description>");
      extraStuff = DOMUtils.parse("<customer:extraStuff xmlns:customer=\"http://example.org/customer\">This should be ignored</customer:extraStuff>");
   }

   private void configureClient() {

      InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         log.info("Using scenario: " + scenario);
         setTargetAddress((Stub)notifyPort, scenario.getTargetEndpoint().toString());
         setTargetAddress((Stub)echoPort, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
   }

   /**
    * Certain MSFT testcases require the connection to be closed
    * since remoting cannot work with particluar HTTP response codes.
    */
   private void forceReset() {
      /*try
      {
         echoPort = null;
         notifyPort = null;
         setUp();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to reset connection");
      } */
   }

   public void test1100() {
      try
      {
         // create addressing properties
         AddressingProperties requestProps =
               AddressingClientUtil.createDefaultProps("http://example.org/action/notify", WSA_TO);
         setRequestProperties((StubExt)notifyPort, requestProps);

         // invoke service
         notifyPort.notify("Message 1100");
         forceReset();

      }
      catch (RemoteException e)
      {
         fail(e.getMessage());
      }
   }

   public void test1101() {
      try
      {
         // create addressing properties
         AddressingProperties requestProps =
               AddressingClientUtil.createDefaultProps("http://example.org/action/notify", WSA_TO);
         requestProps.setMessageID( BUILDER.newURI( AddressingClientUtil.generateMessageID()));

         setRequestProperties((StubExt)notifyPort, requestProps);

         // invoke service
         notifyPort.notify("Message 1101");
         forceReset();
      }
      catch (Exception e)
      {
         fail(e.getMessage());
      }
   }

   public void test1102() {
      try
      {
         // create addressing properties
         AddressingProperties requestProps =
               AddressingClientUtil.createOneWayProps("http://example.org/action/notify", WSA_TO);

         setRequestProperties((StubExt)notifyPort, requestProps);

         // invoke service
         notifyPort.notify("Message 1102");
         forceReset();
      }
      catch (Exception e)
      {
         fail(e.getMessage());
      }
   }

   public void test1103() throws Exception {
      // create addressing properties
      AddressingProperties requestProps =
            AddressingClientUtil.createDefaultProps(
                  "http://example.org/action/notify", WSA_TO);

      requestProps.setFaultTo(BUILDER.newEndpointReference(new URI(CONSTANTS.getNoneURI())));
      setRequestProperties((StubExt)notifyPort, requestProps);

      notifyPort.notify("Message 1103");
      forceReset();
   }

   public void test1104() throws Exception {
      // create addressing properties
      AddressingProperties requestProps =
            AddressingClientUtil.createOneWayProps(
                  "http://example.org/action/notify",
                  WSA_TO
            );

      requestProps.setFaultTo(BUILDER.newEndpointReference(new URI(CONSTANTS.getNoneURI())));
      setRequestProperties((StubExt)notifyPort, requestProps);
      notifyPort.notify("Message 1104");
      forceReset();
   }

   public void test1106() throws Exception {
      // create addressing properties
      AddressingProperties requestProps =
            AddressingClientUtil.createOneWayProps(
                  "http://example.org/action/notify",
                  WSA_TO
            );

      requestProps.getReplyTo().getReferenceParameters().addElement(customerParam);
      setRequestProperties((StubExt)notifyPort, requestProps);

      notifyPort.notify("Message 1106");
      forceReset();
   }


   public void test1107() throws Exception {
      // create addressing properties
      AddressingProperties requestProps =
            AddressingClientUtil.createOneWayProps("http://example.org/action/notify", WSA_TO);

      requestProps.getReplyTo().getMetadata().addElement(wsdl1Param);
      requestProps.getReplyTo().getMetadata().addElement(wsdl2Param);

      setRequestProperties((StubExt)notifyPort, requestProps);

      notifyPort.notify("Message 1107");
      forceReset();
   }

   //
   //  One-way message containing a ReplyTo address
   //  with an element extension and an attribute extension
   //  of the ReferenceParameters and Metadata elements.
   // /
   public void test1108() throws Exception {

      AddressingProperties requestProps =
            AddressingClientUtil.createOneWayProps("http://example.org/action/notify", WSA_TO);

      requestProps.getReplyTo().getReferenceParameters().addElement(customerParam);
      requestProps.getReplyTo().addAttribute(new QName("http://example.org/customer","level"), "premium");

      requestProps.getReplyTo().getMetadata().addElement(extraStuff);
      requestProps.getReplyTo().getMetadata().addAttribute(new QName("http://example.org/customer", "total"), "1");

      setRequestProperties((StubExt)notifyPort, requestProps);

      notifyPort.notify("Message 1108");
      forceReset();
   }

   //
   //  Two-way message exchange containing an Action.
   //  All other fields are defaulted.
   //  The presence of a MessageID in the first message and
   //  of the corresponding RelatesTo in the second message is tested.
   //
   public void test1130() throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createDefaultProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      setRequestProperties((StubExt)echoPort, requestProps);

      // invoke service
      echoPort.echo("Message 1130");

      SOAPAddressingProperties responseProperties = (SOAPAddressingProperties)
            getResponseProperties((StubExt)echoPort);

      forceReset();

      Relationship rel = responseProperties.getRelatesTo()[0];
      assertEquals(rel.getID().toString(), requestProps.getMessageID().getURI().toString());

   }

   //
   //  Two-way message exchange containing
   //  an Action, MessageID and a ReplyTo of anonymous.
   //  All other fields are defaulted.
   //
   public void test1131 () throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createAnonymousProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      setRequestProperties((StubExt)echoPort, requestProps);

      // invoke service
      echoPort.echo("Message 1131");

      SOAPAddressingProperties responseProperties = (SOAPAddressingProperties)
            getResponseProperties((StubExt)echoPort);
      forceReset();

      Relationship rel = responseProperties.getRelatesTo()[0];
      assertEquals(rel.getID().toString(), requestProps.getMessageID().getURI().toString());
      assertTrue(null == responseProperties.getReplyTo());

   }

   //
   //  Two-way message exchange containing an Action and a ReplyTo
   //  with the address set to anonymous.
   //  The ReplyTo contains at least one Reference Parameter value.
   //  The reply message is returned on the HTTP response with
   //  the Reference Parameter value as a first class SOAP header.
   //
   public void test1132() throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createAnonymousProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      requestProps.getReplyTo().getReferenceParameters().addElement(customerParam);

      setRequestProperties((StubExt)echoPort, requestProps);

      // invoke service
      echoPort.echo("Message 1132");

      SOAPAddressingProperties responseProperties = (SOAPAddressingProperties)
            getResponseProperties((StubExt)echoPort);

      forceReset();

      Relationship rel = responseProperties.getRelatesTo()[0];
      assertEquals(rel.getID().toString(), requestProps.getMessageID().getURI().toString());
      assertTrue(null == responseProperties.getReplyTo());
      List<Object> returnParameters = responseProperties.getReferenceParameters().getElements();
      assertFalse("Reference parameter is missing", returnParameters.isEmpty());

   }

   //
   //  Two-way message exchange containing an Action.
   //  The ReplyTo and FaultTo addresses are both anonymous.
   //  The ReplyTo and FaultTo contain at least one Reference Parameter value
   //  which are different.
   //  A fault message is returned on the HTTP response with the
   //  FaultTo Reference Parameter value as a first class SOAP header.
   //
   public void test1133() throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createAnonymousProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      requestProps.setFaultTo(BUILDER.newEndpointReference(new URI(CONSTANTS.getAnonymousURI())));

      requestProps.getReplyTo().getReferenceParameters().addElement(customerParam);
      requestProps.getFaultTo().getReferenceParameters().addElement(faultParam);

      setRequestProperties((StubExt)echoPort, requestProps);

      // invoke service
      try
      {
         echoPort.echo("Message 1133");
      }
      catch (RemoteException e)
      {
         boolean isSoapFault = (e.getCause() instanceof SOAPFaultException);
         if(!isSoapFault) throw e;
      }

      SOAPAddressingProperties responseProperties = (SOAPAddressingProperties)
            getResponseProperties((StubExt)echoPort);

      forceReset();

      Relationship rel = responseProperties.getRelatesTo()[0];
      assertEquals(rel.getID().toString(), requestProps.getMessageID().getURI().toString());
      assertTrue(null == responseProperties.getReplyTo());
      List<Object> returnParameters = responseProperties.getReferenceParameters().getElements();
      assertFalse("Reference parameter is missing", returnParameters.isEmpty());
   }

   //
   //  Two-way message exchange containing an Action and a ReplyTo address,
   //  but no FaultTo EPR. The ReplyTo address is anonymous.
   //  The ReplyTo contains at least one Reference Parameter value.
   //  A fault message is returned on the HTTP response with the
   //  ReplyTo Reference Parameter value as a first class SOAP header.
   //
   public void test1134() throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createAnonymousProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());

      requestProps.getReplyTo().getReferenceParameters().addElement(customerParam);

      setRequestProperties((StubExt)echoPort, requestProps);

      // invoke service
      try
      {
         echoPort.echo("Message 1134");
      }
      catch (RemoteException e)
      {
         boolean isSoapFault = (e.getCause() instanceof SOAPFaultException);
         if(!isSoapFault) throw e;
      }

      SOAPAddressingProperties responseProperties = (SOAPAddressingProperties)
            getResponseProperties((StubExt)echoPort);

      forceReset();

      Relationship rel = responseProperties.getRelatesTo()[0];
      assertEquals(rel.getID().toString(), requestProps.getMessageID().getURI().toString());
      assertTrue(null == responseProperties.getReplyTo());
      List<Object> returnParameters = responseProperties.getReferenceParameters().getElements();
      assertFalse("Reference parameter is missing", returnParameters.isEmpty());
   }

   //
   //  Two-way message exchange containing a duplicate Reply-To header.
   //
   public void test1140() throws Exception {
      System.out.println("1140: Not supported on the client side");
   }

   //
   //  Two-way message exchange containing a duplicate To header.
   //
   public void test1141() throws Exception {
      System.out.println("1141: Not supported on the client side");
   }

   //
   //  Two-way message exchange containing a duplicate Fault-To header.
   //
   public void test1142() throws Exception {
      System.out.println("1142: Not supported on the client side");
   }

   //
   //  Two-way message exchange containing a duplicate action header.
   //
   public void test1143() throws Exception {
      System.out.println("1143: Not supported on the client side");
   }

   //
   //  wo-way message exchange containing a duplicate message ID header.
   //
   public void test1144() throws Exception {
      System.out.println("1144: Not supported on the client side");
   }

   //
   //  Two-way message exchange containing an
   //  Action and a ReplyTo identifying an endpoint.
   //  All other fields are defaulted.
   //
   public void test1150() throws Exception {
      AddressingProperties requestProps =
            AddressingClientUtil.createDefaultProps("http://example.org/action/echoIn", WSA_TO);
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      requestProps.setReplyTo(
            BUILDER.newEndpointReference(
                  new URI("http://localhost:8080/wsa10/wsaTestService")
            )
      );

      setRequestProperties((StubExt)echoPort, requestProps);

      echoPort.echo("Messsage 1150");

      forceReset();

      // todo: check echOut results
   }

   //
   //  customize a stubs endpoint url
   //
   private static void setTargetAddress(Stub stub, String url) {
      stub._setProperty(StubExt.ENDPOINT_ADDRESS_PROPERTY, url);
   }

   private void setRequestProperties(Stub stub, AddressingProperties props) {
      stub._setProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
   }

   private AddressingProperties getResponseProperties(Stub stub) {
      return (AddressingProperties)stub._getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND);
   }
}
