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
