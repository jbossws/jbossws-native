/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Sun May 06 14:14:38 CEST 2007
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */

package org.jboss.test.ws.jbws1627;


public class  Person
{

protected java.lang.String surname;

protected org.jboss.test.ws.jbws1627.NickName[]  nickNames;
public Person(){}

public Person(java.lang.String surname, org.jboss.test.ws.jbws1627.NickName[] nickNames){
this.surname=surname;
this.nickNames=nickNames;
}
public java.lang.String getSurname() { return surname ;}

public void setSurname(java.lang.String surname){ this.surname=surname; }

public org.jboss.test.ws.jbws1627.NickName[]  getNickNames() { return nickNames ;}

public void setNickNames(org.jboss.test.ws.jbws1627.NickName[] nickNames){ this.nickNames=nickNames; }

}
