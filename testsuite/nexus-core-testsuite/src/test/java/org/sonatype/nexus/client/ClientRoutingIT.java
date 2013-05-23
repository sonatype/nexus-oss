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
package org.sonatype.nexus.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.routing.DiscoveryConfiguration;
import org.sonatype.nexus.client.core.subsystem.routing.Routing;
import org.sonatype.nexus.client.core.subsystem.routing.Status;
import org.sonatype.nexus.client.core.subsystem.routing.Status.Outcome;

/**
 * Will not work until proxy404 merged into master, AND at least one CI build/deploys of that master, as it seems Sisu
 * Maven Bridge will download the "latest" from remote, not use the build from branch.
 * 
 * @author cstamas
 */
public class ClientRoutingIT
    extends ClientITSupport
{

    public ClientRoutingIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    private Routing routing()
    {
        return client().getSubsystem( Routing.class );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void getNonExistentStatus()
    {
        final Status status = routing().getStatus( "no-such-repo-id" );
    }

    @Test
    public void getReleaseStatus()
    {
        final Status status = routing().getStatus( "releases" );
        assertThat( status, is( not( nullValue() ) ) );
        assertThat( status.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        assertThat( status.getPublishedMessage(), is( notNullValue() ) );
        assertThat( status.getPublishedTimestamp(), greaterThan( 0L ) );
        assertThat( status.getPublishedUrl(), is( notNullValue() ) );
    }

    @Test
    public void getSnapshotsStatus()
    {
        final Status status = routing().getStatus( "snapshots" );
        assertThat( status, is( not( nullValue() ) ) );
        assertThat( status.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        assertThat( status.getPublishedMessage(), is( notNullValue() ) );
        assertThat( status.getPublishedTimestamp(), greaterThan( 0L ) );
        assertThat( status.getPublishedUrl(), is( notNullValue() ) );
    }

    @Test( expected = NexusClientBadRequestException.class )
    public void getCentralM1Status()
    {
        final Status status = routing().getStatus( "central-m1" );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void getNonExistentConfig()
    {
        final DiscoveryConfiguration config = routing().getDiscoveryConfigurationFor( "no-such-repo-id" );
    }

    @Test
    public void getCentralDefaultConfig()
    {
        final DiscoveryConfiguration config = routing().getDiscoveryConfigurationFor( "central" );
        assertThat( config, is( notNullValue() ) );
        assertThat( config.isEnabled(), is( true ) );
        assertThat( config.getIntervalHours(), is( 24 ) );
    }

    @Test
    public void modifyDiscoveryConfig()
    {
        {
            final DiscoveryConfiguration config = routing().getDiscoveryConfigurationFor( "central" );
            config.setEnabled( false );
            config.setIntervalHours( 12 );
            routing().setDiscoveryConfigurationFor( "central", config );
        }
        {
            final DiscoveryConfiguration config = routing().getDiscoveryConfigurationFor( "central" );
            assertThat( config.isEnabled(), is( false ) );
            assertThat( config.getIntervalHours(), is( 12 ) );
        }
    }

    @Test
    public void updateReleases()
    {
        routing().updatePrefixFile( "releases" );
    }

    @Test
    public void updateSnapshots()
    {
        routing().updatePrefixFile( "snapshots" );
    }

    @Test
    public void updateCentral()
    {
        routing().updatePrefixFile( "central" );
    }

    @Test( expected = NexusClientBadRequestException.class )
    public void updateCentralM1()
    {
        routing().updatePrefixFile( "central-m1" );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void updateNonExistent()
    {
        routing().updatePrefixFile( "no-such-repo-id" );
    }
}
