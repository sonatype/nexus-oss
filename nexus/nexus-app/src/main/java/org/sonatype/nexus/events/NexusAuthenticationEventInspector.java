package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.auth.AuthenticationItem;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "NexusAuthenticationEventInspector" )
public class NexusAuthenticationEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private FeedRecorder feedRecorder;

    private volatile NexusAuthenticationEvent lastNexusAuthenticationEvent;

    @Override
    public boolean accepts( Event<?> evt )
    {
        // We accept only if it is NexusAuthenticationEvent but not alike previos one, see #isSimilar()
        if ( evt instanceof NexusAuthenticationEvent )
        {
            final NexusAuthenticationEvent nae = (NexusAuthenticationEvent) evt;

            if ( isSimilarEvent( nae ) )
            {
                return false;
            }
            else
            {
                return true;
            }
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

        feedRecorder.addAuthcAuthzEvent( aae );

        lastNexusAuthenticationEvent = nae;
    }

    // ==

    protected boolean isSimilarEvent( final NexusAuthenticationEvent nae )
    {
        if ( lastNexusAuthenticationEvent == null )
        {
            return false;
        }

        if ( lastNexusAuthenticationEvent.getItem().equals( nae.getItem() )
            && ( System.currentTimeMillis() - lastNexusAuthenticationEvent.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }
}
