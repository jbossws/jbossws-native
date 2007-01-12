package org.jboss.ws.metadata.wsse;

import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: hbraun
 * Date: 14.12.2006
 * Time: 16:17:02
 * To change this template use File | Settings | File Templates.
 */
public class WSSecurityConfigFactory
{

   public static WSSecurityConfigFactory newInstance()
   {
      return new WSSecurityConfigFactory();
   }

   public WSSecurityConfiguration createConfiguration(UnifiedDeploymentInfo udi) throws IOException
   {
      WSSecurityConfiguration config = null;

      String resource = WSSecurityOMFactory.SERVER_RESOURCE_NAME;
      if (udi.metaData instanceof UnifiedWebMetaData)
      {
         resource = "WEB-INF/" + resource;
      }
      else
      {
         resource = "META-INF/" + resource;
      }

      URL location = getResource(udi, resource);

      if (location != null)
      {
         config = WSSecurityOMFactory.newInstance().parse(location);

         // Get and set deployment path to the keystore file
         if (config.getKeyStoreFile() != null)
         {
            location = getResource(udi, config.getKeyStoreFile());
            if (location != null)
               config.setKeyStoreURL(location);
         }

         if (config.getTrustStoreFile() != null)
         {
            location = getResource(udi, config.getTrustStoreFile());
            if (location != null)
               config.setTrustStoreURL(location);
         }
      }

      return config;
   }

   private URL getResource(UnifiedDeploymentInfo udi, String resource)
   {
      try
      {
         URL url = udi.getMetaDataFileURL(resource);
         InputStream inputStream = url.openStream();
         inputStream.close();

         return url;
      }
      catch (IOException e)
      {
         return null;
      }
   }

}
