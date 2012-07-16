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
package org.jboss.test.ws.jaxws.samples.wssecurity;

import java.net.URL;
import java.security.cert.X509Certificate;

import org.jboss.ws.extensions.security.SecurityStore;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;

import org.jboss.wsf.test.JBossWSTest;

public class IssuerMatchTestCase extends JBossWSTest
{
  public void testIssuerMatch() throws Exception
  {
    //The space at the beginning of the issuer string causes
    //the signer to not match exactly
    //TODO: get these values from the certificate
    String issuer = "   EMAILADDRESS=admin@jboss.com, CN=jboss.com, OU=QA, O=JBoss Inc., L=Snoqualmie Pass, ST=Washington, C=US";
    String serial = "3";
    
    URL keystoreUrl = getResourceURL("jaxws/samples/wssecurity/wsse.keystore");
    WSSecurityConfiguration config = new WSSecurityConfiguration();
    config.setKeyStoreURL(keystoreUrl);
    config.setKeyStoreType("jks");
    config.setKeyStorePassword("jbossws");
    SecurityStore store = new SecurityStore(config);
    X509Certificate cert = store.getCertificateByIssuerSerial(issuer, serial);

    assertNotNull("Certificate null?", cert);
  }
}
