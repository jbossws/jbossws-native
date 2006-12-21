package org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesRpcLit_Service;

import org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.*;

import java.rmi.RemoteException;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 06-Mar-2006
 */
public class ServiceImpl implements IComplexDataTypesRpcLit {
   public ArrayOfstring retArrayString1D(ArrayOfstring inArrayString1D) throws RemoteException {
      return inArrayString1D;
   }

   public ArrayOfint retArrayInt1D(ArrayOfint inArrayInt1D) throws RemoteException {
      return inArrayInt1D;
   }

   public ArrayOfNullableOfdecimal retArrayDecimal1D(ArrayOfNullableOfdecimal inArrayDecimal1D) throws RemoteException {
      return inArrayDecimal1D;
   }

   public ArrayOfNullableOfdateTime retArrayDateTime1D(ArrayOfNullableOfdateTime inArrayDateTime1D) throws RemoteException {
      return inArrayDateTime1D;
   }

   public ArrayOfArrayOfstring retArrayString2D(ArrayOfArrayOfstring inArrayString2D) throws RemoteException {
      return inArrayString2D;
   }

   public ArrayOfPerson retArray1D_SN(ArrayOfPerson inArray1D_SN) throws RemoteException {
      return inArray1D_SN;
   }

   public ArrayOfanyType retArrayAnyType1D(ArrayOfanyType inArrayAnyType1D) throws RemoteException {
      return inArrayAnyType1D;
   }

   public Name retStructS1(Name inStructS1) throws RemoteException {
      return inStructS1;
   }

   public Person retStructSN(Person inStructSN) throws RemoteException {
      return inStructSN;
   }

   public Employee retStructSNSA(Employee inStructSNSA) throws RemoteException {
      return inStructSNSA;
   }

   public Group retStructSNSAS(Group inStructSNSAS) throws RemoteException {
      return inStructSNSAS;
   }

   public BitMask retEnumString(BitMask inEnumString) throws RemoteException {
      return inEnumString;
   }

   public IntSet retEnumInt(IntSet inEnumInt) throws RemoteException {
      return inEnumInt;
   }

   public Table retDerivedClass(Table inDerivedClass) throws RemoteException {
      return inDerivedClass;
   }
}
