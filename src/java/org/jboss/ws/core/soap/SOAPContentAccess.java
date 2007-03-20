package org.jboss.ws.core.soap;

import javax.xml.transform.Source;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 05.02.2007
 */
public interface SOAPContentAccess
{
   Source getPayload();

   void setPayload(Source source);

   XMLFragment getXMLFragment();

   void setXMLFragment(XMLFragment xmlFragment);

   Object getObjectValue();

   void setObjectValue(Object objValue);
}
