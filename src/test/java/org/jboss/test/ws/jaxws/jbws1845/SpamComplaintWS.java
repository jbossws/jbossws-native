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
package org.jboss.test.ws.jaxws.jbws1845;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.ResponseWrapper;

import org.jboss.wsf.spi.annotation.WebContext;

/**
 * Webservice impl
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
@Stateless
@WebService
(
   targetNamespace="http://service.responsys.com/rsystools/ws/SpamComplaintWS/1.0",
   serviceName="SpamService"
)
@WebContext
(
   transportGuarantee="NONE",
   contextRoot="/jaxws-jbws1845",
   urlPattern="/SpamService"
)
public final class SpamComplaintWS implements SpamComplaintWSIface
{
   @WebMethod(operationName="processSpamComplaints")
   @WebResult(name="SpamResult")
   @ResponseWrapper(className="org.jboss.test.ws.jaxws.jbws1845.jaxws.SpamResult")
   public SpamResult processSpamComplaints(
       @WebParam(name = "email") String email,
       @WebParam(name = "fromAddress") String fromAddress,
       @WebParam(name = "mailDate") String mailDate,
       @WebParam(name = "complaintDate") String complaintDate,
       @WebParam(name = "mailbox") String mailbox,
       @WebParam(name = "complainer") String complainer,
       @WebParam(name = "xRext") String xRext,
       @WebParam(name = "accountName") String accountName)
   {
      return new SpamResult(email, fromAddress, mailDate, complaintDate, mailbox, complainer, xRext, accountName);
   }
}