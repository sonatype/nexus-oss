/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.packer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

/**
 * Default provider for IndexPacker. Creates the properties and zip files.
 * 
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 * @plexus.component
 */
public class DefaultIndexPacker
    extends AbstractLogEnabled
    implements IndexPacker
{
    private final SimpleDateFormat df;

    public DefaultIndexPacker()
    {
        this.df = new SimpleDateFormat( INDEX_TIME_FORMAT );
        this.df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    public void packIndex( IndexPackingRequest request )
        throws IOException,
            IllegalArgumentException
    {
        if ( request.getTargetDir() == null )
        {
            throw new IllegalArgumentException( "The target dir is null" );
        }

        if ( request.getTargetDir().exists() )
        {
            if ( !request.getTargetDir().isDirectory() )
            {
                throw new IllegalArgumentException( //
                    String.format( "Specified target path %s is not a directory", request
                        .getTargetDir().getAbsolutePath() ) );
            }
            if ( !request.getTargetDir().canWrite() )
            {
                throw new IllegalArgumentException( String.format( "Specified target path %s is not writtable", request
                    .getTargetDir().getAbsolutePath() ) );
            }
        }
        else
        {
            if ( !request.getTargetDir().mkdirs() )
            {
                throw new IllegalArgumentException( "Can't create " + request.getTargetDir().getAbsolutePath() );
            }
        }

        Properties info = new Properties();

        if ( request.isCreateIncrementalChunks() )
        {
            if ( request.getIndexChunker() == null )
            {
                throw new IllegalArgumentException( "Can't create incremental index without supplied chunker!" );
            }

            Map<String, List<Integer>> chunks = getIndexChunks( request );

            writeIndexChunks( info, chunks, request );

        }

        writeIndexArchive( request.getContext(), new File( request.getTargetDir(), IndexingContext.INDEX_FILE + ".zip" ) );

        writeIndexProperties( request, info, new File( request.getTargetDir(), IndexingContext.INDEX_FILE
            + ".properties" ) );
    }

    private Map<String, List<Integer>> getIndexChunks( IndexPackingRequest request )
        throws IOException
    {
        Map<String, List<Integer>> chunks = new TreeMap<String, List<Integer>>( Collections.<String> reverseOrder() );

        IndexReader r = request.getContext().getIndexReader();

        for ( int i = 0; i < r.numDocs(); i++ )
        {
            if ( !r.isDeleted( i ) )
            {
                Document d = r.document( i );

                String lastModified = d.get( ArtifactInfo.LAST_MODIFIED );

                if ( lastModified != null )
                {
                    Date t = new Date( Long.parseLong( lastModified ) );

                    String chunkId = request.getIndexChunker().getChunkId( t );

                    if ( chunkId != null )
                    {
                        getChunk( chunks, chunkId ).add( i );
                    }
                }
            }
        }

        return chunks;
    }

    void writeIndexChunks( Properties info, Map<String, List<Integer>> chunks, IndexPackingRequest request )
        throws IOException
    {
        if ( chunks.size() < 2 )
        {
            return; // no updates available
        }

        RAMDirectory chunkDir = new RAMDirectory();

        IndexingContext chunkContext;
        try
        {
            chunkContext = new DefaultIndexingContext(
                request.getContext().getId(),
                request.getContext().getRepositoryId(),
                null,
                chunkDir,
                null,
                null,
                request.getContext().getIndexCreators(),
                false );
        }
        catch ( UnsupportedExistingLuceneIndexException ex )
        {
            throw new IOException( "Can't create temporary indexing context" );
        }

        IndexReader r = request.getContext().getIndexReader();

        IndexWriter w = chunkContext.getIndexWriter();

        int n = 0;

        for ( Entry<String, List<Integer>> e : chunks.entrySet() )
        {
            String key = e.getKey();

            for ( int i : e.getValue() )
            {
                request.getContext().copyDocument( r.document( i ), w );
            }

            w.flush();

            w.optimize();

            info.put(
                IndexingContext.INDEX_PROPERTY_PREFIX + request.getIndexChunker().getId() + "-" + n,
                format( request.getIndexChunker().getChunkDate( key ) ) );

            writeIndexArchive( chunkContext, new File( request.getTargetDir(), IndexingContext.INDEX_FILE + "." + key
                + ".zip" ) );

            n++;

            if ( request.getMaxIndexChunks() <= n || n == chunks.size() - 1 )
            {
                break;
            }
        }

        w.close();

        r.close();

        chunkContext.close( /* delete files */false );

        // ctxDir.delete();

        chunkDir.close();
    }

    void writeIndexArchive( IndexingContext context, File targetArchive )
        throws IOException
    {
        if ( targetArchive.exists() )
        {
            targetArchive.delete();
        }

        OutputStream os = null;

        try
        {
            os = new BufferedOutputStream( new FileOutputStream( targetArchive ), 4096 );

            IndexUtils.packIndexArchive( context, os );
        }
        finally
        {
            IOUtil.close( os );
        }
    }

    void writeIndexProperties( IndexPackingRequest request, Properties info, File propertiesFile )
        throws IOException
    {
        info.setProperty( IndexingContext.INDEX_ID, request.getContext().getId() );

        Date timestamp = request.getContext().getTimestamp();

        if ( timestamp == null )
        {
            timestamp = new Date( 0 ); // never updated
        }

        info.setProperty( IndexingContext.INDEX_TIMESTAMP, format( timestamp ) );

        if ( request.isCreateIncrementalChunks() )
        {
            info.setProperty( IndexingContext.INDEX_CHUNKS_RESOLUTION, request.getIndexChunker().getId() );
        }

        OutputStream os = null;

        try
        {
            os = new FileOutputStream( propertiesFile );

            info.store( os, null );
        }
        finally
        {
            IOUtil.close( os );
        }
    }

    private String format( Date d )
    {
        return df.format( d );
    }

    private List<Integer> getChunk( Map<String, List<Integer>> chunks, String key )
    {
        List<Integer> chunk = chunks.get( key );

        if ( chunk == null )
        {
            chunk = new ArrayList<Integer>();

            chunks.put( key, chunk );
        }

        return chunk;
    }

}
