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
package org.sonatype.nexus.rest.indexng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.index.SearchType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * Test for SearchNGIndexPlexusResource
 */
public class SearchNGIndexPlexusResourceTest
    extends AbstractMavenRepoContentTests
{

    @Test
    public void testPlexusResourceException()
        throws Exception
    {
        SearchNGIndexPlexusResource resource = new SearchNGIndexPlexusResource();
        Map<String, String> terms = new HashMap<String, String>( 4 );
        terms.put( "q", "!" );
        Searcher searcher = mock( Searcher.class );
        when( searcher.canHandle( Mockito.any( Map.class ) ) ).thenReturn( true );

        when(
            searcher.flatIteratorSearch( Mockito.any( Map.class ), anyString(), anyInt(), anyInt(), anyInt(),
                                         anyBoolean(), Mockito.any( SearchType.class ), Mockito.any( List.class ) ) )
            // emulate current indexer search behavior, illegal query results in IllegalArgEx with the ParseEx as cause
            .thenThrow( new IllegalArgumentException( new ParseException( "mock" ) ) );

        try
        {
            resource.searchByTerms( terms, "rid", 1, 1, false, Arrays.asList( searcher ) );
            Assert.fail( "Expected PlexusResourceException" );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse resultObject = (ErrorResponse) e.getResultObject();
            assertThat( resultObject, notNullValue() );
            List<ErrorMessage> errors = resultObject.getErrors();
            assertThat( errors, hasSize( 1 ) );
            ErrorMessage errorMessage = errors.get( 0 );

            // ID needs to be stable for UI handling
            assertThat( errorMessage.getId(), equalTo( "search" ) );
            assertThat( errorMessage.getMsg(), containsString( "mock" ) );
        }
    }

    @Test
    public void NEXUS5412Uncollapse()
        throws Exception
    {
        // disable security completely, as it just interferes with test
        getNexus().getNexusConfiguration().setSecurityEnabled( false );
        getNexus().getNexusConfiguration().saveConfiguration();
        wairForAsyncEventsToCalmDown();
        waitForTasksToStop();

        // we deploy 50 fluke artifacts, to have them end up on index
        // same GA but version goes 1..50
        final IndexerManager indexerManager = lookup( IndexerManager.class );
        final Repository releases = repositoryRegistry.getRepository( "releases" );
        for ( int i = 1; i <= 50; i++ )
        {
            final String path = String.format( "/org/nexus5412/%s/nexus5412-%s.jar", i, i );
            final ResourceStoreRequest request = new ResourceStoreRequest( path );
            releases.storeItem( request, new ByteArrayInputStream( "Junk JAR".getBytes() ), null );
        }
        wairForAsyncEventsToCalmDown();
        waitForTasksToStop();

        final SearchNGIndexPlexusResource subject =
            (SearchNGIndexPlexusResource) lookup( PlexusResource.class, SearchNGIndexPlexusResource.ROLE_HINT );
        Context context = new Context();
        Request request = new Request();
        Reference ref = new Reference( "http://localhost:12345/" );
        request.setRootRef( ref );
        request.setResourceRef( new Reference( ref, SearchNGIndexPlexusResource.RESOURCE_URI
            + "?q=nexus5412&collapseresults=true" ) );
        Response response = new Response( request );

        // perform a search
        SearchNGResponse result = subject.get( context, request, response, null );

        // NEXUS-5412 causes this assertion below to fail
        // Reason: by mistake, there are two comparisons against DEFAULT_COLLAPSE_OVERRIDE_TRESHOLD, a constant
        // that defines the "lower threshold" when to un-collapse, but one of those checks compares apples to oranges.
        // The "unit" of this constant is "rows in UI", while the 1st comparison is done against matched ArtifactInfos
        // Clearly, as we above created 50 (50 > 35), the check will pass. Then, "repackage" happens, where collapse
        // is applied too, and result ends up with having 1 line (versions collapsed, GA gives 1 line, they are all same)
        // Second check will detect this, and set collapseResults=false, but search is not redone anymore.
        Assert.assertEquals( 50, result.getData().size() );
    }

    @Test
    public void testUncollapseResults()
        throws Exception
    {
        fillInRepo();
        getNexus().getNexusConfiguration().setSecurityEnabled( false );
        getNexus().getNexusConfiguration().saveConfiguration();
        wairForAsyncEventsToCalmDown();
        waitForTasksToStop();

        final IndexerManager indexerManager = lookup( IndexerManager.class );
        indexerManager.reindexAllRepositories( "/", true );

        SearchNGIndexPlexusResource subject =
            (SearchNGIndexPlexusResource) lookup( PlexusResource.class, SearchNGIndexPlexusResource.ROLE_HINT );

        Context context = new Context();
        Request request = new Request();
        Reference ref = new Reference( "http://localhost:12345/" );
        request.setRootRef( ref );
        request.setResourceRef( new Reference( ref, SearchNGIndexPlexusResource.RESOURCE_URI
            + "?q=nexus&collapseresults=true" ) );

        Response response = new Response( request );
        SearchNGResponse result = subject.get( context, request, response, null );

        // explanation:
        // we test here, does this resource "expand" the result set even if the request told to collaps
        // (like UI does). This happens when result set (the grid count in search UI) would contain less
        // rows than COLLAPSE_OVERRIDE_TRESHOLD = 35 lines. If yes, it will repeat the search but uncollapsed
        // kinda overriding the "hint" that was in original request (see request query parameters above).
        //
        // Found items uncollapsed (without any specific order, is unstable):
        // org.sonatype.nexus:nexus:1.3.0-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4
        // org.sonatype.nexus:nexus-indexer:1.0-beta-5-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-3-SNAPSHOT
        // org.sonatype.nexus:nexus:1.2.2-SNAPSHOT
        // org.sonatype:nexus-3148:1.0.SNAPSHOT
        //
        // Found items collapsed (G:A:maxVersion):
        // org.sonatype.nexus:nexus:1.3.0-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4 (rel preferred over snap)
        // org.sonatype:nexus-3148:1.0.SNAPSHOT

        // we assert that the grid would contain 7, not 3 hits (corresponds to grid lines in Search UI)
        Assert.assertEquals( 7, result.getData().size() );
    }
}
