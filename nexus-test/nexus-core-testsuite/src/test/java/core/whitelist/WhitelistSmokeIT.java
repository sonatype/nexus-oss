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
package core.whitelist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.subsystem.whitelist.DiscoveryConfiguration;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;
import org.sonatype.sisu.litmus.testsupport.group.Smoke;

/**
 * Simple smoke IT for Whitelist REST being responsive and is reporting the expected statuses.
 *
 * @author cstamas
 */
@Category( Smoke.class )
public class WhitelistSmokeIT
    extends WhitelistITSupport
{
    public WhitelistSmokeIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void smokeIt()
        throws Exception
    {
        waitForWLDiscoveryOutcome( "central" );

        // See https://issues.sonatype.org/browse/CENTRAL-515
        // As of Feb 15 2013 Central has prefix file published,
        // and it means that Nexus on boot immediately gets WL lists populated
        // for Central, and hence, for Public group also.

        {
            // public
            final Status publicStatus = whitelist().getWhitelistStatus( "public" );
            // public cannot be published, since Central is member and scrape in underway
            assertThat( publicStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        }

        {
            // releases
            final Status releasesStatus = whitelist().getWhitelistStatus( "releases" );
            // releases is published, is hosted
            assertThat( releasesStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
            // is hosted, discovery status must be null
            assertThat( releasesStatus.getDiscoveryStatus(), is( nullValue() ) );
        }

        {
            // central
            final Status centralStatus = whitelist().getWhitelistStatus( "central" );
            // central is published, it got prefix file
            assertThat( centralStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
            // is proxy, discovery status must not be null
            assertThat( centralStatus.getDiscoveryStatus(), is( notNullValue() ) );
            // succeeded, since not scrapin anymore but getting prefix file
            assertThat( centralStatus.getDiscoveryStatus().getDiscoveryLastStatus(), equalTo( Outcome.SUCCEEDED ) );
        }

        // get configuration for central and check for sane values (actually, they should be defaults).
        {
            final DiscoveryConfiguration centralConfiguration = whitelist().getDiscoveryConfigurationFor( "central" );
            assertThat( centralConfiguration, is( notNullValue() ) );
            assertThat( centralConfiguration.isEnabled(), equalTo( true ) );
            assertThat( centralConfiguration.getIntervalHours(), equalTo( 24 ) );
            // checked ok, set interval to 12h
            centralConfiguration.setIntervalHours( 12 );
            whitelist().setDiscoveryConfigurationFor( "central", centralConfiguration );
        }
        {
            final DiscoveryConfiguration centralConfiguration = whitelist().getDiscoveryConfigurationFor( "central" );
            assertThat( centralConfiguration, is( notNullValue() ) );
            assertThat( centralConfiguration.isEnabled(), equalTo( true ) );
            assertThat( centralConfiguration.getIntervalHours(), equalTo( 12 ) );
        }

        // for non proxy repositories config is undefined, response should be 400
        try
        {
            final DiscoveryConfiguration releasesConfiguration = whitelist().getDiscoveryConfigurationFor( "releases" );
            assertThat( "Request should fail with HTTP 400 and being converted to NexusClientBadRequestException",
                false );
        }
        catch ( NexusClientBadRequestException e )
        {
            // good
        }
    }
}
