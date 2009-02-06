/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.sonatype.nexus.index.AbstractRepoNexusIndexerTest;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.NexusIndexer;

/**
 * @author Eugene Kuleshov
 */
public class IndexDataTest
    extends AbstractRepoNexusIndexerTest
{
    private Directory newDir;

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        indexDir = new RAMDirectory();
        
        context = nexusIndexer.addIndexingContext(
            "test-default",
            "test",
            repo,
            indexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX );

        // assertNull( context.getTimestamp() ); // unknown upon creation

        nexusIndexer.scan( context );

        Date timestamp = context.getTimestamp();
        
        assertNotNull( timestamp );

        // save and restore index to be used by common tests 

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IndexDataWriter dw = new IndexDataWriter( bos );
        dw.write( context, null );

        ByteArrayInputStream is = new ByteArrayInputStream( bos.toByteArray() );

        newDir = new RAMDirectory();

        Date newTimestamp = DefaultIndexUpdater.unpackIndexData( is, newDir, context.getIndexCreators() );

        assertEquals( timestamp, newTimestamp );
        
        context.replace( newDir );
    }
    
    public void testData()
        throws Exception
    {
        IndexReader r1 = context.getIndexReader();
        
        Map<String, ArtifactInfo> r1map = readIndex( r1 );
        
        IndexReader r2 = IndexReader.open( newDir );

        Map<String, ArtifactInfo> r2map = readIndex( r2 );
        
        for ( Entry<String, ArtifactInfo> e : r1map.entrySet() )
        {
            String key = e.getKey();
            assertTrue( "Expected for find " + key, r2map.containsKey( key ) );
        }
        
        assertEquals( r1map.size(), r2map.size() );
    }

    private Map<String, ArtifactInfo> readIndex( IndexReader r1 )
        throws CorruptIndexException,
            IOException
    {
        Map<String,ArtifactInfo> map = new HashMap<String, ArtifactInfo>(); 

        for ( int i = 0; i < r1.maxDoc(); i++ )
        {
            Document document = r1.document( i );
            
            ArtifactInfo ai = context.constructArtifactInfo( document );
            
            if( ai != null)
            {
                map.put( ai.getUinfo(), ai );
            }
        }
        
        return map;
    }

}
