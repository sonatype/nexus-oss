/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.siesta;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.log.LogConfigurationParticipant;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkState;

/**
 * Contributes "logback-siesta.xml" logging configuration.
 * 
 * @since 2.3
 */
@Named
@Singleton
public class LogConfigurationParticipantImpl
    implements LogConfigurationParticipant
{
    private static final Logger log = LoggerFactory.getLogger(LogConfigurationParticipantImpl.class);

    @Override
    public String getName() {
        return "logback-siesta.xml";
    }

    @Override
    public InputStream getConfiguration() {
        URL resource = getClass().getResource(getName());
        log.debug("Using resource: {}", resource);
        checkState(resource != null);
        try {
            assert resource != null; // Keep IDEA happy
            return resource.openStream();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
