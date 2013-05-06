/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.error.reporting;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Joiner;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.events.GlobalHttpProxySettingsChangedEvent;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.plexus.appevents.Event;

/**
 * This inspector sets system properties according to the Nexus proxy settings.
 * Only username/password authentication is supported.
 */
@Named
@Singleton
public class SetProxyPropertiesInspector
    extends AbstractLoggingComponent
    implements EventInspector
{

    private final GlobalHttpProxySettings globalHttpProxySettings;

    @Inject
    public SetProxyPropertiesInspector( final GlobalHttpProxySettings globalHttpProxySettings )
    {
        this.globalHttpProxySettings = checkNotNull( globalHttpProxySettings );
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return evt instanceof GlobalHttpProxySettingsChangedEvent
            || evt instanceof NexusStartedEvent;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        if ( globalHttpProxySettings.isEnabled() )
        {
            String username = null;
            String password = null;

            final RemoteAuthenticationSettings authentication = globalHttpProxySettings.getProxyAuthentication();

            if ( authentication != null
                && UsernamePasswordRemoteAuthenticationSettings.class.isAssignableFrom( authentication.getClass() ) )
            {
                username = ( (UsernamePasswordRemoteAuthenticationSettings) authentication ).getUsername();
                password = ( (UsernamePasswordRemoteAuthenticationSettings) authentication ).getPassword();
            }

            final String hostname = globalHttpProxySettings.getHostname();
            final int port = globalHttpProxySettings.getPort();
            final Set<String> nonProxyHosts = globalHttpProxySettings.getNonProxyHosts();

            getLogger().debug(
                "Configure proxy using global http proxy settings: hostname={}, port={}, username={}, nonProxyHosts={}",
                new Object[]{ hostname, port, username, nonProxyHosts }
            );

            setProperties( true, hostname, port, username, password, nonProxyHosts );
        }
        else
        {
            getLogger().debug( "No global http proxy settings. Resetting proxy properties." );
            setProperties( false, null, -1, null, null, null );
        }
    }

    private void setProperties( final boolean proxiesEnabled, final String hostname, final int port,
                                final String username, final String password,
                                final Set<String> nonProxyHosts )
    {
        Properties properties = System.getProperties();
        setProperties( proxiesEnabled, hostname, port, username, password, nonProxyHosts, properties, "http." );
        setProperties( proxiesEnabled, hostname, port, username, password, nonProxyHosts, properties, "https." );
    }

    private void setProperties( final boolean proxiesEnabled, final String hostname, final int port,
                                final String username, final String password, final Set<String> nonProxyHosts,
                                final Properties properties, final String prefix )
    {
        if ( !proxiesEnabled || hostname == null || hostname.equals( "" ) )
        {
            properties.remove( prefix + "proxySet" );
            properties.remove( prefix + "proxyHost" );
            properties.remove( prefix + "proxyPort" );
            properties.remove( prefix + "nonProxyHosts" );
            properties.remove( prefix + "proxyUser" );
            properties.remove( prefix + "proxyUserName" );
            properties.remove( prefix + "proxyPassword" );
        }
        else
        {
            properties.put( prefix + "proxySet", "true" );
            properties.put( prefix + "proxyHost", hostname );
            if ( port == -1 )
            {
                properties.remove( prefix + "proxyPort" );
            }
            else
            {
                properties.put( prefix + "proxyPort", String.valueOf( port ) );
            }
            properties.put( prefix + "nonProxyHosts", Joiner.on( "|" ).join( nonProxyHosts ) );

            if ( username == null || password == null || username.length() == 0 || password.length() == 0 )
            {
                properties.remove( prefix + "proxyUser" );
                properties.remove( prefix + "proxyUserName" );
                properties.remove( prefix + "proxyPassword" );
            }
            else
            {
                properties.put( prefix + "proxyUser", username );
                properties.put( prefix + "proxyUserName", username );
                properties.put( prefix + "proxyPassword", password );
            }
        }
    }

}
