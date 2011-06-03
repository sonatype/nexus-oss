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
package org.sonatype.nexus.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.timeline.TimelineException;

/**
 * This class extends log4j, record all error/warning log
 * 
 * @author juven xu
 */
public class ErrorWarningRecordAppender
    extends AppenderSkeleton
{
    private FeedRecorder feedRecorder;

    @Override
    protected void append( LoggingEvent event )
    {
        // we get local instance, just in case another thread comes in and closes the appender
        // (thus nullifying the feedRecorder object) before we attempt to add to it below
        FeedRecorder localRecorder = feedRecorder;
        
        if ( localRecorder == null )
        {
            return;
        }

        // hack to prevent infinite loop
        if ( event.getThrowableInformation() != null
            && event.getThrowableInformation().getThrowable() instanceof TimelineException )
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

        if ( !shouldIgnore( message, event ) )
        {
            if ( event.getThrowableInformation() != null )
            {
                localRecorder.addErrorWarningEvent( action, message, event.getThrowableInformation().getThrowable() );
            }
            else
            {
                localRecorder.addErrorWarningEvent( action, message );
            }
        }
    }

    @Override
    public void close()
    {
        feedRecorder = null;
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

    protected boolean shouldIgnore( String message, LoggingEvent event )
    {
        if ( event != null && event.getThrowableInformation() != null
            && event.getThrowableInformation().getThrowable() != null )
        {
            if ( "org.mortbay.jetty.EofException".equals( event.getThrowableInformation().getThrowable().getClass().getName() ) )
            {
                return true;
            }
        }

        if ( message != null )
        {
            // we don't want to notify in feed about client exceptions closing, this will just annoy everyone
            if ( message.contains( "An exception occured writing the response entity" )
                || message.contains( "Error while handling an HTTP server call" ) )
            {
                return true;
            }
        }

        return false;

    }

}
