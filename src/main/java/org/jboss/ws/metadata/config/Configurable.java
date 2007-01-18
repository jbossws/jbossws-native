package org.jboss.ws.metadata.config;

import java.util.Observer;

/**
 * A marker interface that identifies configurable JBossWS components.
 * Configurables may register themselves with a {@link ConfigurationProvider} in order
 * to get notified then the configuration changes.
 *
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 15.12.2006
 */
public interface Configurable extends Observer {
}
