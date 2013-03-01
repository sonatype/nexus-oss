/*
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
package org.sonatype.nexus.client.testsuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.whitelist.DiscoveryConfiguration;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;
import org.sonatype.nexus.client.core.subsystem.whitelist.Whitelist;

/**
 * Will not work until proxy404 merged into master, AND at least one CI build/deploys of that master, as it seems Sisu
 * Maven Bridge will download the "latest" from remote, not use the build from branch.
 * 
 * @author cstamas
 */
public class WhitelistIT
    extends NexusClientITSupport
{

    public WhitelistIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    private Whitelist whitelist()
    {
        return client().getSubsystem( Whitelist.class );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void getNonExistentStatus()
    {
        final Status status = whitelist().getWhitelistStatus( "no-such-repo-id" );
    }

    @Test
    public void getReleaseStatus()
    {
        final Status status = whitelist().getWhitelistStatus( "releases" );
        assertThat( status, is( not( nullValue() ) ) );
        assertThat( status.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        assertThat( status.getPublishedMessage(), is( notNullValue() ) );
        assertThat( status.getPublishedTimestamp(), greaterThan( 0L ) );
        assertThat( status.getPublishedUrl(), is( notNullValue() ) );
    }

    @Test
    public void getSnapshotsStatus()
    {
        final Status status = whitelist().getWhitelistStatus( "snapshots" );
        assertThat( status, is( not( nullValue() ) ) );
        assertThat( status.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        assertThat( status.getPublishedMessage(), is( notNullValue() ) );
        assertThat( status.getPublishedTimestamp(), greaterThan( 0L ) );
        assertThat( status.getPublishedUrl(), is( notNullValue() ) );
    }

    @Test( expected = NexusClientBadRequestException.class )
    public void getCentralM1Status()
    {
        final Status status = whitelist().getWhitelistStatus( "central-m1" );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void getNonExistentConfig()
    {
        final DiscoveryConfiguration config = whitelist().getDiscoveryConfigurationFor( "no-such-repo-id" );
    }

    @Test
    public void getCentralDefaultConfig()
    {
        final DiscoveryConfiguration config = whitelist().getDiscoveryConfigurationFor( "central" );
        assertThat( config, is( notNullValue() ) );
        assertThat( config.isEnabled(), is( true ) );
        assertThat( config.getIntervalHours(), is( 24 ) );
    }

    @Test
    public void modifyDiscoveryConfig()
    {
        {
            final DiscoveryConfiguration config = whitelist().getDiscoveryConfigurationFor( "central" );
            config.setEnabled( false );
            config.setIntervalHours( 12 );
            whitelist().setDiscoveryConfigurationFor( "central", config );
        }
        {
            final DiscoveryConfiguration config = whitelist().getDiscoveryConfigurationFor( "central" );
            assertThat( config.isEnabled(), is( false ) );
            assertThat( config.getIntervalHours(), is( 12 ) );
        }
    }

    @Test
    public void updateReleases()
    {
        whitelist().updateWhitelist( "releases" );
    }

    @Test
    public void updateSnapshots()
    {
        whitelist().updateWhitelist( "snapshots" );
    }

    @Test
    public void updateCentral()
    {
        whitelist().updateWhitelist( "central" );
    }

    @Test( expected = NexusClientBadRequestException.class )
    public void updateCentralM1()
    {
        whitelist().updateWhitelist( "central-m1" );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void updateNonExistent()
    {
        whitelist().updateWhitelist( "no-such-repo-id" );
    }
}
