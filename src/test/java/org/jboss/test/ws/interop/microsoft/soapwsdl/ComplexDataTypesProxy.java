package org.jboss.test.ws.interop.microsoft.soapwsdl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Adapts a standard SEI to several test services
 * that share the same signature but got different SEI's.
 *
 * @author Heiko Braun <heiko@openj.net>
 * @since 19-02-2006
 */
public class ComplexDataTypesProxy implements InvocationHandler {

   private Object obj;

   public static Object newInstance(Object obj) {
      return java.lang.reflect.Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            new Class[] {ComplexDataTypesSEI.class},
            new ComplexDataTypesProxy(obj)
      );
   }

   private ComplexDataTypesProxy(Object obj) {
      this.obj = obj;
   }

   public Object invoke(Object proxy, Method m, Object[] args)
         throws Throwable
   {
      Object result;
      try {
         Method targetMethod = obj.getClass().getMethod(m.getName(), m.getParameterTypes());
         result = targetMethod.invoke(obj, args);
      } catch (InvocationTargetException e) {
         throw e.getTargetException();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
      } finally {
         //
      }
      return result;
   }
}
