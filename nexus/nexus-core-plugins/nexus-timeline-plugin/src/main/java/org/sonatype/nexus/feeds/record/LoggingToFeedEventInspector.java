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
package org.sonatype.nexus.feeds.record;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.logging.LoggingEvent;
import org.sonatype.nexus.logging.LoggingEvent.Level;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * Listens to {@link LoggingEvent}s of ERROR/WARN level and creates corresponding {@link FeedRecorder} entries. It is an
 * asynchronous listener so Nexus is not blocked during feed entry registration.
 * 
 * @author adreghiciu@gmail.com
 */
@Component( role = EventInspector.class, hint = "LoggingToFeedEventInspector" )
public class LoggingToFeedEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{

    public boolean accepts( Event<?> evt )
    {
        return evt instanceof LoggingEvent;
    }

    public void inspect( Event<?> evt )
    {
        LoggingEvent event = (LoggingEvent) evt;

        Throwable throwable = event.getThrowable();
        String message = event.getMessage();

        if ( shouldIgnore( message, throwable ) )
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
        else
        {
            return;
        }

        if ( throwable != null )
        {
            getFeedRecorder().addErrorWarningEvent( action, message, throwable );
        }
        else
        {
            getFeedRecorder().addErrorWarningEvent( action, message );
        }

    }

    private static boolean shouldIgnore( String message, Throwable throwable )
    {
        if ( throwable != null )
        {
            String exClassName = throwable.getClass().getName();
            if ( "org.sonatype.timeline.TimelineException".equals( exClassName )
                || ( exClassName.endsWith( "EofException" ) && exClassName.contains( "jetty" ) ) )
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
