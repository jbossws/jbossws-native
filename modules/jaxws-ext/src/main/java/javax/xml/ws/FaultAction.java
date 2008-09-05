/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package javax.xml.ws;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>FaultAction</code> annotation is used inside an <a href="Action.html">
 * Action</a> annotation to allow an explicit association of <code>Action</code> message
 * addressing property with the <code>fault</code> messages of the WSDL operation mapped from
 * the exception class.
 * <p>
 * The <code>fault</code> message in the generated WSDL operation mapped for <code>className</code> 
 * class contains explicit <code>wsaw:Action</code> attribute.
 *
 * <p>
 * <b>Example 1</b>: Specify explicit values for <code>Action</code> message addressing 
 * property for the <code>input</code>, <code>output</code> and <code>fault</code> message 
 * if the Java method throws only one service specific exception.
 * 
 * <pre>
 * &#64;javax.jws.WebService
 * public class AddNumbersImpl {
 *     &#64;javax.xml.ws.Action(
 *         input=&quot;http://example.com/inputAction&quot;,
 *         output=&quot;http://example.com/outputAction&quot;,
 *         fault = {
 *             &#64;javax.xml.ws.FaultAction(className=AddNumbersException.class, value=&quot;http://example.com/faultAction&quot;)
 *         })
 *     public int addNumbers(int number1, int number2) 
 *         throws AddNumbersException {
 *         return number1 + number2;
 *     }
 * }
 * </pre>
 * 
 * The generated WSDL looks like:
 * 
 * <pre>
 *   &lt;definitions targetNamespace=&quot;http://example.com/numbers&quot; ...&gt;
 *   ...
 *     &lt;portType name=&quot;AddNumbersPortType&quot;&gt;
 *       &lt;operation name=&quot;AddNumbers&quot;&gt;
 *         &lt;input message=&quot;tns:AddNumbersInput&quot; name=&quot;Parameters&quot;
 *           wsaw:Action=&quot;http://example.com/inputAction&quot;/&gt;
 *        &lt;output message=&quot;tns:AddNumbersOutput&quot; name=&quot;Result&quot;
 *          wsaw:Action=&quot;http://example.com/outputAction&quot;/&gt;
 *        &lt;fault message=&quot;tns:AddNumbersException&quot; name=&quot;AddNumbersException&quot;
 *          wsaw:Action=&quot;http://example.com/faultAction&quot;/&gt;
 *       &lt;/operation&gt;
 *     &lt;portType&gt;
 *   ...
 *   &lt;definitions&gt;
 * </pre>
 *
 * <p>
 * Example 2: Here is an example that shows how to specify explicit values for <code>Action</code> 
 * message addressing property if the Java method throws only one service specific exception, 
 * without specifying the values for <code>input</code> and <code>output</code> messages.
 * 
 * <pre>
 * &#64;javax.jws.WebService
 * public class AddNumbersImpl {
 *     &#64;javax.xml.ws.Action(
 *         fault = {
 *             &#64;javax.xml.ws.FaultAction(className=AddNumbersException.class, value=&quot;http://example.com/faultAction&quot;)
 *         })
 *     public int addNumbers(int number1, int number2) 
 *         throws AddNumbersException {
 *         return number1 + number2;
 *     }
 * }
 * </pre>
 * 
 * The generated WSDL looks like:
 * 
 * <pre>
 *   &lt;definitions targetNamespace=&quot;http://example.com/numbers&quot; ...&gt;
 *   ...
 *     &lt;portType name=&quot;AddNumbersPortType&quot;&gt;
 *       &lt;operation name=&quot;AddNumbers&quot;&gt;
 *         &lt;input message=&quot;tns:AddNumbersInput&quot; name=&quot;Parameters&quot;/&gt;
 *         &lt;output message=&quot;tns:AddNumbersOutput&quot; name=&quot;Result&quot;/&gt;
 *         &lt;fault message=&quot;tns:addNumbersFault&quot; name=&quot;InvalidNumbers&quot;
 *           wsa:Action=&quot;http://example.com/addnumbers/fault&quot;/&gt;
 *       &lt;/operation&gt;
 *     &lt;portType&gt;
 *   ...
 *   &lt;definitions&gt;
 * </pre>
 * 
 * <p>
 * Example 3: Here is an example that shows how to specify explicit values for <code>Action</code> 
 * message addressing property if the Java method throws more than one service specific exception.
 * 
 * <pre>
 * &#64;javax.jws.WebService
 * public class AddNumbersImpl {
 *     &#64;javax.xml.ws.Action(
 *         fault = {
 *             &#64;javax.xml.ws.FaultAction(className=AddNumbersException.class, value=&quot;http://example.com/addFaultAction&quot;)
 *             &#64;javax.xml.ws.FaultAction(className=TooBigNumbersException.class, value=&quot;http://example.com/toobigFaultAction&quot;)
 *         })
 *     public int addNumbers(int number1, int number2) 
 *         throws AddNumbersException, TooBigNumbersException {
 *         return number1 + number2;
 *     }
 * }
 * </pre>
 * 
 * The generated WSDL looks like:
 * 
 * <pre>
 *   &lt;definitions targetNamespace=&quot;http://example.com/numbers&quot; ...&gt;
 *   ...
 *     &lt;portType name=&quot;AddNumbersPortType&quot;&gt;
 *       &lt;operation name=&quot;AddNumbers&quot;&gt;
 *         &lt;input message=&quot;tns:AddNumbersInput&quot; name=&quot;Parameters&quot;/&gt;
 *         &lt;output message=&quot;tns:AddNumbersOutput&quot; name=&quot;Result&quot;/&gt;
 *         &lt;fault message=&quot;tns:addNumbersFault&quot; name=&quot;AddNumbersException&quot;
 *           wsa:Action=&quot;http://example.com/addnumbers/fault&quot;/&gt;
 *         &lt;fault message=&quot;tns:tooBigNumbersFault&quot; name=&quot;TooBigNumbersException&quot;
 *           wsa:Action=&quot;http://example.com/toobigFaultAction&quot;/&gt;
 *       &lt;/operation&gt;
 *     &lt;portType&gt;
 *   ...
 *   &lt;definitions&gt;
 * </pre>
 * 
 * @since JAX-WS 2.1
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FaultAction {
   /**
    * Name of the exception class
    */
   Class className();

   /**
    * Value of <code>Action</code> message addressing property for the exception
    */
   String value() default "";
}
