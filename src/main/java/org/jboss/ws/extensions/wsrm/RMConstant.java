package org.jboss.ws.extensions.wsrm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;

public final class RMConstant
{

   private RMConstant()
   {
      // instances not allowed
   }
   
   private static final String PREFIX = "wsrm";
   
   public static final String ONE_WAY_OPERATION = PREFIX + ".oneWayOperation";
   public static final String REQUEST_CONTEXT = PREFIX + ".requestContext";
   public static final String RESPONSE_CONTEXT = PREFIX + ".responseContext";
   public static final String SEQUENCE_REFERENCE = PREFIX + ".sequenceReference";
   public static final String PROTOCOL_MESSAGES = PREFIX + ".protocolMessages";
   public static final String PROTOCOL_MESSAGES_MAPPING = PREFIX + ".protocolMessagesMapping";
   public static final String WSA_MESSAGE_ID = PREFIX + ".wsaMessageId";
   public static final Set<QName> PROTOCOL_OPERATION_QNAMES;
   
   static
   {
      Set<QName> temp = new HashSet<QName>();
      RMConstants constants = RMProvider.get().getConstants();
      temp.add(constants.getSequenceQName());
      temp.add(constants.getSequenceFaultQName());
      temp.add(constants.getSequenceAcknowledgementQName());
      temp.add(constants.getAckRequestedQName());
      temp.add(constants.getCreateSequenceQName());
      temp.add(constants.getCreateSequenceResponseQName());
      temp.add(constants.getCloseSequenceQName());
      temp.add(constants.getCloseSequenceResponseQName());
      temp.add(constants.getTerminateSequenceQName());
      temp.add(constants.getTerminateSequenceResponseQName());
      PROTOCOL_OPERATION_QNAMES = Collections.unmodifiableSet(temp);
   }

}
