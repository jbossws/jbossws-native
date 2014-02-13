package org.jboss.ws.tools;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Require the TCCL to this framework's classloader.
 *
 * User: rsearls
 * Date: 11/20/13
 */
public class ServerSideDocumentBuilder {
    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DocumentBuilderFactory factory;
        try {
            Thread.currentThread().setContextClassLoader(ServerSideDocumentBuilder.class.getClassLoader());
            factory = DocumentBuilderFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return factory;
    }
}
