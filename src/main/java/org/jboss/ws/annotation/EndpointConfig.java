package org.jboss.ws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 16.01.2007
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface EndpointConfig {

   /**
    * The optional config-name element gives the client configuration name that must be present in
    * the configuration given by element config-file.
    *
    * Server side default: Standard Endpoint
    * Client side default: Standard Client
    */
   String configName() default "";

   /**
    * The optional config-file element gives the to a URL or resource name for the configuration.
    *
    * Server side default: standard-jaxrpc-endpoint-config.xml, standard-jaxws-endpoint-config.xml
    * Client side default: standard-jaxrpc-client-config.xml, standard-jaxws-client-config.xml
    */
   String configFile() default "";

}
