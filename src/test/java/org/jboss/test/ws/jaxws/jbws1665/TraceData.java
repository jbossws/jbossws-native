package org.jboss.test.ws.jaxws.jbws1665;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TraceData", propOrder = { "type", "source", "time",
      "coordinate", "mileage", "heading", "speed", "property" })
public class TraceData implements Serializable {
   @XmlElement(required=true)
   private short type;
   @XmlElement(required=true)
   private String source;
   @XmlElement(required=true)
   private Calendar time;
   private CoordinateData coordinate;
   private Integer mileage;
   private Short heading;
   private Short speed;
   private PropertyData[] property;
   
   public CoordinateData getCoordinate() {
      return coordinate;
   }

   public void setCoordinate(CoordinateData coordinate) {
      this.coordinate = coordinate;
   }

   public Short getHeading() {
      return heading;
   }

   public void setHeading(Short heading) {
      this.heading = heading;
   }

   public Integer getMileage() {
      return mileage;
   }

   public void setMileage(Integer mileage) {
      this.mileage = mileage;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public Short getSpeed() {
      return speed;
   }

   public void setSpeed(Short speed) {
      this.speed = speed;
   }

   public Calendar getTime() {
      return time;
   }

   public void setTime(Calendar time) {
      this.time = time;
   }

   public short getType() {
      return type;
   }

   public void setType(short type) {
      this.type = type;
   }
   
   public void setProperty(PropertyData[] property) {
      this.property = property;
   }

   public PropertyData[] getProperty() {
      return property;
   }
}
