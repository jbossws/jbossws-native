package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit_Service;

import javax.xml.soap.SOAPElement;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 06-Mar-2006
 */
public class ServiceImpl implements IBaseDataTypesRpcLit {
   public boolean retBool(boolean inBool) throws RemoteException {
      return inBool;
   }

   public short retByte(short inByte) throws RemoteException {
      return inByte;
   }

   public byte retSByte(byte inSByte) throws RemoteException {
      return inSByte;
   }

   public byte[] retByteArray(byte[] inByteArray) throws RemoteException {
      return inByteArray;
   }

   public int retChar(int inChar) throws RemoteException {
      return inChar;
   }

   public BigDecimal retDecimal(BigDecimal inDecimal) throws RemoteException {
      return inDecimal;
   }

   public float retFloat(float inFloat) throws RemoteException {
      return inFloat;
   }

   public double retDouble(double inDouble) throws RemoteException {
      return inDouble;
   }

   public float retSingle(float inSingle) throws RemoteException {
      return inSingle;
   }

   public int retInt(int inInt) throws RemoteException {
      return inInt;
   }

   public short retShort(short inShort) throws RemoteException {
      return inShort;
   }

   public long retLong(long inLong) throws RemoteException {
      return inLong;
   }

   public SOAPElement retObject(SOAPElement inObject) throws RemoteException {
      return inObject;
   }

   public long retUInt(long inUInt) throws RemoteException {
      return inUInt;
   }

   public int retUShort(int inUShort) throws RemoteException {
      return inUShort;
   }

   public BigInteger retULong(BigInteger inULong) throws RemoteException {
      return inULong;
   }

   public String retString(String inString) throws RemoteException {
      return inString;
   }

   public String retGuid(String inGuid) throws RemoteException {
      return inGuid;
   }

   public URI retUri(URI inUri) throws RemoteException {
      return inUri;
   }

   public Calendar retDateTime(Calendar inDateTime) throws RemoteException {
      return inDateTime;
   }

   public String retTimeSpan(String inTimeSpan) throws RemoteException {
      return inTimeSpan;
   }

   public QName retQName(QName inQName) throws RemoteException {
      return inQName;  
   }
}
