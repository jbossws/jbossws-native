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


public class  Person implements java.io.Serializable
{

protected java.lang.String firstName;

protected java.lang.String surname;
public Person(){}

public Person(java.lang.String firstName, java.lang.String surname){
this.firstName=firstName;
this.surname=surname;
}
public java.lang.String getFirstName() { return firstName ;}

public void setFirstName(java.lang.String firstName){ this.firstName=firstName; }

public java.lang.String getSurname() { return surname ;}

public void setSurname(java.lang.String surname){ this.surname=surname; }

}
