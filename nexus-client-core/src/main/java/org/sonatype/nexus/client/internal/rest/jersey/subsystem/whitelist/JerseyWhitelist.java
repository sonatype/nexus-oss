package org.sonatype.nexus.client.internal.rest.jersey.subsystem.whitelist;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.whitelist.DiscoveryConfiguration;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.DiscoveryStatus;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;
import org.sonatype.nexus.client.core.subsystem.whitelist.Whitelist;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.WLConfigMessage;
import org.sonatype.nexus.rest.model.WLConfigMessageWrapper;
import org.sonatype.nexus.rest.model.WLStatusMessage;
import org.sonatype.nexus.rest.model.WLStatusMessageWrapper;

import com.google.common.base.Throwables;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Jersey based {@link Whitelist} implementation.
 * 
 * @author cstamas
 * @since 2.4
 */
public class JerseyWhitelist
    extends SubsystemSupport<JerseyNexusClient>
    implements Whitelist
{

    /**
     * Constructor.
     * 
     * @param nexusClient
     */
    public JerseyWhitelist( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public Status getWhitelistStatus( final String mavenRepositoryId )
    {
        try
        {
            final WLStatusMessage message =
                getNexusClient().serviceResource( wlPath( mavenRepositoryId ) ).get( WLStatusMessageWrapper.class ).getData();

            final DiscoveryStatus discoveryStatus;
            if ( message.getDiscovery() == null )
            {
                // not a proxy
                discoveryStatus = null;
            }
            else
            {
                final Outcome discoveryOutcome = Outcome.values()[message.getDiscovery().getDiscoveryLastStatus() + 1];
                discoveryStatus =
                    new DiscoveryStatus( message.getDiscovery().isDiscoveryEnabled(),
                        message.getDiscovery().getDiscoveryIntervalHours(), discoveryOutcome,
                        message.getDiscovery().getDiscoveryLastStrategy(),
                        message.getDiscovery().getDiscoveryLastMessage(),
                        message.getDiscovery().getDiscoveryLastRunTimestamp() );
            }

            final Outcome publishOutcome = Outcome.values()[message.getPublishedStatus() + 1];
            return new Status( publishOutcome, message.getPublishedMessage(), message.getPublishedTimestamp(),
                message.getPublishedUrl(), discoveryStatus );
        }
        catch ( UniformInterfaceException e )
        {
            throw getNexusClient().convert( e );
        }
        catch ( ClientHandlerException e )
        {
            throw getNexusClient().convert( e );
        }
    }

    @Override
    public void updateWhitelist( final String mavenProxyRepositoryId )
        throws IllegalArgumentException, NexusClientNotFoundException
    {
        try
        {
            getNexusClient().serviceResource( wlPath( mavenProxyRepositoryId ) ).delete();
        }
        catch ( UniformInterfaceException e )
        {
            throw getNexusClient().convert( e );
        }
        catch ( ClientHandlerException e )
        {
            throw getNexusClient().convert( e );
        }
    }

    @Override
    public DiscoveryConfiguration getDiscoveryConfigurationFor( final String mavenProxyRepositoryId )
        throws IllegalArgumentException, NexusClientNotFoundException
    {
        try
        {
            final WLConfigMessage message =
                getNexusClient().serviceResource( wlConfigPath( mavenProxyRepositoryId ) ).get(
                    WLConfigMessageWrapper.class ).getData();
            return new DiscoveryConfiguration( message.isDiscoveryEnabled(), message.getDiscoveryIntervalHours() );
        }
        catch ( UniformInterfaceException e )
        {
            throw getNexusClient().convert( e );
        }
        catch ( ClientHandlerException e )
        {
            throw getNexusClient().convert( e );
        }
    }

    @Override
    public void setDiscoveryConfigurationFor( final String mavenProxyRepositoryId,
                                              final DiscoveryConfiguration configuration )
        throws IllegalArgumentException, NexusClientNotFoundException
    {
        try
        {
            final WLConfigMessage message = new WLConfigMessage();
            message.setDiscoveryEnabled( configuration.isEnabled() );
            message.setDiscoveryIntervalHours( configuration.getIntervalHours() );
            final WLConfigMessageWrapper wrapper = new WLConfigMessageWrapper();
            wrapper.setData( message );
            getNexusClient().serviceResource( wlConfigPath( mavenProxyRepositoryId ) ).put( wrapper );
        }
        catch ( UniformInterfaceException e )
        {
            throw getNexusClient().convert( e );
        }
        catch ( ClientHandlerException e )
        {
            throw getNexusClient().convert( e );
        }
    }

    // ==

    static String wlPath( final String mavenRepositoryId )
    {
        try
        {
            return "repositories/" + URLEncoder.encode( mavenRepositoryId, "UTF-8" ) + "/wl";
        }
        catch ( UnsupportedEncodingException e )
        {
            throw Throwables.propagate( e );
        }
    }

    static String wlConfigPath( final String mavenRepositoryId )
    {
        return wlPath( mavenRepositoryId ) + "/config";
    }
}
