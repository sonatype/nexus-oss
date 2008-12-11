/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus526;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FeedUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

/**
 * Tests for deployment entries in feeds.
 */
public class Nexus526FeedsTests
    extends AbstractNexusIntegrationTest
{

    private Gav gav;

    public Nexus526FeedsTests()
    {
        super( "nexus-test-harness-repo" );
        this.gav =
            new Gav( this.getTestId(), "artifact1", "1.0.0", null, "jar", 0, new Date().getTime(), "Artifact 1", false,
                     false, null, false, null );
    }

    

    private void validateArtifactInFeedEntries( List<SyndEntry> entries, Gav gav, String... extensions )
        throws FileNotFoundException
    {

        List<String> links = new ArrayList<String>();

        for ( SyndEntry entry : entries )
        {

            // check if the title contains the groupid, artifactid, and version
            String title = entry.getTitle();

            Assert.assertTrue( "Feed title does not contain the groupId. Title was: " + title,
                               title.contains( gav.getGroupId() ) );
            Assert.assertTrue( "Feed title does not contain the artifactId. Title was: " + title,
                               title.contains( gav.getArtifactId() ) );
            Assert.assertTrue( "Feed title does not contain the version. Title was: " + title,
                               title.contains( gav.getVersion() ) );

            // check link
            String link = entry.getLink();
            Assert.assertNotNull( "Feed title link was null", link );

            // http://
            //localhost:8087/nexus/content/repositories/nexus-test-harness-repo/nexus526/artifact1/1.0.0/artifact1-1.0.0
            // .pom

            links.add( link );
        }

        for ( String extention : extensions )
        {
            String expectedUrl =
                this.getBaseNexusUrl() + "content/repositories/" + this.getTestRepositoryId() + "/"
                    + this.getRelitiveArtifactPath( gav );
            expectedUrl = expectedUrl.replaceAll( gav.getExtension() + "$", extention );
            Assert.assertTrue( "The feed link was wrong", links.contains( expectedUrl ) );
        }

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentlyDeployedFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = FeedUtil.getFeed( "recentlyDeployed" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

        // we just want the first 2 because this test only deployed 2 artifacts... although we should beef up this tests....
        List<SyndEntry> testEntries = new ArrayList<SyndEntry>();
        testEntries.add( entries.get( 0 ) );
        testEntries.add( entries.get( 1 ) );
        
        this.validateArtifactInFeedEntries( testEntries, gav, "pom", "jar" );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentChangesFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = FeedUtil.getFeed( "recentChanges" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

     // we just want the first 2 because this test only deployed 2 artifacts... although we should beef up this tests....
        List<SyndEntry> testEntries = new ArrayList<SyndEntry>();
        testEntries.add( entries.get( 0 ) );
        testEntries.add( entries.get( 1 ) );
        
        this.validateArtifactInFeedEntries( testEntries, gav, "pom", "jar" );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void recentCacheOrDeploymentsFeedTest()
        throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeed feed = FeedUtil.getFeed( "recentCacheOrDeployments" );
        List<SyndEntry> entries = feed.getEntries();
        Assert.assertTrue( "Feed should have at least 2 entries", entries.size() >= 2 );

     // we just want the first 2 because this test only deployed 2 artifacts... although we should beef up this tests....
        List<SyndEntry> testEntries = new ArrayList<SyndEntry>();
        testEntries.add( entries.get( 0 ) );
        testEntries.add( entries.get( 1 ) );
        
        this.validateArtifactInFeedEntries( testEntries, gav, "pom", "jar" );
    }

    

}
