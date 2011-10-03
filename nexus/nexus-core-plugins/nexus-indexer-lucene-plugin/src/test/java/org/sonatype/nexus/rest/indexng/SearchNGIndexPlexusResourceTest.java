/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.rest.indexng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * Test for SearchNGIndexPlexusResource
 */
public class SearchNGIndexPlexusResourceTest
    extends AbstractNexusTestCase
{

    @Before
    public void setUp()
        throws Exception
    {
    }

    @Test
    public void testGet()
        throws Exception
    {
        SearchNGIndexPlexusResource resource = new SearchNGIndexPlexusResource();
        Map<String, String> terms = new HashMap<String, String>( 4 );
        terms.put( "q", "!" );
        Searcher searcher = mock( Searcher.class );
        when( searcher.canHandle( Mockito.any( Map.class ) ) ).thenReturn( true );

        when( searcher.flatIteratorSearch( Mockito.any( Map.class ), anyString(), anyInt(), anyInt(), anyInt(), anyBoolean(),
                                           Mockito.any( SearchType.class ), Mockito.any( List.class ) ) )
            // emulate current indexer search behavior, illegal query results in IllegalArgEx with the ParseEx as cause
            .thenThrow( new IllegalArgumentException( new ParseException( "mock" ) ) );

        try
        {
            resource.searchByTerms( terms, "rid", 1, 1, false, false, true, Collections.<ArtifactInfoFilter>emptyList(),
                                    Arrays.asList( searcher ) );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse resultObject = (ErrorResponse) e.getResultObject();
            assertThat( resultObject, notNullValue() );
            List<ErrorMessage> errors = resultObject.getErrors();
            assertThat( errors, hasSize( 1 ) );
            ErrorMessage errorMessage = errors.get( 0 );

            // ID needs to be stable for UI handling
            assertThat( errorMessage.getId(), equalTo( "search" ));
            assertThat( errorMessage.getMsg(), containsString( "mock" ) );
        }
    }
}
