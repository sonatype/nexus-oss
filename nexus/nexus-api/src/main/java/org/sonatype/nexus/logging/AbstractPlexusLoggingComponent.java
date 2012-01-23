/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.logging;

import static com.google.common.base.Preconditions.checkNotNull;

import org.codehaus.plexus.logging.Logger;

/**
 * Plexus' AbstractLogEnabled in compatibility way.
 *
 * @author: cstamas
 * @deprecated To be used by components still relying on Plexus Logger only, but in general, avoid this! Use
 *             AbstractLoggingComponent or directly SLF4J API instead!
 */
public class AbstractPlexusLoggingComponent
{

    private final Logger plexusLogger;

    protected AbstractPlexusLoggingComponent()
    {
        this.plexusLogger = checkNotNull( createLogger() );
    }

    /**
     * Creates logger instance to be used with component instance. It might be overridden by subclasses to implement
     * alternative logger naming strategy. By default, this method does the "usual" fluff: {@code Slf4jPlexusLogger.getPlexusLogger(getClass() )}.
     *
     * @return The Logger instance to be used by component for logging.
     */
    protected Logger createLogger()
    {
        return Slf4jPlexusLogger.getPlexusLogger( getClass() );
    }

    /**
     * Returns the Logger instance of this component. Never returns {@code null}.
     *
     * @return
     */
    protected Logger getLogger()
    {
        return plexusLogger;
    }

}
