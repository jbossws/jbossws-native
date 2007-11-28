package org.jboss.ws.extensions.wsrm;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;

import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;

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
   public static final String REQUEST_CONTEXT = PREFIX + ".requestContext";
   public static final String RESPONSE_CONTEXT = PREFIX + ".responseContext";
   public static final String SEQUENCE_REFERENCE = PREFIX + ".sequenceReference";
   public static final String PROTOCOL_MESSAGES = PREFIX + ".protocolMessages";
   public static final String PROTOCOL_MESSAGES_MAPPING = PREFIX + ".protocolMessagesMapping";
   // WS-Addressing related actions
   public static final String CREATE_SEQUENCE_WSA_ACTION;
   public static final String CLOSE_SEQUENCE_WSA_ACTION;
   public static final String TERMINATE_SEQUENCE_WSA_ACTION;
   
   public static final Set<QName> PROTOCOL_OPERATION_QNAMES;
   
   public static final String WSA_ANONYMOUS_URI = AddressingBuilder.getAddressingBuilder().newAddressingConstants().getAnonymousURI();
   public static final String WSA_MESSAGE_ID = PREFIX + ".wsaMessageId";
   
   static
   {
      Set<QName> temp = new HashSet<QName>();
      RMConstants constants = RMProvider.get().getConstants();
      temp.add(constants.getSequenceQName());
      temp.add(constants.getSequenceFaultQName());
      temp.add(constants.getAcknowledgementRangeQName());
      temp.add(constants.getAckRequestedQName());
      temp.add(constants.getCreateSequenceQName());
      temp.add(constants.getCreateSequenceResponseQName());
      temp.add(constants.getCloseSequenceQName());
      temp.add(constants.getCloseSequenceResponseQName());
      temp.add(constants.getTerminateSequenceQName());
      temp.add(constants.getTerminateSequenceResponseQName());
      PROTOCOL_OPERATION_QNAMES = Collections.unmodifiableSet(temp);
      CREATE_SEQUENCE_WSA_ACTION = RMProvider.get().getConstants().getNamespaceURI() + "/CreateSequence";
      CLOSE_SEQUENCE_WSA_ACTION = RMProvider.get().getConstants().getNamespaceURI() + "/CloseSequence";
      TERMINATE_SEQUENCE_WSA_ACTION = RMProvider.get().getConstants().getNamespaceURI() + "/TerminateSequence";
   }

   private RMConstant()
   {
      // no instances
   }
}
