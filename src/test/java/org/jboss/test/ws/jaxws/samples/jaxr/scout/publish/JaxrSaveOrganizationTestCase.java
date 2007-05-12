/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.samples.jaxr.scout.publish;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;

import org.jboss.test.ws.jaxws.samples.jaxr.scout.JaxrBaseTest;

/**
 * Tests Jaxr Save Organization
 *
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 * @since Dec 29, 2004
 */

public class JaxrSaveOrganizationTestCase extends JaxrBaseTest
{
   private Key orgKey = null;

   public void testSaveOrg() throws JAXRException
   {
      String keyid = "";
      login();

      rs = connection.getRegistryService();

      blm = rs.getBusinessLifeCycleManager();
      Collection orgs = new ArrayList();
      Organization org = createOrganization("JBOSS");

      orgs.add(org);
      BulkResponse br = blm.saveOrganizations(orgs);
      if (br.getStatus() == JAXRResponse.STATUS_SUCCESS)
      {
         log.debug("Organization Saved");
         Collection coll = br.getCollection();
         Iterator iter = coll.iterator();
         while (iter.hasNext())
         {
            Key key = (Key)iter.next();
            keyid = key.getId();
            log.debug("Saved Key=" + key.getId());
            assertNotNull(keyid);
         }//end while
      }
      else
      {
         log.error("JAXRExceptions " + "occurred during save:");
         Collection exceptions = br.getExceptions();
         Iterator iter = exceptions.iterator();
         while (iter.hasNext())
         {
            Exception e = (Exception)iter.next();
            log.error(e.toString());
            fail(e.toString());
         }
      }
      checkBusinessExists("JBOSS");
   }

   private void checkBusinessExists(String bizname) throws JAXRException
   {
      String request = "<find_business generic='2.0' xmlns='urn:uddi-org:api_v2'>" + "<name xml:lang='en'>" + bizname + "</name></find_business>";
      String response = rs.makeRegistrySpecificRequest(request);
      if (response == null || "".equals(response))
         fail("Find Business failed");

   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      if (this.orgKey != null)
         this.deleteOrganization(orgKey);
   }
}
