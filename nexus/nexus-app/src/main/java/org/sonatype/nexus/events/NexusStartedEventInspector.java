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
import org.sonatype.plexus.appevents.Event;

/**
 * @author juven
 */
@Component( role = EventInspector.class, hint = "NexusStartedEvent" )
public class NexusStartedEventInspector
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
        return ( evt instanceof NexusStartedEvent );
    }

    public void inspect( Event<?> evt )
    {
        if ( logConfigEnabled() )
        {
            ensureErrorWarningAppender();

            startToRecordErrorWarningLog();
        }
    }

    @SuppressWarnings( "unchecked" )
    private void startToRecordErrorWarningLog()
    {
        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( appender instanceof ErrorWarningRecordAppender )
            {
                ( (ErrorWarningRecordAppender) appender ).setFeedRecorder( feedRecorder );

                return;
            }
        }
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
