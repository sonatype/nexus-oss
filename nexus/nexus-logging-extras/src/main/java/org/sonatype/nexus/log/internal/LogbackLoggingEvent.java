/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.log.internal;

import org.sonatype.nexus.logging.LoggingEvent;
import org.sonatype.plexus.appevents.AbstractEvent;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * Logback specific {@link LoggingEvent} implementation that adapts Logback @{link ILoggingEvent}.
 * 
 * @author adreghiciu@gmail.com
 */
public class LogbackLoggingEvent
    extends AbstractEvent<String>
    implements LoggingEvent
{

    /**
     * Logging level.
     */
    private final Level level;

    /**
     * Logging message.
     */
    private final String message;

    /**
     * Logging Throwable (if present), null otherwise.
     */
    private final Throwable throwable;

    /**
     * Constructor.
     * 
     * @param event Logback event
     */
    public LogbackLoggingEvent( ILoggingEvent event )
    {
        super( event.getLoggerName() );

        switch ( event.getLevel().levelInt )
        {

            case ch.qos.logback.classic.Level.TRACE_INT:
                level = Level.TRACE;
                break;

            case ch.qos.logback.classic.Level.DEBUG_INT:
                level = Level.DEBUG;
                break;

            case ch.qos.logback.classic.Level.INFO_INT:
                level = Level.INFO;
                break;

            case ch.qos.logback.classic.Level.WARN_INT:
                level = Level.WARN;
                break;

            case ch.qos.logback.classic.Level.ERROR_INT:
                level = Level.ERROR;
                break;

            default:
                throw new IllegalArgumentException( String.format(
                    "Log level %s is not supported. Supported levels: WARN, ERROR", event.getLevel() ) );
        }

        message = event.getFormattedMessage();

        if ( event.getThrowableProxy() instanceof ThrowableProxy )
        {
            throwable = ( (ThrowableProxy) event.getThrowableProxy() ).getThrowable();
        }
        else
        {
            throwable = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel()
    {
        return level;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public Throwable getThrowable()
    {
        return throwable;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "LogbackLoggingEvent [" );
        if ( level != null )
        {
            builder.append( "level=" );
            builder.append( level );
            builder.append( ", " );
        }
        if ( message != null )
        {
            builder.append( "message=" );
            builder.append( message );
            builder.append( ", " );
        }
        if ( throwable != null )
        {
            builder.append( "throwable=" );
            builder.append( throwable.getClass().getName() );
        }
        builder.append( "]" );
        return builder.toString();
    }
    
    

}
