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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;
import org.sonatype.sisu.litmus.testsupport.group.Slow;

import com.google.common.io.Closeables;

/**
 * This IT will just boot up nexus, wait for WL to be discovered (only for Central, with default config), and then
 * download the central prefix file and sanity check it.
 * <p>
 * This is a slow test. Here, we check that on boot of a "virgin" Nexus, Central is being "remotely discovered" (today
 * -- 2013. 02. 08 -- scraped, but once prefix file published, it will be used instead of lengthy scrape). Warning, this
 * IT scraping Central for real! On my Mac (cstamas), this IT runs for 210seconds, hence, is marked as Slow.
 * <P>
 * Not anymore, as prefix file is deployed to central.
 * 
 * @author cstamas
 */
@Category( Slow.class )
public class WhitelistInitialSanityIT
    extends WhitelistITSupport
{
    // we will timeout after 15 minutes, just as a safety net
    @Rule
    public Timeout timeout = new Timeout( 900000 );

    public WhitelistInitialSanityIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        // we lessen the throttling as otherwise this test would run even longer
        return super.configureNexus( configuration ).setSystemProperty(
            "org.sonatype.nexus.proxy.maven.wl.internal.scrape.Scraper.pageSleepTimeMillis", "50" );
    }

    @Test
    public void initialScrape()
        throws Exception
    {
        waitForWLDiscoveryOutcome( "central" );

        {
            // public
            Status publicStatus = whitelist().getWhitelistStatus( "public" );
            // public cannot be published, since Central is member and scrape will take longer
            assertThat( publicStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        }

        // central
        Status centralStatus = whitelist().getWhitelistStatus( "central" );
        // central is not published, is being scraped
        assertThat( centralStatus.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
        // central gave us URL of the published whitelist too
        assertThat( centralStatus.getPublishedUrl(), is( notNullValue() ) );
        // is proxy, discovery status must not be null
        assertThat( centralStatus.getDiscoveryStatus(), is( notNullValue() ) );
        // undecided, since still scraping and this is the first run of remote discovery
        assertThat( centralStatus.getDiscoveryStatus().getDiscoveryLastStatus(), equalTo( Outcome.SUCCEEDED ) );

        // let's verify it
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet get = new HttpGet( centralStatus.getPublishedUrl() );
        final HttpResponse httpResponse = httpClient.execute( get );
        assertThat( httpResponse.getStatusLine().getStatusCode(), equalTo( 200 ) );
        assertThat( httpResponse.getEntity(), is( notNullValue() ) );
        final InputStream entityStream = httpResponse.getEntity().getContent();
        try
        {
            final LineNumberReader lnr = new LineNumberReader( new InputStreamReader( entityStream, "UTF-8" ) );
            boolean hasAbbot = false;
            boolean hasComApple = false;
            boolean hasOrgSonatype = false;
            String currentLine = lnr.readLine();
            while ( currentLine != null )
            {
                hasAbbot = hasAbbot || "/abbot".equals( currentLine );
                hasComApple = hasComApple || "/com/apple".equals( currentLine );
                hasOrgSonatype = hasOrgSonatype || "/org/sonatype".equals( currentLine );
                currentLine = lnr.readLine();
            }

            // check is this what we think should be
            assertThat( "Line /abbot is missing?", hasAbbot );
            assertThat( "Line /com/apple is missing?", hasComApple );
            assertThat( "Line /org/sonatype is missing?", hasOrgSonatype );

            // count lines
            lnr.skip( Long.MAX_VALUE );
            // 2013. 02. 08. Today, Nexus scraped prefix file with 5517 lines (depth=2)
            // So, safely assuming the prefix file MUST HAVE more than 5k lines
            // Naturally, if depth changes, making it lesser, this might fail.
            // 2012. 02. 14. Today the prefix file is deployed to Central, no more scraping
            // The prefix file has around 1600 entries.
            assertThat( lnr.getLineNumber() + 1, is( greaterThanOrEqualTo( 1000 ) ) );
        }
        finally
        {
            Closeables.closeQuietly( entityStream );
        }
    }
}
