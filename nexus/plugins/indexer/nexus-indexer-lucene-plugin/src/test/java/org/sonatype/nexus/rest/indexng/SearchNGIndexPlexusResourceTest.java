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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.SearchType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.index.Searcher;
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
            resource.searchByTerms( terms, "rid", 1, 1, false, false, true,
                                    Collections.<ArtifactInfoFilter> emptyList(), Arrays.asList( searcher ) );

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
    public void testUncollapseResults()
        throws Exception
    {
        fillInRepo();

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

        Assert.assertEquals( 1, result.getTotalCount() );
    }
}
