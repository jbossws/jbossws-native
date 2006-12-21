/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package javax.xml.rpc.soap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;

import org.jboss.logging.Logger;
import org.jboss.util.id.SerialVersion;

/** The SOAPFaultException exception represents a SOAP fault.
 * 
 * The message part in the SOAP fault maps to the contents of faultdetail
 * element accessible through the getDetail method on the SOAPFaultException.
 * The method createDetail on the javax.xml.soap.SOAPFactory creates an instance
 * of the javax.xml.soap.Detail.
 * 
 * The faultstring provides a human-readable description of the SOAP fault. The
 * faultcode element provides an algorithmic mapping of the SOAP fault.
 *  
 * Refer to SOAP 1.1 and WSDL 1.1 specifications for more details of the SOAP
 * faults. 
 * 
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public class SOAPFaultException extends RuntimeException
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPFaultException.class);

   /** @since 4.0.2 */
   static final long serialVersionUID;
   private static final int CODE_IDX = 0;
   private static final int STRING_IDX = 1;
   private static final int ACTOR_IDX = 2;
   private static final int DETAIL_IDX = 3;
   private static final ObjectStreamField[] serialPersistentFields;
   static
   {
      if (SerialVersion.version == SerialVersion.LEGACY)
      {
         serialVersionUID = -290987278985292477L;
         serialPersistentFields = new ObjectStreamField[] { new ObjectStreamField("faultCode", QName.class), new ObjectStreamField("faultString", String.class),
               new ObjectStreamField("faultActor", String.class), new ObjectStreamField("faultDetail", Detail.class), };
      }
      else
      {
         serialVersionUID = -7224636940495025621L;
         serialPersistentFields = new ObjectStreamField[] { new ObjectStreamField("faultcode", QName.class), new ObjectStreamField("faultstring", String.class),
               new ObjectStreamField("faultactor", String.class), new ObjectStreamField("faultdetail", Detail.class), };
      }
   }

   private QName faultCode;
   private String faultString;
   private String faultActor;
   private Detail faultDetail;

   public SOAPFaultException(QName faultCode, String faultString, String faultActor, Detail faultDetail)
   {
      super(faultString);

      Name detailName = faultDetail != null ? faultDetail.getElementName() : null;
      log.debug("new SOAPFaultException [code=" + faultCode + ",string=" + faultString + ",actor=" + faultActor + ",detail=" + detailName + "]");

      this.faultCode = faultCode;
      this.faultString = faultString;
      this.faultActor = faultActor;
      this.faultDetail = faultDetail;
   }

   public QName getFaultCode()
   {
      return faultCode;
   }

   public String getFaultString()
   {
      return faultString;
   }

   public String getFaultActor()
   {
      return faultActor;
   }

   public Detail getDetail()
   {
      return faultDetail;
   }

   // Private -------------------------------------------------------
   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
   {
      ObjectInputStream.GetField fields = ois.readFields();
      String name = serialPersistentFields[CODE_IDX].getName();
      this.faultCode = (QName)fields.get(name, null);
      name = serialPersistentFields[STRING_IDX].getName();
      this.faultString = (String)fields.get(name, null);
      name = serialPersistentFields[ACTOR_IDX].getName();
      this.faultActor = (String)fields.get(name, null);
      name = serialPersistentFields[DETAIL_IDX].getName();
      this.faultDetail = (Detail)fields.get(name, null);
   }

   private void writeObject(ObjectOutputStream oos) throws IOException
   {
      // Write j2ee 1.4.1 RI field names
      ObjectOutputStream.PutField fields = oos.putFields();
      String name = serialPersistentFields[CODE_IDX].getName();
      fields.put(name, faultCode);
      name = serialPersistentFields[STRING_IDX].getName();
      fields.put(name, faultString);
      name = serialPersistentFields[ACTOR_IDX].getName();
      fields.put(name, faultActor);
      name = serialPersistentFields[DETAIL_IDX].getName();
      fields.put(name, faultDetail);
      oos.writeFields();
   }
}
