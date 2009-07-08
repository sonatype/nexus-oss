package org.sonatype.nexus.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;

/**
 * This class extends log4j, record all error/warning log
 * 
 * @author juven
 */
public class ErrorWarningRecordAppender
    extends AppenderSkeleton
{
    private FeedRecorder feedRecorder;

    @Override
    protected void append( LoggingEvent event )
    {
        if ( feedRecorder == null )
        {
            return;
        }

        String action = "";

        if ( event.getLevel().equals( Level.WARN ) )
        {
            action = ErrorWarningEvent.ACTION_WARNING;
        }
        else if ( event.getLevel().equals( Level.ERROR ) )
        {
            action = ErrorWarningEvent.ACTION_ERROR;
        }
        // TODO: record Level.FATAL ?
        else
        {
            return;
        }

        String message = (String) event.getMessage();

        if ( event.getThrowableInformation() != null )
        {
            feedRecorder.addErrorWarningEvent( action, message, event.getThrowableInformation().getThrowable() );
        }
        else
        {
            feedRecorder.addErrorWarningEvent( action, message );
        }
    }

    @Override
    public void close()
    {
        // do nothing
    }

    @Override
    public boolean requiresLayout()
    {
        return false;
    }

    public FeedRecorder getFeedRecorder()
    {
        return feedRecorder;
    }

    public void setFeedRecorder( FeedRecorder feedRecorder )
    {
        this.feedRecorder = feedRecorder;
    }

}
