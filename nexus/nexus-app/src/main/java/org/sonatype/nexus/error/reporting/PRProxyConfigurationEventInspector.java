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
package org.sonatype.nexus.error.reporting;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.jira.AttachmentHandlerConfiguration;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.events.GlobalHttpProxySettingsChangedEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.plexus.appevents.Event;

/**
 * Act on startup and proxy configuration changes to save proxy configuration to {@link AttachmentHandlerConfiguration}.
 */
@Named
public class PRProxyConfigurationEventInspector
    implements EventInspector
{

    private final Logger logger = LoggerFactory.getLogger( PRProxyConfigurationEventInspector.class );

    private final AttachmentHandlerConfiguration remoteCfg;

    private final GlobalHttpProxySettings proxySettings;

    @Inject
    public PRProxyConfigurationEventInspector( AttachmentHandlerConfiguration remoteCfg, GlobalHttpProxySettings proxySettings )
    {
        this.remoteCfg = remoteCfg;
        this.proxySettings = proxySettings;
    }

    @Override
    public boolean accepts( final Event<?> event )
    {
        return event instanceof GlobalHttpProxySettingsChangedEvent || event instanceof NexusStartedEvent;
    }

    @Override
    public void inspect( final Event<?> event )
    {
        if ( proxySettings.isEnabled() )
        {
            final RemoteAuthenticationSettings proxyAuthentication = proxySettings.getProxyAuthentication();
            if ( proxyAuthentication instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                final UsernamePasswordRemoteAuthenticationSettings auth =
                    (UsernamePasswordRemoteAuthenticationSettings) proxyAuthentication;

                remoteCfg.setProxyHost( proxySettings.getHostname() );
                remoteCfg.setProxyPort( proxySettings.getPort() );
                remoteCfg.setNonProxyHosts( proxySettings.getNonProxyHosts() );
                remoteCfg.setProxyPrincipal( auth.getUsername() );
                remoteCfg.setProxyPassword( auth.getPassword() );
            }
            else
            {
                logger.info( "Proxy type unsupported for problem reporting." );
            }
        }
    }
}
