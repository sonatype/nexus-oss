package org.sonatype.nexus.events;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.log.ErrorWarningRecordAppender;
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

    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof NexusStartedEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( Event<?> evt )
    {
        startToRecordErrorWarningLog();
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
}
