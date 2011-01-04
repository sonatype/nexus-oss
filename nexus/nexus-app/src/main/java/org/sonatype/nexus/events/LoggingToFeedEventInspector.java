/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.events;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.log.ErrorWarningRecordAppender;
import org.sonatype.nexus.log.LogConfig;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * @author juven
 */
@Component( role = EventInspector.class, hint = "LoggingToFeedEventInspector" )
public class LoggingToFeedEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private FeedRecorder feedRecorder;

    @Requirement
    private LogManager logManager;

    private static final String RECORDER_APPENDER_KEY_SHORT = "record";

    private static final String RECORDER_APPENDER_KEY = "log4j.appender." + RECORDER_APPENDER_KEY_SHORT;

    private static final String RECORDER_APPENDER_VALUE = ErrorWarningRecordAppender.class.getName();

    private static final String ROOT_LOGGER_KEY = "log4j.rootLogger";

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof NexusStartedEvent || evt instanceof NexusStoppedEvent );
    }

    public void inspect( Event<?> evt )
    {
        if ( logConfigEnabled() )
        {
            if ( evt instanceof NexusStartedEvent )
            {
                ensureErrorWarningAppender();

                startToRecordErrorWarningLog();
            }
            else if ( evt instanceof NexusStoppedEvent )
            {
                stopRecordErrorWarningLog();
            }
        }
    }

    private void startToRecordErrorWarningLog()
    {
        ErrorWarningRecordAppender appender = getAppender();

        if ( appender != null )
        {
            appender.setFeedRecorder( feedRecorder );
        }
    }

    private void stopRecordErrorWarningLog()
    {
        ErrorWarningRecordAppender appender = getAppender();

        if ( appender != null )
        {
            appender.close();
        }
    }

    @SuppressWarnings( "unchecked" )
    private ErrorWarningRecordAppender getAppender()
    {
        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( appender instanceof ErrorWarningRecordAppender )
            {
                return (ErrorWarningRecordAppender) appender;
            }
        }

        return null;
    }

    private void ensureErrorWarningAppender()
    {
        try
        {
            LogConfig logConfig = logManager.getLogConfig();

            if ( logConfig.get( RECORDER_APPENDER_KEY ) == null )
            {
                getLogger().info(
                    "Adding log4j appender '" + RECORDER_APPENDER_KEY + "=" + RECORDER_APPENDER_VALUE + "'..." );

                logConfig.put( RECORDER_APPENDER_KEY, RECORDER_APPENDER_VALUE );

                String rootLoggerValue = logConfig.get( ROOT_LOGGER_KEY );

                if ( !rootLoggerValue.contains( RECORDER_APPENDER_KEY_SHORT ) )
                {
                    logConfig.put( ROOT_LOGGER_KEY, rootLoggerValue + ", " + RECORDER_APPENDER_KEY_SHORT );
                }

                logManager.setLogConfig( logConfig );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Unable to get log4j configuration.", e );
        }
    }

    private boolean logConfigEnabled()
    {
        try
        {
            if ( logManager.getLogConfig().isEmpty() )
            {
                return false;
            }

            return true;
        }
        catch ( IOException e )
        {
            getLogger().warn( "Unable to get log4j configuration.", e );

            return false;
        }
    }
}
