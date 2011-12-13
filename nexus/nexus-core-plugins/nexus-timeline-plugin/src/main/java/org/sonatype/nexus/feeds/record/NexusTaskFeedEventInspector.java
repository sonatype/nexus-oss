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

import java.lang.reflect.Method;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.events.NexusTaskEvent;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector that creates feeds about tasks. Note: this EventInspector is
 * <em>intentionally synchronous EventInspector</em>! Reasoning is mainly to avoid unordered event arrivals (stopped
 * before started), but also, one can easily see by inspecting where these events are fired (see
 * {@link AbstractNexusTask}), that those are fired from already pooled thread (task executing thread), and not the main
 * HTTP request processing ones, hence, this event inspector being synchronous will not steal much CPU cycles from it
 * (well, will do as much to decide does it "accepts" the event, but that is negligible, and will be fixed once we start
 * using "event bus" since then it will not even be invoked).
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "NexusTaskFeedEventInspector" )
public class NexusTaskFeedEventInspector
    extends AbstractFeedRecorderEventInspector
    implements EventInspector
{
    public boolean accepts( final Event<?> evt )
    {
        return evt != null && evt instanceof NexusTaskEvent;
    }

    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        if ( evt instanceof NexusTaskEventStarted<?> )
        {
            final String action = getActionFromTask( ( (NexusTaskEventStarted<?>) evt ).getNexusTask() );
            final String message = getMessageFromTask( ( (NexusTaskEventStarted<?>) evt ).getNexusTask() );
            final SystemProcess prc = getFeedRecorder().systemProcessStarted( action, message );
            putSystemProcessFromEventContext( (NexusTaskEventStarted<?>) evt, prc );
        }
        else if ( evt instanceof NexusTaskEventStoppedDone<?> )
        {
            final SystemProcess prc =
                getSystemProcessFromEventContext( ( (NexusTaskEventStoppedDone<?>) evt ).getStartedEvent() );
            final String message = getMessageFromTask( ( (NexusTaskEventStoppedDone<?>) evt ).getNexusTask() );
            getFeedRecorder().systemProcessFinished( prc, message );
        }
        else if ( evt instanceof NexusTaskEventStoppedCanceled<?> )
        {
            final SystemProcess prc =
                getSystemProcessFromEventContext( ( (NexusTaskEventStoppedCanceled<?>) evt ).getStartedEvent() );
            final String message = getMessageFromTask( ( (NexusTaskEventStoppedCanceled<?>) evt ).getNexusTask() );
            getFeedRecorder().systemProcessCanceled( prc, message );
        }
        else if ( evt instanceof NexusTaskEventStoppedFailed<?> )
        {
            final SystemProcess prc =
                getSystemProcessFromEventContext( ( (NexusTaskEventStoppedFailed<?>) evt ).getStartedEvent() );
            getFeedRecorder().systemProcessBroken( prc, ( (NexusTaskEventStoppedFailed<?>) evt ).getFailureCause() );
        }
    }

    // ==

    protected void putSystemProcessFromEventContext( final NexusTaskEventStarted<?> evt, final SystemProcess prc )
    {
        evt.getEventContext().put( SystemProcess.class.getName(), prc );
    }

    protected SystemProcess getSystemProcessFromEventContext( final NexusTaskEventStarted<?> evt )
    {
        return (SystemProcess) evt.getEventContext().get( SystemProcess.class.getName() );
    }

    protected String getActionFromTask( final NexusTask<?> task )
    {
        if ( task instanceof AbstractNexusTask<?> )
        {
            try
            {
                final Method getActionMethod = AbstractNexusTask.class.getDeclaredMethod( "getAction" );
                getActionMethod.setAccessible( true );
                return (String) getActionMethod.invoke( task );
            }
            catch ( Exception e )
            {
                // nothing
            }
        }
        return "UNKNOWN";
    }

    protected String getMessageFromTask( final NexusTask<?> task )
    {
        if ( task instanceof AbstractNexusTask<?> )
        {
            try
            {
                final Method getMessageMethod = AbstractNexusTask.class.getDeclaredMethod( "getMessage" );
                getMessageMethod.setAccessible( true );
                return (String) getMessageMethod.invoke( task );
            }
            catch ( Exception e )
            {
                // nothing
            }
        }
        return "UNKNOWN";
    }
}
