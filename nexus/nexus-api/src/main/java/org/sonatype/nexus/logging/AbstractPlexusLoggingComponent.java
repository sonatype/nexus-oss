/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
