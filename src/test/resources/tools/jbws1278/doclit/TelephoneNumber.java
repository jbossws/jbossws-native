/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Thu Mar 27 15:40:15 GMT 2008
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */

package org.jboss.test.ws.jbws1278;


public class  TelephoneNumber implements java.io.Serializable
{

protected java.lang.String areaCode;

protected java.lang.String number;
public TelephoneNumber(){}

public TelephoneNumber(java.lang.String areaCode, java.lang.String number){
this.areaCode=areaCode;
this.number=number;
}
public java.lang.String getAreaCode() { return areaCode ;}

public void setAreaCode(java.lang.String areaCode){ this.areaCode=areaCode; }

public java.lang.String getNumber() { return number ;}

public void setNumber(java.lang.String number){ this.number=number; }

}
