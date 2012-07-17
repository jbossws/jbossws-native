/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
/**
 * LSInput implementation
 * 
 * @author ema@redhat.com
 */

public class LSInputImpl implements LSInput {

    protected String publicId;

    protected String systemId;

    protected String baseSystemId;

    protected InputStream byteStream;

    protected Reader charStream;

    protected String data;

    protected String encoding;

    protected boolean certifiedText;

    public LSInputImpl() {
    }

    public LSInputImpl(String systemId, InputStream byteStream) {
        this.systemId = systemId;
        this.byteStream = byteStream;
    }
    
    public LSInputImpl(String publicId, String systemId, InputStream byteStream) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.byteStream = byteStream;
    }

    public InputStream getByteStream() {
        return byteStream;
    }

    public void setByteStream(InputStream byteStream) {
        this.byteStream = byteStream;
    }

    public Reader getCharacterStream() {
        return charStream;
    }

    public void setCharacterStream(Reader characterStream) {
        charStream = characterStream;
    }

    public String getStringData() {
        return data;
    }

    public void setStringData(String stringData) {
        data = stringData;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getBaseURI() {
        return baseSystemId;
    }

    public void setBaseURI(String baseURI) {
        this.baseSystemId = baseURI;
    }

    public boolean getCertifiedText() {
        return certifiedText;
    }

    public void setCertifiedText(boolean certifiedText) {
        this.certifiedText = certifiedText;
    }

}

