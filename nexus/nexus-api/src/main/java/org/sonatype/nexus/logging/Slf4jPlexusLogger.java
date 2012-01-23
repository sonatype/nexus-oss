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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper that wraps SLF4J logger into a Plexus Logger interface to be used with Legacy Plexus components.
 *
 * @author: cstamas
 */
public class Slf4jPlexusLogger
    implements org.codehaus.plexus.logging.Logger
{

    private final Logger logger;

    public Slf4jPlexusLogger( final Logger logger )
    {
        this.logger = checkNotNull( logger );
    }

    public Logger getSlf4jLogger()
    {
        return logger;
    }

    // ==

    @Override
    public void debug( final String message )
    {
        logger.debug( message );
    }

    @Override
    public void debug( final String message, final Throwable throwable )
    {
        logger.debug( message, throwable );
    }

    @Override
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    @Override
    public void info( final String message )
    {
        logger.info( message );
    }

    @Override
    public void info( final String message, final Throwable throwable )
    {
        logger.info( message, throwable );
    }

    @Override
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    @Override
    public void warn( final String message )
    {
        logger.warn( message );
    }

    @Override
    public void warn( final String message, final Throwable throwable )
    {
        logger.warn( message, throwable );
    }

    @Override
    public boolean isWarnEnabled()
    {
        return isWarnEnabled();
    }

    @Override
    public void error( final String message )
    {
        logger.error( message );
    }

    @Override
    public void error( final String message, final Throwable throwable )
    {
        logger.error( message, throwable );
    }

    @Override
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    @Override
    public void fatalError( final String message )
    {
        error( message );
    }

    @Override
    public void fatalError( final String message, final Throwable throwable )
    {
        error( message, throwable );
    }

    @Override
    public boolean isFatalErrorEnabled()
    {
        return isErrorEnabled();
    }

    // ==

    @Override
    public int getThreshold()
    {
        // unused
        return LEVEL_DEBUG;
    }

    @Override
    public void setThreshold( final int threshold )
    {
        // noop, it is a matter of Slf4j backend.
    }

    @Override
    public org.codehaus.plexus.logging.Logger getChildLogger( final String name )
    {
        // this is a noop implementation actually, since in Core there is only one component using this
        // feature that itself is disabled (parked).
        return this;
    }

    @Override
    public String getName()
    {
        return logger.getName();
    }

    // ==

    /**
     * Factory method for Plexus Logger instances, that uses the good old {@code LoggerFactory.getLogger(owner)} way
     * to obtain Slf4l Logger to have it wrapped into Slf4jPlexusLogger instance.
     *
     * @param owner
     * @return
     */
    public static org.codehaus.plexus.logging.Logger getPlexusLogger( final Class<?> owner )
    {
        return getPlexusLogger( LoggerFactory.getLogger( owner ) );
    }

    /**
     * Factory method for Plexus Logger instances, that wraps existing Slf4j Logger instances.
     *
     * @param logger
     * @return
     */
    public static org.codehaus.plexus.logging.Logger getPlexusLogger( final Logger logger )
    {
        return new Slf4jPlexusLogger( logger );
    }

}
