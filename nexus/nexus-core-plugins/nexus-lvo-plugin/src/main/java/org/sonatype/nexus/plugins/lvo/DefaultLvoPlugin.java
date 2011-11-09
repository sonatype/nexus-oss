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
package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;
import java.util.Map;

import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.lvo.config.LvoPluginConfiguration;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

@Component( role = LvoPlugin.class )
public class DefaultLvoPlugin
    extends AbstractLoggingComponent
    implements LvoPlugin
{
    @Requirement
    private LvoPluginConfiguration lvoPluginConfiguration;

    @Requirement( role = DiscoveryStrategy.class )
    private Map<String, DiscoveryStrategy> strategies;

    public DiscoveryResponse getLatestVersionForKey( String key )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException
    {
        if ( lvoPluginConfiguration.isEnabled() )
        {
            CLvoKey info = lvoPluginConfiguration.getLvoKey( key );

            String strategyId = info.getStrategy();

            if ( StringUtils.isEmpty( strategyId ) )
            {
                // default it
                strategyId = "index";
            }

            if ( strategies.containsKey( strategyId ) )
            {
                DiscoveryStrategy strategy = strategies.get( strategyId );

                DiscoveryRequest req = new DiscoveryRequest( key, info );

                return strategy.discoverLatestVersion( req );
            }
            else
            {
                throw new NoSuchStrategyException( info.getStrategy() );
            }
        }
        else
        {
            return getDisabledResponse();
        }
    }

    public DiscoveryResponse queryLatestVersionForKey( String key, String v )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException
    {
        if ( lvoPluginConfiguration.isEnabled() )
        {
            DiscoveryResponse lv = getLatestVersionForKey( key );

            if ( !lv.isSuccessful() )
            {
                // nothing to compare to
                return lv;
            }

            // compare the two versions

            ArtifactInfo ca = new ArtifactInfo();
            ca.groupId = "dummy";
            ca.artifactId = "dummy";
            ca.version = "[" + v + "]";

            ArtifactInfo la = new ArtifactInfo();
            la.groupId = "dummy";
            la.artifactId = "dummy";
            la.version = "[" + lv.getVersion() + "]";

            if ( ArtifactInfo.VERSION_COMPARATOR.compare( la, ca ) >= 0 )
            {
                lv.getResponse().clear();

                lv.setSuccessful( true );
            }

            return lv;
        }
        else
        {
            return getDisabledResponse();
        }
    }

    protected DiscoveryResponse getDisabledResponse()
    {
        DiscoveryResponse response = new DiscoveryResponse( null );

        response.setSuccessful( true );

        return response;
    }
}
