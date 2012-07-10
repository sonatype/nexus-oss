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
package org.sonatype.nexus.mindexer.client.internal;

import static org.sonatype.nexus.client.rest.BaseUrl.baseUrlFrom;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy;
import org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy.Strategy;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.mindexer.client.KeywordQuery;
import org.sonatype.nexus.mindexer.client.MavenIndexer;
import org.sonatype.nexus.mindexer.client.SearchRequest;
import org.sonatype.nexus.mindexer.client.SearchResponse;
import org.sonatype.nexus.mindexer.client.SearchResponseArtifact;
import org.sonatype.nexus.mindexer.client.rest.JerseyMavenIndexerSubsystemFactory;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.client.rest.jersey.subsystem.JerseyContentSubsystemFactory;
import org.sonatype.nexus.client.testsuite.AbstractNexusClientTestSupport;

@NexusStartAndStopStrategy( Strategy.EACH_TEST )
public class JerseyMavenIndexerTest
    extends AbstractNexusClientTestSupport
{
    private static boolean prepared = false;

    @Before
    public void prepareInstance()
        throws IOException, InterruptedException
    {
        if ( !prepared )
        {
            // deploy:
            // nexus-api 2.0
            final Content contentService = createClientForLiveInstance().getSubsystem( Content.class );
            contentService.upload( new Location( "releases", "org/sonatype/nexus/nexus-api/2.0/nexus-api-2.0.pom" ),
                resolveTestFile( "nexus-api-2.0.pom" ) );
            contentService.upload( new Location( "releases", "org/sonatype/nexus/nexus-api/2.0/nexus-api-2.0.jar" ),
                resolveTestFile( "nexus-api-2.0.jar" ) );
            contentService.upload(
                new Location( "releases", "org/sonatype/nexus/nexus-api/2.0.1/nexus-api-2.0.1.pom" ),
                resolveTestFile( "nexus-api-2.0.1.pom" ) );
            contentService.upload(
                new Location( "releases", "org/sonatype/nexus/nexus-api/2.0.1/nexus-api-2.0.1.jar" ),
                resolveTestFile( "nexus-api-2.0.1.jar" ) );
            contentService.upload(
                new Location( "releases", "org/sonatype/nexus/nexus-api/2.0.2/nexus-api-2.0.2.pom" ),
                resolveTestFile( "nexus-api-2.0.2.pom" ) );
            contentService.upload(
                new Location( "releases", "org/sonatype/nexus/nexus-api/2.0.2/nexus-api-2.0.2.jar" ),
                resolveTestFile( "nexus-api-2.0.2.jar" ) );
            contentService.download( new Location( "releases", "archetype-catalog.xml" ),
                File.createTempFile( "foo", "bar" ) );
            Thread.sleep( 1000 ); // give one second to nexus to settle
            this.prepared = true;
        }
    }

    protected NexusClientFactory getNexusClientFactory()
    {
        return new JerseyNexusClientFactory( new JerseyContentSubsystemFactory(),
            new JerseyMavenIndexerSubsystemFactory() );
    }

    protected NexusClient createClientForLiveInstance()
        throws MalformedURLException
    {
        final NexusClientFactory factory = getNexusClientFactory();
        final NexusClient client =
            factory.createFor( baseUrlFrom( nexus().getUrl() ), new UsernamePasswordAuthenticationInfo( "admin",
                "admin123" ) );

        return client;
    }

    @Test
    public void identifyBySha1Full()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance();

        final MavenIndexer indexer = client.getSubsystem( MavenIndexer.class );
        Assert.assertNotNull( indexer );

        // nexus-api 2.0
        final SearchResponse artifacts = indexer.identifyBySha1( "ca4f89a6d9aa9c6c407412bc811bb97c1d4712d2" );
        Assert.assertTrue(
            "We expect at least one match! (this might be possible if nexus-api-2.0.jar is duplicated in Nexus instance?)",
            artifacts.getHits().size() > 0 );
        final SearchResponseArtifact hit = artifacts.getHits().get( 0 );
        Assert.assertEquals( "org.sonatype.nexus", hit.getGroupId() );
        Assert.assertEquals( "nexus-api", hit.getArtifactId() );
        Assert.assertEquals( "2.0", hit.getVersion() );
    }

    @Test
    public void identifyBySha1Partial()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance();

        final MavenIndexer indexer = client.getSubsystem( MavenIndexer.class );
        Assert.assertNotNull( indexer );

        // nexus-api 2.0
        // yes, this was always like this: as long the PREFIX of SHA1 is unique, it still works
        // even without supplying the FULL sha1, similar to GIT
        SearchResponse artifacts = indexer.identifyBySha1( "ca4f89a6d9aa" );
        Assert.assertTrue(
            "We expect at least one match! (this might be possible if nexus-api-2.0.jar is duplicated in Nexus instance?)",
            artifacts.getHits().size() > 0 );
        final SearchResponseArtifact hit = artifacts.getHits().get( 0 );
        Assert.assertEquals( "org.sonatype.nexus", hit.getGroupId() );
        Assert.assertEquals( "nexus-api", hit.getArtifactId() );
        Assert.assertEquals( "2.0", hit.getVersion() );
    }

    @Test
    public void searchByGav()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance();

        final MavenIndexer indexer = client.getSubsystem( MavenIndexer.class );
        Assert.assertNotNull( indexer );

        SearchResponse artifacts =
            indexer.searchByGAV( "org.sonatype.nexus", "nexus-api", null, null, null, "releases" );
        Assert.assertTrue( "We expect multiple matches!", artifacts.getHits().size() > 1 );
        final SearchResponseArtifact hit = artifacts.getHits().get( 0 );
        Assert.assertEquals( "org.sonatype.nexus", hit.getGroupId() );
        Assert.assertEquals( "nexus-api", hit.getArtifactId() );
    }

    @Test
    public void searchPaged()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance();

        final MavenIndexer indexer = client.getSubsystem( MavenIndexer.class );
        Assert.assertNotNull( indexer );

        // some very broad query:
        final KeywordQuery query = new KeywordQuery();
        query.setKeyword( "sonatype" ); // all of sonatype
        final SearchRequest request = new SearchRequest( 0, 1, "releases", query );

        SearchResponse artifacts = indexer.search( request );
        Assert.assertTrue( "Total count must be enourmous, it represents full domain size",
            artifacts.getTotalCount() == 3 );
        Assert.assertEquals( "We expect 10 matches (domain is huge, but page size is 10)!", 1,
            artifacts.getHits().size() );
        final SearchResponseArtifact firstFromFirstPage = artifacts.getHits().get( 0 );
        final String firstFromFirstPageAsString =
            firstFromFirstPage.getGroupId() + ":" + firstFromFirstPage.getArtifactId() + ":"
                + firstFromFirstPage.getVersion();

        // now get "next page"
        artifacts = indexer.search( artifacts.getRequestForNextPage() );
        Assert.assertTrue( "Total count must be enourmous, it represents full domain size",
            artifacts.getTotalCount() == 3 );
        Assert.assertEquals( "We expect 10 matches (domain is huge, but page size is 10)!", 1,
            artifacts.getHits().size() );

        // pages must contain different things
        final SearchResponseArtifact firstFromSecondPage = artifacts.getHits().get( 0 );
        final String firstFromSecondPageAsString =
            firstFromSecondPage.getGroupId() + ":" + firstFromSecondPage.getArtifactId() + ":"
                + firstFromSecondPage.getVersion();

        Assert.assertTrue( "We expect different things at different pages",
            !firstFromFirstPageAsString.equals( firstFromSecondPageAsString ) );
    }
}
