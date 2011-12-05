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
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthorizationEvent;
import org.sonatype.nexus.auth.ResourceInfo;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "NexusAuthorizationEventInspector" )
public class NexusAuthorizationEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
    @Requirement
    private NexusConfiguration nexusConfiguration;

    private volatile NexusAuthorizationEvent lastNexusAuthorizationEvent;

    @Override
    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof NexusAuthorizationEvent )
        {
            // We accept only some NexusAuthenticationEvent, see #isRecordedEvent()
            return isRecordedEvent( (NexusAuthorizationEvent) evt );
        }
        else
        {
            return false;
        }
    }

    @Override
    public void inspect( Event<?> evt )
    {
        final NexusAuthorizationEvent nae = (NexusAuthorizationEvent) evt;

        if ( !isRecordedEvent( nae ) )
        {
            // do nothing
            return;
        }

        lastNexusAuthorizationEvent = nae;

        final ClientInfo ai = nae.getClientInfo();
        final ResourceInfo ri = nae.getResourceInfo();

        final String msg =
            "Unable to authorize user [" + ai.getUserid() + "] for " + ri.getAction() + "(" + ri.getAccessProtocol()
                + " method \"" + ri.getAccessMethod() + "\") to " + ri.getAccessedUri() + " from IP Address "
                + ai.getRemoteIP() + ", user agent:\"" + ai.getUserAgent() + "\"";

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( msg );
        }

        AuthcAuthzEvent aae = new AuthcAuthzEvent( nae.getEventDate(), FeedRecorder.SYSTEM_AUTHZ, msg );

        final String ip = ai.getRemoteIP();

        if ( ip != null )
        {
            evt.getEventContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, ip );
        }

        getFeedRecorder().addAuthcAuthzEvent( aae );
    }

    // ==

    protected boolean isRecordedEvent( final NexusAuthorizationEvent nae )
    {
        // we record only authz failures
        if ( nae.isSuccessful() )
        {
            return false;
        }
        // we record everything except anonymous related ones
        if ( StringUtils.equals( nexusConfiguration.getAnonymousUsername(), nae.getClientInfo().getUserid() ) )
        {
            return false;
        }

        // if here, we record the event if not similar to previous one
        return !isSimilarEvent( nae );
    }

    protected boolean isSimilarEvent( final NexusAuthorizationEvent nae )
    {
        // event is similar (to previously processed one) if there was previously processed at all, the carried
        // AuthenticationItem equals to the one carried by previously processed one, and the events happened in range
        // less than 2 seconds
        if ( lastNexusAuthorizationEvent != null
            && lastNexusAuthorizationEvent.getClientInfo().equals( nae.getClientInfo() )
            && lastNexusAuthorizationEvent.getResourceInfo().equals( nae.getResourceInfo() )
            && ( System.currentTimeMillis() - lastNexusAuthorizationEvent.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }
}
