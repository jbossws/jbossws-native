/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.metadata.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.ResourceLoaderAdapter;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.ws.common.utils.JBossWSEntityResolver;
import org.jboss.ws.metadata.config.binding.OMFactoryJAXRPC;
import org.jboss.ws.metadata.config.jaxrpc.ConfigRootJAXRPC;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.w3c.dom.Element;

/**
 * A factory for the JBossWS endpoint/client configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public class JBossWSConfigFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JBossWSConfigFactory.class);
   // provide logging
   private final Logger log = Logger.getLogger(JBossWSConfigFactory.class);

   private static String URN_JAXRPC_CONFIG = "urn:jboss:jaxrpc-config:2.0";
   private static String URN_JAXWS_CONFIG = "urn:jboss:jbossws-jaxws-config:4.0";
   
   private ClassLoader loader;

   // Hide constructor
   private JBossWSConfigFactory(ClassLoader loader)
   {
      //use a delegate classloader: first try lookup using the provided classloader,
      //otherwise use the native core module classloader (the default confs are there)
      this.loader = new DelegateClassLoader(JBossWSConfigFactory.class.getClassLoader(), loader);
   }

   /** Create a new instance of the factory
    */
   public static JBossWSConfigFactory newInstance()
   {
      return new JBossWSConfigFactory(getContextClassLoader());
   }

   public static JBossWSConfigFactory newInstance(ClassLoader loader)
   {
      return new JBossWSConfigFactory(loader);
   }

   public Object parse(URL configURL)
   {
      if(log.isDebugEnabled()) log.debug("parse: " + configURL);

      Object wsConfig;
      InputStream is = null;
      try
      {
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         unmarshaller.setValidation(true);
         unmarshaller.setSchemaValidation(true);
         unmarshaller.setEntityResolver(new JBossWSEntityResolver(loader));

         String nsURI = getNamespaceURI(configURL);
         is = configURL.openStream();
         if (URN_JAXRPC_CONFIG.equals(nsURI))
         {
            wsConfig = unmarshaller.unmarshal(is, new OMFactoryJAXRPC(), null);
         }
         else if (URN_JAXWS_CONFIG.equals(nsURI))
         {
            wsConfig = ConfigMetaDataParser.parse(is);
         }
         else
         {
            throw new WSException(BundleUtils.getMessage(bundle, "INVALID_CONFIG_NS",  nsURI));
         }

      }
      catch (JBossXBException e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "ERROR_WHILE_PARSING"),  e);
      }
      catch (IOException e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "FAILED_TO_READ_CONFIG_FILE",  configURL),  e);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ioe)
            {
               if(log.isDebugEnabled()) log.warn(ioe.getMessage(), ioe);
            }
         }
      }

      return wsConfig;
   }

   private String getNamespaceURI(URL configURL)
   {
      try
      {
         Element root = DOMUtils.parse(configURL.openStream());
         return root.getNamespaceURI();
      }
      catch (IOException ex)
      {
         throw new WSException(ex);
      }
   }

   /**
    * @return config - cannot be null
    */
   public CommonConfig getConfig(UnifiedVirtualFile vfsRoot, String configName, String configFile)
   {
      if(log.isDebugEnabled()) log.debug("getConfig: [name=" + configName + ",url=" + configFile + "]");

      if (configName == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CONFIG_NAME_CANNOT_BE_NULL"));
      if (configFile == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CONFIG_FILE_CANNOT_BE_NULL"));

      //First check if AS7 domain model is available and comes with the required endpoint config
      if (ConfigurationProvider.DEFAULT_JAXWS_ENDPOINT_CONFIG_FILE.equals(configFile)) {
         try {
            final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
            SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
            ServerConfig sc = spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
            for (org.jboss.wsf.spi.metadata.config.EndpointConfig config : sc.getEndpointConfigs())
            {
               if (config.getConfigName().equals(configName))
               {
                  return config;
               }
            }
         } catch (Exception e) {
            log.debug("Could not get server config");
         }
      }
      //otherwise (AS6 or not mached) try looking for the specified conf file...
      
      // Get the config root
      URL configURL = filenameToURL(vfsRoot, configFile);
      Object configRoot = parse(configURL);

      // Get the endpoint config
      CommonConfig config;
      if (configRoot instanceof ConfigRootJAXRPC)
      {
         config = ((ConfigRootJAXRPC)configRoot).getConfigByName(configName);
      }
      else
      {
         config = ((ConfigRoot)configRoot).getConfigByName(configName);
      }

      if (config == null)
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_CONFIG",  configName));

      return config;
   }

   private URL filenameToURL(UnifiedVirtualFile vfsRoot, String configFile)
   {
      URL configURL = null;
      try
      {
         configURL = vfsRoot.findChild(configFile).toURL();
      }
      catch (IOException ex)
      {
         // ignore
      }
      
      // Try to get the URL as resource
      if (configURL == null)
      {
         try
         {
            configURL = new ResourceLoaderAdapter(loader).findChild(configFile).toURL();
         }
         catch (IOException ex)
         {
            // ignore
         }
      }
      
      if (configURL == null)
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_FIND_CONFIGFILE",  configFile));
      
      return configURL;
   }
   
   /**
    * Get context classloader.
    * 
    * @return the current context classloader
    */
   private static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
}
