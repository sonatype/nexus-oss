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
package org.sonatype.nexus.testsuite.nexus5533whitelist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
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
        // public
        final Status publicStatus = whitelist().getWhitelistStatus( "public" );
        // public cannot be published, since Central is member and scrape in underway
        assertThat( publicStatus.getPublishedStatus(), equalTo( Outcome.FAILED ) );

        // releases
        final Status releasesStatus = whitelist().getWhitelistStatus( "releases" );
        // central is not published, is being scraped
        assertThat( releasesStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        // is hosted, discovery status must be null
        assertThat( releasesStatus.getDiscoveryStatus(), is( nullValue() ) );

        // central
        final Status centralStatus = whitelist().getWhitelistStatus( "central" );
        // central is not published, is being scraped
        assertThat( centralStatus.getPublishedStatus(), equalTo( Outcome.FAILED ) );
        // is proxy, discovery status must not be null
        assertThat( centralStatus.getDiscoveryStatus(), is( notNullValue() ) );
        // undecided, since still scraping and this is the first run of remote discovery
        assertThat( centralStatus.getDiscoveryStatus().getDiscoveryLastStatus(), equalTo( Outcome.UNDECIDED ) );
    }
}
