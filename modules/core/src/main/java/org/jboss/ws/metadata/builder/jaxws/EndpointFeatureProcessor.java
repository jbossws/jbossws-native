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
package org.jboss.ws.metadata.builder.jaxws;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.ws.WSException;
import org.jboss.ws.annotation.FastInfoset;
import org.jboss.ws.annotation.JsonEncoding;
import org.jboss.ws.annotation.SchemaValidation;
import org.jboss.ws.feature.FastInfosetFeature;
import org.jboss.ws.feature.JsonEncodingFeature;
import org.jboss.ws.feature.SchemaValidationFeature;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.xml.sax.ErrorHandler;

/**
 * Process EndpointFeature annotations
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Mar-2008
 */
public class EndpointFeatureProcessor
{
   protected void processEndpointFeatures(Deployment dep, ServerEndpointMetaData sepMetaData, Class<?> sepClass)
   {
      for (Annotation an : sepClass.getAnnotations())
      {
         if (an.annotationType() == SchemaValidation.class)
         {
            processSchemaValidation(dep, sepMetaData, sepClass);
         }
         else if (an.annotationType() == FastInfoset.class)
         {
            FastInfoset anFeature = sepClass.getAnnotation(FastInfoset.class);
            FastInfosetFeature feature = new FastInfosetFeature(anFeature.enabled());
            sepMetaData.addFeature(feature);
         }
         else if (an.annotationType() == JsonEncoding.class)
         {
            JsonEncoding anFeature = sepClass.getAnnotation(JsonEncoding.class);
            JsonEncodingFeature feature = new JsonEncodingFeature(anFeature.enabled());
            sepMetaData.addFeature(feature);
         }
      }
   }

   private void processSchemaValidation(Deployment dep, ServerEndpointMetaData sepMetaData, Class<?> sepClass)
   {
      SchemaValidation anFeature = sepClass.getAnnotation(SchemaValidation.class);
      SchemaValidationFeature feature = new SchemaValidationFeature(anFeature.enabled());

      String xsdLoc = anFeature.schemaLocation();
      if (xsdLoc.length() > 0)
      {
         if (dep instanceof ArchiveDeployment)
         {
            try
            {
               URL xsdURL = ((ArchiveDeployment)dep).getMetaDataFileURL(xsdLoc);
               xsdLoc = xsdURL.toExternalForm();
            }
            catch (IOException ex)
            {
               throw new WSException("Cannot load schema: " + xsdLoc, ex);
            }
         }
         feature.setSchemaLocation(xsdLoc);
      }

      Class handlerClass = anFeature.errorHandler();
      if (handlerClass != null)
      {
         try
         {
            ErrorHandler errorHandler = (ErrorHandler)handlerClass.newInstance();
            feature.setErrorHandler(errorHandler);
         }
         catch (Exception ex)
         {
            throw new WSException("Cannot instanciate error handler: " + handlerClass, ex);
         }
      }
      sepMetaData.addFeature(feature);
   }
}
