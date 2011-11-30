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
package org.sonatype.nexus.timeline;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginDeactivatedEvent;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector that activates the search for "real" timeline when Nexus is initialized (plugins are loaded).
 *
 * @author: cstamas
 * @since 1.10.0
 */
@Component( role = EventInspector.class, hint = "TimelinePluginEventInspector" )
public class TimelinePluginEventInspector
    extends AbstractEventInspector
    implements EventInspector
{

    @Requirement
    private NexusTimeline nexusTimeline;

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return evt instanceof NexusInitializedEvent;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( nexusTimeline instanceof RedirectingTimeline )
        {
            ( (RedirectingTimeline) nexusTimeline ).tryToActivateTimeline();
        }
    }
}
