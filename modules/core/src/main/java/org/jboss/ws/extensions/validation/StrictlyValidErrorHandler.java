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
package org.jboss.ws.extensions.validation;

import org.jboss.logging.Logger;
import java.util.ResourceBundle;
import org.jboss.ws.api.util.BundleUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An error handler that throws a @{SAXException} on error and fatal error.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 29-Feb-2008
 */
public class StrictlyValidErrorHandler implements ErrorHandler
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(StrictlyValidErrorHandler.class);
   // provide logging
   private static Logger log = Logger.getLogger(StrictlyValidErrorHandler.class);
   
   public void error(SAXParseException ex) throws SAXException
   {
      log.error(BundleUtils.getMessage(bundle, ""));
      throw new SAXException(ex.getMessage());
   }

   public void fatalError(SAXParseException ex) throws SAXException
   {
      log.fatal(ex.toString());
      throw new SAXException(ex.getMessage());
   }

   public void warning(SAXParseException ex) throws SAXException
   {
      log.warn(BundleUtils.getMessage(bundle, ""));
   }
}
