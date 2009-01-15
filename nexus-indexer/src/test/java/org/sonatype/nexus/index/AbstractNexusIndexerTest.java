/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.context.IndexingContext;

public abstract class AbstractNexusIndexerTest
    extends PlexusTestCase
{
    protected NexusIndexer nexusIndexer;

    protected Directory indexDir = new RAMDirectory();

    protected IndexingContext context;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        // FileUtils.deleteDirectory( indexDir );
        nexusIndexer = lookup( NexusIndexer.class );
        prepareNexusIndexer( nexusIndexer );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        unprepareNexusIndexer( nexusIndexer );
        // TODO: Brian reported, does not work on Windows because of left open files?
        // FileUtils.deleteDirectory( indexDir );
    }

    protected abstract void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception;

    protected void unprepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        nexusIndexer.removeIndexingContext( context, false );
    }

    protected void assertGroup( int expected, String group, IndexingContext context )
        throws IOException
    {
        // ArtifactInfo.UINFO - UN_TOKENIZED
        // ArtifactInfo.GROUP_ID - TOKENIZED

        Term term = new Term( ArtifactInfo.GROUP_ID, group );
        PrefixQuery pq = new PrefixQuery( term );
        // new WildcardQuery( //
        // SpanTermQuery pq = new SpanTermQuery( term );
        // PhraseQuery pq = new PhraseQuery();
        // pq.add( new Term( ArtifactInfo.UINFO, group + "*" ) );

        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( pq, context ) );
        Collection<ArtifactInfo> artifacts = response.getResults();
        assertEquals( artifacts.toString(), expected, artifacts.size() );
    }

}
