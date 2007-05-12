package org.jboss.test.ws.interop.soapwsdl;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id:$
 * @since 29.01.2007
 */
public interface BaseDataTypesSEI {

   public boolean retBool(boolean inBool);

    public short retByte(short inByte);

    public byte[] retByteArray(byte[] inByteArray);

    public int retChar(int inChar);

    public XMLGregorianCalendar retDateTime(XMLGregorianCalendar inDateTime);

    public java.math.BigDecimal retDecimal(java.math.BigDecimal inDecimal);

    public double retDouble(double inDouble);

    public float retFloat(float inFloat);

    public java.lang.String retGuid(java.lang.String inGuid);

    public int retInt(int inInt);

    public long retLong(long inLong);

    public javax.xml.soap.SOAPElement retObject(javax.xml.soap.SOAPElement inObject);

    public javax.xml.namespace.QName retQName(javax.xml.namespace.QName inQName);

    public byte retSByte(byte inSByte);

    public short retShort(short inShort);

    public float retSingle(float inSingle);

    public java.lang.String retString(java.lang.String inString);

    public java.lang.String retTimeSpan(java.lang.String inTimeSpan);

    public long retUInt(long inUInt);

    public java.math.BigInteger retULong(java.math.BigInteger inULong);

    public int retUShort(int inUShort);

    public String retUri(String inUri);
         

}
