/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

public class UniqueArtifactFilterTest
    extends AbstractIndexCreatorHelper
{
    private IndexingContext context;

    public void testSearchIterator()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = indexer.constructQuery( MAVEN.GROUP_ID, "qdox", SearchType.SCORED );

        IteratorSearchRequest request = new IteratorSearchRequest( q );

        IteratorSearchResponse response = indexer.searchIterator( request );

        assertEquals( 2, response.getTotalHits() );

        for ( ArtifactInfo ai : response.getResults() )
        {
            assertEquals( "GroupId must match \"qdox\"!", "qdox", ai.groupId );
        }
    }

    public void testSearchIteratorWithFilter()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = indexer.constructQuery( MAVEN.GROUP_ID, "commons", SearchType.SCORED );

        UniqueArtifactFilterPostprocessor filter = new UniqueArtifactFilterPostprocessor();
        filter.addField( MAVEN.GROUP_ID );
        filter.addField( MAVEN.ARTIFACT_ID );

        IteratorSearchRequest request = new IteratorSearchRequest( q, filter );

        IteratorSearchResponse response = indexer.searchIterator( request );

        assertEquals( "15 total hits (before filtering!)", 15, response.getTotalHits() );

        ArtifactInfo ai = response.getResults().next();
        assertTrue( "Iterator has to have next (2 should be returned)", ai != null );

        ai = response.getResults().next();
        assertTrue( "Iterator has to have next (2 should be returned)", ai != null );

        assertEquals( "Property that is not unique has to have \"COLLAPSED\" value!",
            UniqueArtifactFilterPostprocessor.COLLAPSED, ai.version );
        assertEquals( "Property that is not unique has to have \"COLLAPSED\" value!",
            UniqueArtifactFilterPostprocessor.COLLAPSED, ai.packaging );
        assertEquals( "Property that is not unique has to have \"COLLAPSED\" value!",
            UniqueArtifactFilterPostprocessor.COLLAPSED, ai.classifier );
    }

    // ==

    private NexusIndexer prepare()
        throws Exception, IOException, UnsupportedExistingLuceneIndexException
    {
        NexusIndexer indexer = lookup( NexusIndexer.class );

        // Directory indexDir = new RAMDirectory();
        File indexDir = new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );
        FileUtils.deleteDirectory( indexDir );

        File repo = new File( getBasedir(), "src/test/repo" );

        context = indexer.addIndexingContext( "test", "test", repo, indexDir, null, null, DEFAULT_CREATORS );

        indexer.scan( context );

        return indexer;
    }
}
