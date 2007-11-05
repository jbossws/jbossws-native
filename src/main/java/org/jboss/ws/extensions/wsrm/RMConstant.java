package org.jboss.ws.extensions.wsrm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;

public final class RMConstant
{
   private static final String PREFIX = RMConstant.class.getName();
   public static final String TARGET_ADDRESS = PREFIX + ".targetAddress";
   public static final String ONE_WAY_OPERATION = PREFIX + ".oneWayOperation";
   public static final String INVOCATION_CONTEXT = PREFIX + ".invocationContext";
   public static final String MARSHALLER = PREFIX + ".marshaller";
   public static final String UNMARSHALLER = PREFIX + ".unmarshaller";
   public static final String SERIALIZATION_CONTEXT = PREFIX + ".serializationContext";
   public static final String REMOTING_INVOCATION_CONTEXT = PREFIX + ".remotingInvocationContext";
   public static final String REMOTING_CONFIGURATION_CONTEXT = PREFIX + ".remotingConfigurationContext";
   public static final String OPERATION_QNAME = PREFIX + ".operationType";
   public static final String REQUEST_CONTEXT = PREFIX + ".requestContext";
   public static final String RESPONSE_CONTEXT = PREFIX + ".responseContext";
   public static final String SEQUENCE_REFERENCE = PREFIX + ".sequenceReference";
   public static final String DATA = PREFIX + ".data";
   // WS-Addressing related actions
   public static final String CREATE_SEQUENCE_WSA_ACTION;
   public static final String TERMINATE_SEQUENCE_WSA_ACTION;
   
   public static final List<QName> PROTOCOL_OPERATION_QNAMES;
   
   static
   {
      LinkedList<QName> temp = new LinkedList<QName>();
      Constants constants = Provider.get().getConstants();
      temp.add(constants.getCreateSequenceQName());
      temp.add(constants.getCreateSequenceResponseQName());
      temp.add(constants.getSequenceQName());
      temp.add(constants.getAckRequestedQName());
      temp.add(constants.getCloseSequenceQName());
      temp.add(constants.getSequenceAcknowledgementQName());
      temp.add(constants.getCloseSequenceResponseQName());
      temp.add(constants.getTerminateSequenceQName());
      temp.add(constants.getTerminateSequenceResponseQName());
      PROTOCOL_OPERATION_QNAMES = Collections.unmodifiableList(temp);
      CREATE_SEQUENCE_WSA_ACTION = Provider.get().getConstants().getNamespaceURI() + "/CreateSequence";
      TERMINATE_SEQUENCE_WSA_ACTION = Provider.get().getConstants().getNamespaceURI() + "/TerminateSequence";
   }

   private RMConstant()
   {
      // no instances
   }
}
