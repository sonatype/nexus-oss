/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
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
        nexusIndexer = (NexusIndexer) lookup( NexusIndexer.class );
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
        throws IOException,
            IndexContextInInconsistentStateException
    {
        // ArtifactInfo.UINFO - UN_TOKENIZED
        // ArtifactInfo.GROUP_ID - TOKENIZED

        Term term = new Term( ArtifactInfo.GROUP_ID, group );
        PrefixQuery pq = new PrefixQuery( term );
        // new WildcardQuery( //
        // SpanTermQuery pq = new SpanTermQuery( term );
        // PhraseQuery pq = new PhraseQuery();
        // pq.add( new Term( ArtifactInfo.UINFO, group + "*" ) );

        Collection<ArtifactInfo> artifacts = nexusIndexer.searchFlat( ArtifactInfo.VERSION_COMPARATOR, pq, context );
        assertEquals( artifacts.toString(), expected, artifacts.size() );
    }

}
