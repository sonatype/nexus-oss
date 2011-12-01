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
package org.sonatype.nexus.rest.feeds;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Event inspector that "nudges" the feed list resource to use Timeline based feeds when possible.
 * 
 * @author cstamas
 * @since 1.10.0
 */
@Component( role = EventInspector.class, hint = "FeedsListEventInspector" )
public class FeedsListEventInspector
    extends AbstractEventInspector
    implements EventInspector, AsynchronousEventInspector
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Override
    public boolean accepts( Event<?> evt )
    {
        // we do this on started event, since this is actually user experience/UI related trick
        return evt instanceof NexusStartedEvent;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        try
        {
            final PlexusResource feedsList = plexusContainer.lookup( PlexusResource.class, "feedList" );

            if ( feedsList instanceof FeedsListPlexusResource )
            {
                final PlexusResource delegate =
                    (AbstractNexusPlexusResource) plexusContainer.lookup( PlexusResource.class, "TimelineFeedList" );

                if ( delegate != null )
                {
                    ( (FeedsListPlexusResource) feedsList ).setDelegate( delegate );

                    getLogger().info( "Timeline based feeds are present and enabled." );

                    return;
                }

                getLogger().info( "Tried to enable Timeline based feeds but failed, fallback to NOOP Feeds." );
            }
        }
        catch ( Exception e )
        {
            // huh
            getLogger().debug( "Unexpected exception!", e );
        }
    }
}
