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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.auth.AuthenticationItem;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "NexusAuthenticationEventInspector" )
public class NexusAuthenticationEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
    private volatile NexusAuthenticationEvent lastNexusAuthenticationEvent;

    @Override
    public boolean accepts( Event<?> evt )
    {
        // We accept only if it is NexusAuthenticationEvent but not alike previous one, see #isSimilar()
        if ( evt instanceof NexusAuthenticationEvent )
        {
            final NexusAuthenticationEvent nae = (NexusAuthenticationEvent) evt;

            return !isSimilarEvent( nae );
        }
        else
        {
            return false;
        }
    }

    @Override
    public void inspect( Event<?> evt )
    {
        final NexusAuthenticationEvent nae = (NexusAuthenticationEvent) evt;

        if ( isSimilarEvent( nae ) )
        {
            // do nothing
            return;
        }

        lastNexusAuthenticationEvent = nae;

        final AuthenticationItem ai = nae.getItem();

        final String msg =
            String.format( "%s user [%s] from IP address %s", ( ai.isSuccess() ? "Sucessfully authenticated"
                : "Unable to authenticate" ), ai.getUserid(), StringUtils.defaultString( ai.getRemoteIP(), "[unknown]" ) );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( msg );
        }

        AuthcAuthzEvent aae = new AuthcAuthzEvent( nae.getEventDate(), FeedRecorder.SYSTEM_AUTHC, msg );

        final String ip = ai.getRemoteIP();

        if ( ip != null )
        {
            evt.getEventContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, ip );
        }

        getFeedRecorder().addAuthcAuthzEvent( aae );
    }

    // ==

    protected boolean isSimilarEvent( final NexusAuthenticationEvent nae )
    {
        // event is similar (to previously processed one) if there was previously processed at all, the carried
        // AuthenticationItem equals to the one carried by previously processed one, and the events happened in range
        // less than 2 seconds
        if ( lastNexusAuthenticationEvent != null && lastNexusAuthenticationEvent.getItem().equals( nae.getItem() )
            && ( System.currentTimeMillis() - lastNexusAuthenticationEvent.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }
}
