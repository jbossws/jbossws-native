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
package org.jboss.ws.core.soap;

import java.util.ResourceBundle;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;

/**
 * Represents the DOM_VALID state of an {@link SOAPContentElement}.<br>
 *
 * @author Heiko.Braun@jboss.org
 * @since 05.02.2007
 */
public class DOMContent extends SOAPContent
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DOMContent.class);
   private static Logger log = Logger.getLogger(DOMContent.class);

   private Source payload;

   protected DOMContent(SOAPContentElement container)
   {
      super(container);
   }

   State getState()
   {
      return State.DOM_VALID;
   }

   SOAPContent transitionTo(State nextState)
   {
      SOAPContent next = null;

      if (State.XML_VALID == nextState)
      {
         log.debug("getXMLFragment from DOM");
         DOMSource domSource = new DOMSource(container);
         XMLFragment fragment = new XMLFragment(domSource);
         if (log.isDebugEnabled())
            log.debug("xmlFragment: " + fragment);

         SOAPContent xmlValid = new XMLContent(container);
         xmlValid.setXMLFragment(fragment);
         next = xmlValid;
      }
      else if (State.OBJECT_VALID == nextState)
      {
         // transition to xml valid first
         XMLFragment fragment = new XMLFragment(new DOMSource(container));
         XMLContent tmpState = new XMLContent(container);
         tmpState.setXMLFragment(fragment);

         // and from XML valid to Object valid
         next = tmpState.transitionTo(State.OBJECT_VALID);

      }
      else if (State.DOM_VALID == nextState)
      {
         next = this;
      }
      else
      {
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "ILLEGAL_STATE_REQUESTED",  nextState));
      }

      return next;
   }

   public Source getPayload()
   {
      return new DOMSource(container);
   }

   public void setPayload(Source source)
   {
      if (!(source instanceof DOMSource))
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "DOMSOURCE_EXPECTED",  source));
      
      this.payload = source;
   }

   public XMLFragment getXMLFragment()
   {
      throw new IllegalStateException(BundleUtils.getMessage(bundle, "XMLFRAGMENT_NOT_AVAILABLE"));
   }

   public void setXMLFragment(XMLFragment xmlFragment)
   {
      throw new IllegalStateException(BundleUtils.getMessage(bundle, "XMLFRAGMENT_NOT_AVAILABLE"));
   }

   public Object getObjectValue()
   {
      throw new IllegalStateException(BundleUtils.getMessage(bundle, "OBJECT_VALUE_NOT_AVAILABLE"));
   }

   public void setObjectValue(Object objValue)
   {
      throw new IllegalStateException(BundleUtils.getMessage(bundle, "OBJECT_VALUE_NOT_AVAILABLE"));
   }
}
