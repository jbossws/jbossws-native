package org.jboss.ws.tools.jaxws.api;

import java.io.File;
import java.io.PrintStream;

import org.jboss.ws.WSException;
import org.jboss.ws.tools.jaxws.spi.WebServiceGeneratorProvider;


/**
 * WebServiceGenerator is responsible for generating the required portable
 * JAX-WS artifacts for a service endpoint implementation. This includes class
 * files for wrapper types and fault beans. WSDL may be optionally generated as
 * well using this API.
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * 
 * <p>The following example generates class files, source files and WSDL for an
 * endpoint:</p> 
 * <pre>
 * WebServiceGenerator generator = WebServiceGenerator.newInstance();
 * generator.setGenerateSource(true);
 * generator.setGenerateWsdl(true);
 * generator.setOutputDirectory(new File("output"));
 * generator.setMessageStream(System.out);
 * generator.generate(TestMe.class);
 * </pre>
 * 
 * <p>Thread-Safety:</p>
 * This class expects to be thread-confined, so it can not be shared between threads.
 */
public abstract class WebServiceGenerator
{
   private static String DEFAULT_PROVIDER = "org.jboss.ws.tools.jaxws.impl.WebServiceGeneratorProviderImpl";
   public static final String PROVIDER_PROPERTY = "org.jboss.ws.tools.jaxws.webServiceGeneratorProvider";
   
   protected WebServiceGenerator() 
   {
      
   }
   
   /**
    * Obtain a new instance of a WebServiceGenerator. This will use the current
    * thread's context class loader to locate the WebServiceGeneratorProvider
    * implementation.
    * 
    * @return a new WebServiceGenerator
    */
   public static WebServiceGenerator newInstance()
   {
      return newInstance(Thread.currentThread().getContextClassLoader());
   }
   
   /**
    * Obtain a new instance of a WebServiceGenerator. The specified ClassLoader will be used to
    * locate the WebServiceGeneratorProvide implementation
    * 
    * @param loader the ClassLoader to use
    * @return a new WebServiceGenerator
    */
   public static WebServiceGenerator newInstance(ClassLoader loader)
   {
      WebServiceGeneratorProvider provider = ProviderLocator.locate(WebServiceGeneratorProvider.class, PROVIDER_PROPERTY, DEFAULT_PROVIDER, loader);
      return provider.createGenerator(loader);
   }
   
   /**
    * Enables/Disables WSDL generation.
    * 
    * @param generateWsdl whether or not to generate WSDL
    */
   public abstract void setGenerateWsdl(boolean generateWsdl);
   
   /**
    * Enables/Disables Java source generation.
    * 
    * @param generateSource whether or not to generate Java source.
    */
   public abstract void setGenerateSource(boolean generateSource);
   
   /**
    * Sets the main output directory. If the directory does not exist, it will be created.
    * 
    * @param directory the root directory for generated files
    */
   public abstract void setOutputDirectory(File directory);
   
   /**
    * Sets the resource directory. This directory will contain any generated
    * WSDL and XSD files. If the directory does not exist, it will be created.
    * If not specified, the output directory will be used instead.
    * 
    * @param directory the root directory for generated resource files
    */
   public abstract void setResourceDirectory(File directory);
  
   /**
    * Sets the source directory. This directory will contain any generated Java source.
    * If the directory does not exist, it will be created. If not specified, 
    * the output directory will be used instead.
    * 
    * @param directory the root directory for generated source code
    */
   public abstract void setSourceDirectory(File directory);
   
   /**
    * Sets the ClassLoader used to discover types. This defaults to the one used
    * in instantiation.
    * 
    * @param loader the ClassLoader to use
    */
   public abstract void setClassLoader(ClassLoader loader);
   
   /**
    * Generates artifacts using the current settings. This method may be invoked
    * more than once (e.g. multiple endpoints).
    * 
    * @param endpointClass the name of the endpoint implementation bean
    * @throws WSException if any error occurs during processing, or the class is not found
    */
   public abstract void generate(String endpointClass);

   /**
    * Generates artifacts using the current settings. This method may be invoked
    * more than once (e.g. multiple endpoints).
    * 
    * @param endpointClass the endpoint implementation bean
    * @throws WSException if any error occurs during processing
    */
   public abstract void generate(Class<?> endpointClass);
   
   /**
    * Sets the PrintStream to use for status feedback. The simplest example
    * would be to use System.out.
    * 
    * <p>Example output:</p> 
    * <pre>
    * Generating WSDL: 
    * TestMeService.wsdl 
    * Writing Source:
    * org/jboss/ws/tools/jaxws/TestMe.java
    * org/jboss/ws/tools/jaxws/TestMeResponse.java 
    * Writing Classes:
    * org/jboss/ws/tools/jaxws/TestMe.class
    * org/jboss/ws/tools/jaxws/TestMeResponse.class
    * </pre>
    * @param messageStream  the stream to use for status messages:
    */
   public abstract void setMessageStream(PrintStream messageStream);
}
