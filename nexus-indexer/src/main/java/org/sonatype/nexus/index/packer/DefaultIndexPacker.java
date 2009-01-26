/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
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
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

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
            Map<String, List<Integer>> chunks = getIndexChunks( request );

            writeIndexChunks( info, chunks, request );
        }

        if ( request.getFormats().contains( IndexPackingRequest.IndexFormat.FORMAT_LEGACY ) )
        {
            writeIndexArchive( request.getContext(), new File( request.getTargetDir(), IndexingContext.INDEX_FILE
                + ".zip" ) );
        }

        if ( request.getFormats().contains( IndexPackingRequest.IndexFormat.FORMAT_V1 ) )
        {
            writeIndexData( request.getContext(), null, new File( request.getTargetDir(), IndexingContext.INDEX_FILE
                + ".gz" ) );
        }

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

        IndexingContext context = request.getContext();

        int n = 0;

        List<Integer> currentIndexes = null;

        for ( Entry<String, List<Integer>> e : chunks.entrySet() )
        {
            String key = e.getKey();

            info.put( IndexingContext.INDEX_CHUNK_PREFIX + n, format( request.getIndexChunker().getChunkDate( key ) ) );

            List<Integer> indexes = e.getValue();
            
            if ( currentIndexes != null )
            {
                indexes.addAll(currentIndexes);
            }
            
            currentIndexes = indexes;

            writeIndexData( context, //
                indexes, new File( request.getTargetDir(), //
                    IndexingContext.INDEX_FILE + "." + key + ".gz" ) );

            n++;

            if ( request.getMaxIndexChunks() <= n || n == chunks.size() - 1 )
            {
                break;
            }
        }
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

    void writeIndexData( IndexingContext context, List<Integer> docIndexes, File targetArchive )
        throws IOException
    {
        if ( targetArchive.exists() )
        {
            targetArchive.delete();
        }

        OutputStream os = null;

        try
        {
            os = new FileOutputStream( targetArchive );

            IndexUtils.packIndexData( os, context, docIndexes );

            os.flush();
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

//        if ( request.isCreateIncrementalChunks() )
//        {
//            info.setProperty( IndexingContext.INDEX_CHUNKS_RESOLUTION, request.getIndexChunker().getId() );
//        }

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
        SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
        df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
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
