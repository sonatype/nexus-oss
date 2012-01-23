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
