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
package org.jboss.test.ws.jaxws.samples.news;

import java.util.Date;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;

/**
 * The MTOM version of the newspaper edition
 *
 * @author alessio.soldano@jboss.org
 * @since 31-Jan-2008
 */
public class EditionMTOM
{
   private Date date;
   private DataHandler content;
   private String id;
   
   public Date getDate()
   {
      return date;
   }
   public void setDate(Date date)
   {
      this.date = date;
   }
   @XmlMimeType("text/plain")
   public DataHandler getContent()
   {
      return content;
   }
   public void setContent(DataHandler content)
   {
      this.content = content;
   }
   public String getId()
   {
      return id;
   }
   public void setId(String id)
   {
      this.id = id;
   }
}
