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
package org.jboss.ws.extensions.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.security.exception.FailedAuthenticationException;
import org.jboss.ws.extensions.security.exception.WSSecurityException;
import org.jboss.ws.metadata.wsse.SecurityDomain;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.wsf.spi.security.JAASSecurityDomainAdaptor;
import org.jboss.wsf.spi.security.JAASSecurityDomainAdaptorResolver;
import org.jboss.wsf.spi.util.ServiceLoader;

/**
 * <code>SecurityStore</code> holds and loads the keystore and truststore required for encyption and signing.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Magesh Kumar B
 * @author Thomas.Diesler@jboss.com
 */
public class SecurityStore
{
   private static Logger log = Logger.getLogger(SecurityStore.class);

   private KeyStore keyStore;

   private String keyStorePassword;

   private KeyStore trustStore;

   private String trustStorePassword;

   private HashMap<String, String> keyPasswords;
   
   private JAASSecurityDomainAdaptor sd;
   
   private String securityDomainAuthToken;
   
   private boolean useSecurityDomainAliases;
   
   public SecurityStore() throws WSSecurityException
   {
      loadKeyStore(null, null, null);
      loadTrustStore(null, null, null);
   }
   
   public SecurityStore(WSSecurityConfiguration conf) throws WSSecurityException
   {
      if (conf == null)
      {
         return;
      }

      SecurityDomain securityDomainConf = conf.getSecurityDomain();
      if (securityDomainConf != null)
      {
         JAASSecurityDomainAdaptorResolver sdResolver = (JAASSecurityDomainAdaptorResolver)ServiceLoader.loadService(JAASSecurityDomainAdaptorResolver.class.getName(),
               null);
         if (sdResolver == null)
         {
            throw new WSSecurityException("Could not get a jaas security domain resolver implementation implementing " + JAASSecurityDomainAdaptorResolver.class
                  + "; this is container specific, so please check your classpath is properly set if running on client side.");
         }
         try
         {
            sd = sdResolver.lookup(securityDomainConf.getJndi());
         }
         catch (Exception e)
         {
            throw new WSSecurityException("JNDI failure handling " + securityDomainConf.getJndi(), e);
         }
         // if we reached this point, means we have a JNDI name pointing to a valid JAAS Security Domain
         keyStore = sd.getKeyStore();
         trustStore = sd.getTrustStore();
         securityDomainAuthToken = securityDomainConf.getAuthToken();
         useSecurityDomainAliases = securityDomainConf.isUseSecurityDomainAliases();
      }
      else
      {
         URL keyStoreURL = conf.getKeyStoreURL();
         String keyStoreType = conf.getKeyStoreType();
         String keyStorePassword = conf.getKeyStorePassword();
         URL trustStoreURL = conf.getTrustStoreURL();
         String trustStoreType = conf.getTrustStoreType();
         String trustStorePassword = conf.getTrustStorePassword();

         loadKeyStore(keyStoreURL, keyStoreType, keyStorePassword);
         loadTrustStore(trustStoreURL, trustStoreType, trustStorePassword);
      }
   }
   
   private void loadKeyStore(URL keyStoreURL, String keyStoreType, String keyStorePassword) throws WSSecurityException
   {
      if (keyStorePassword == null)
         keyStorePassword = SecurityActions.getSystemProperty("org.jboss.ws.wsse.keyStorePassword");

      keyStore = loadStore("org.jboss.ws.wsse.keyStore", "Keystore", keyStoreURL, keyStoreType, keyStorePassword);
      this.keyStorePassword = keyStorePassword;
   }

   private void loadTrustStore(URL trustStoreURL, String trustStoreType, String trustStorePassword) throws WSSecurityException
   {
      if (trustStorePassword == null)
         trustStorePassword = SecurityActions.getSystemProperty("org.jboss.ws.wsse.trustStorePassword");

      trustStore = loadStore("org.jboss.ws.wsse.trustStore", "Truststore", trustStoreURL, trustStoreType, trustStorePassword);
      this.trustStorePassword = trustStorePassword;
   }

   private KeyStore loadStore(String property, String type, URL storeURL, String storeType, String storePassword) throws WSSecurityException
   {
      if (storeURL == null)
      {
         String defaultStore = SecurityActions.getSystemProperty(property);
         if (defaultStore == null)
         {
            return null;
         }

         File storeFile = new File(defaultStore);
         try
         {
            storeURL = storeFile.toURL();
         }
         catch (MalformedURLException e)
         {
            throw new WSSecurityException("Problems loading " + type + ": " + e.getMessage(), e);
         }
      }

      if (storeType == null)
         storeType = SecurityActions.getSystemProperty(property + "Type");
      if (storeType == null)
         storeType = "jks";

      KeyStore keyStore = null;
      InputStream stream = null;
      try
      {
         if (log.isDebugEnabled())
            log.debug("loadStore: " + storeURL);
         stream = storeURL.openStream();
         if (stream == null)
            throw new WSSecurityException("Cannot load store from: " + storeURL);

         keyStore = KeyStore.getInstance(storeType);
         if (keyStore == null)
            throw new WSSecurityException("Cannot get keystore for type: " + storeType);

         String decryptedPassword = decryptPassword(storePassword);
         if (decryptedPassword == null)
            throw new WSSecurityException("Cannot decrypt store password");

         keyStore.load(stream, decryptedPassword.toCharArray());
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (WSSecurityException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new WSSecurityException("Problems loading " + type + ": " + ex.getMessage(), ex);
      }
      finally
      {
         if (stream != null)
         {
            try
            {
               stream.close();
            }
            catch (IOException ioe)
            {
               log.warn(ioe.getMessage(), ioe);
            }
         }
      }

      return keyStore;
   }

   /**
    * This method examines the password for the presence of a encryption algorithm, if found
    * decrypts and returns the password, else returns the password as is.
    */
   private String decryptPassword(String password) throws WSSecurityException
   {
      log.trace("decrypt password: " + password);

      if (password == null)
         throw new WSSecurityException("Invalid null password for security store");

      if (password.charAt(0) == '{')
      {
         StringTokenizer tokenizer = new StringTokenizer(password, "{}");
         String keyStorePasswordCmdType = tokenizer.nextToken();
         String keyStorePasswordCmd = tokenizer.nextToken();
         if (keyStorePasswordCmdType.equals("EXT"))
         {
            password = execPasswordCmd(keyStorePasswordCmd);
         }
         else if (keyStorePasswordCmdType.equals("CLASS"))
         {
            password = invokePasswordClass(keyStorePasswordCmd);
         }
         else
         {
            throw new WSSecurityException("Unknown keyStorePasswordCmdType: " + keyStorePasswordCmdType);
         }
      }
      if (password == null)
         throw new WSSecurityException("Cannot decrypt password, result is null");

      log.trace("decrypted password: " + password);
      return password;
   }

   private String execPasswordCmd(String keyStorePasswordCmd) throws WSSecurityException
   {
      boolean debugEnabled = log.isDebugEnabled();
      if (debugEnabled)
         log.debug("Executing cmd: " + keyStorePasswordCmd);
      try
      {
         String password = null;
         Runtime rt = Runtime.getRuntime();
         Process p = rt.exec(keyStorePasswordCmd);
         int status = p.waitFor();
         if (status == 0)
         {
            InputStream stdin = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            password = reader.readLine();
            stdin.close();
         }
         else
         {
            InputStream stderr = p.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stderr));
            String line = reader.readLine();
            while (line != null)
            {
               log.error(line);
               line = reader.readLine();
            }
            reader.close();
            stderr.close();
         }
         if (debugEnabled)
            log.debug("Command exited with: " + status);
         return password;
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems executing password cmd: " + keyStorePasswordCmd, e);
      }
   }

   private String invokePasswordClass(String keyStorePasswordCmd) throws WSSecurityException
   {
      String password = null;
      String classname = keyStorePasswordCmd;
      String ctorArg = null;
      int colon = keyStorePasswordCmd.indexOf(':');
      if (colon > 0)
      {
         classname = keyStorePasswordCmd.substring(0, colon);
         ctorArg = keyStorePasswordCmd.substring(colon + 1);
      }
      if (log.isDebugEnabled())
         log.debug("Loading class: " + classname + ", ctorArg=" + ctorArg);
      try
      {
         ClassLoader loader = SecurityActions.getContextClassLoader();
         Class c = loader.loadClass(classname);
         Object instance = null;
         if (ctorArg != null)
         {
            Class[] sig = { String.class };
            Constructor ctor = c.getConstructor(sig);
            Object[] args = { ctorArg };
            instance = ctor.newInstance(args);
         }
         else
         {
            instance = c.newInstance();
         }
         try
         {
            log.debug("Checking for toCharArray");
            Class[] sig = {};
            Method toCharArray = c.getMethod("toCharArray", sig);
            Object[] args = {};
            log.debug("Invoking toCharArray");
            password = new String((char[])toCharArray.invoke(instance, args));
         }
         catch (NoSuchMethodException e)
         {
            log.debug("No toCharArray found, invoking toString");
            password = instance.toString();
         }
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems loading or invoking Password class : " + classname, e);
      }
      return password;
   }

   public static byte[] getSubjectKeyIdentifier(X509Certificate cert)
   {
      // Maybee we should make one ourselves if it isn't there?
      byte[] encoded = cert.getExtensionValue("2.5.29.14");
      if (encoded == null)
         return null;

      // We need to skip 4 bytes [(OCTET STRING) (LENGTH)[(OCTET STRING) (LENGTH) (Actual data)]]
      int trunc = encoded.length - 4;

      byte[] identifier = new byte[trunc];
      System.arraycopy(encoded, 4, identifier, 0, trunc);

      return identifier;
   }

   public X509Certificate getCertificate(String alias, String securityDomainAliasLabel) throws WSSecurityException
   {
      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }

      X509Certificate cert;
      try
      {
         cert = (X509Certificate)keyStore.getCertificate(resolveAlias(alias, securityDomainAliasLabel));
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems retrieving cert: " + e.getMessage(), e);
      }

      if (cert == null)
         throw new WSSecurityException("Certificate (" + alias + ") not in keystore");

      return cert;
   }
   
   private String resolveAlias(String alias, String label)
   {
      if (useSecurityDomainAliases && label != null)
      {
         Properties props = sd.getAdditionalOptions();
         return props.getProperty(label);
      }
      return alias;
   }
   
   public X509Certificate getCertificateByPublicKey(PublicKey key) throws WSSecurityException
   {
      if (key == null)
         return null;
      
      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }
      
      try
      {
         Enumeration<String> i = keyStore.aliases();
         while (i.hasMoreElements())
         {
            String alias = (String)i.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (!(cert instanceof X509Certificate))
               continue;

            if (cert.getPublicKey().equals(key))
               return (X509Certificate)cert;
         }
         return null;
      }
      catch (KeyStoreException e)
      {
         throw new WSSecurityException("Problems retrieving cert: " + e.getMessage(), e);
      }
   }

   public X509Certificate getCertificateBySubjectKeyIdentifier(byte[] identifier) throws WSSecurityException
   {
      if (identifier == null)
         return null;

      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }

      try
      {
         Enumeration<String> i = keyStore.aliases();

         while (i.hasMoreElements())
         {
            String alias = (String)i.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (!(cert instanceof X509Certificate))
               continue;

            byte[] subjectKeyIdentifier = getSubjectKeyIdentifier((X509Certificate)cert);
            if (subjectKeyIdentifier == null)
               continue;

            if (Arrays.equals(identifier, subjectKeyIdentifier))
               return (X509Certificate)cert;
         }
      }
      catch (KeyStoreException e)
      {
         throw new WSSecurityException("Problems retrieving cert: " + e.getMessage(), e);
      }

      return null;
   }

   public X509Certificate getCertificateByIssuerSerial(String issuer, String serial) throws WSSecurityException
   {
      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }

      try
      {
         Enumeration i = keyStore.aliases();

         while (i.hasMoreElements())
         {
            String alias = (String)i.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (!(cert instanceof X509Certificate))
               continue;

            X509Certificate x509 = (X509Certificate)cert;
            if (issuer.equals(x509.getIssuerDN().toString()) && serial.equals(x509.getSerialNumber().toString()))
               return x509;
         }
      }
      catch (KeyStoreException e)
      {
         throw new WSSecurityException("Problems retrieving cert: " + e.getMessage(), e);
      }

      return null;
   }

   public PrivateKey getPrivateKey(String alias, String securityDomainAliasLabel) throws WSSecurityException
   {
      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }
      
      PrivateKey key;
      try
      {
         if (sd == null)
         {
            String password = keyStorePassword;
            if (keyPasswords != null && keyPasswords.containsKey(alias))
                password = keyPasswords.get(alias);
            key = (PrivateKey)keyStore.getKey(alias, decryptPassword(password).toCharArray());
         }
         else
         {
            key = (PrivateKey)sd.getKey(resolveAlias(alias, securityDomainAliasLabel), securityDomainAuthToken);
         }
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems retrieving private key: " + e.getMessage(), e);
      }

      if (key == null)
         throw new WSSecurityException("Private key (" + alias + ") not in keystore");

      return key;
   }

   public PrivateKey getPrivateKey(X509Certificate cert) throws WSSecurityException
   {
      if (keyStore == null)
      {
         throw new WSSecurityException("KeyStore not set.");
      }

      try
      {
         String alias = keyStore.getCertificateAlias(cert);
         return getPrivateKey(alias, null);
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems retrieving private key: " + e.getMessage(), e);
      }
   }

   public void validateCertificate(X509Certificate cert) throws WSSecurityException
   {
      try
      {
         cert.checkValidity();
      }
      catch (Exception e)
      {
         log.debug("Certificate is invalid", e);
         throw new FailedAuthenticationException();
      }

      if (keyStore == null)
      {
         throw new WSSecurityException("TrustStore not set.");
      }

      // Check for the exact entry in the truststore first, then fallback to a CA check
      try
      {
         if (trustStore.getCertificateAlias(cert) != null)
         {
            return;
         }
      }
      catch (KeyStoreException e)
      {
         throw new WSSecurityException("Problems searching truststore", e);
      }

      List list = new ArrayList(1);
      list.add(cert);

      CertPath cp;
      CertPathValidator cpv;
      PKIXParameters parameters;

      try
      {
         cp = CertificateFactory.getInstance("X.509").generateCertPath(list);
         cpv = CertPathValidator.getInstance("PKIX");
         parameters = new PKIXParameters(trustStore);

         // We currently don't support CRLs
         parameters.setRevocationEnabled(false);
      }
      catch (Exception e)
      {
         throw new WSSecurityException("Problems setting up certificate validation", e);
      }

      try
      {
         cpv.validate(cp, parameters);
      }
      catch (CertPathValidatorException cpve)
      {
         log.debug("Certificate is invalid:", cpve);
         throw new FailedAuthenticationException();
      }
      catch (InvalidAlgorithmParameterException e)
      {
         throw new WSSecurityException("Problems setting up certificate validation", e);
      }
   }
}
