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
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * Boot listening event inspector. This one is intentionally not async, to mark exact time stamps of Nexus important
 * events: when it booted and when shutdown was commenced.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "NexusBootEventInspector" )
public class NexusBootEventInspector
    extends AbstractFeedRecorderEventInspector
{
    @Requirement
    private ApplicationStatusSource applicationStatusSource;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt != null && ( evt instanceof NexusStartedEvent || evt instanceof NexusStoppedEvent );
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof NexusStartedEvent )
        {
            getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Started Nexus (version "
                + applicationStatusSource.getSystemStatus().getVersion() + " "
                + applicationStatusSource.getSystemStatus().getEditionShort() + ")" );
        }
        else if ( evt instanceof NexusStoppedEvent )
        {
            getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_BOOT_ACTION, "Stopping Nexus (version "
                + applicationStatusSource.getSystemStatus().getVersion() + " "
                + applicationStatusSource.getSystemStatus().getEditionShort() + ")" );
        }
    }

}
