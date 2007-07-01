package org.jboss.test.ws.jaxws.jbws1665;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TracePollData", propOrder = { "traces", "mark", "more" })
public class TracePollData implements Serializable {
   @XmlElement(required=true,nillable=true)
   protected TraceData[] traces;
   @XmlElement(required = true, nillable = true)
   protected String mark;
   protected boolean more;

   public String getMark() {
      return mark;
   }

   public void setMark(String mark) {
      this.mark = mark;
   }

   public boolean isMore() {
      return more;
   }

   public void setMore(boolean more) {
      this.more = more;
   }
   
   public void setTraces(TraceData[] traces) {
      this.traces = traces;
   }

   public TraceData[] getTraces() {
      return traces;
   }
}
