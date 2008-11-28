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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
 * 
 * @plexus.component
 */
public class DefaultIndexPacker
    extends AbstractLogEnabled
    implements IndexPacker
{
    private static final int MAX_CHUNKS = 30;

    public void packIndex( IndexingContext context, File targetDir )
        throws IOException, IllegalArgumentException
    {
        if ( targetDir == null )
        {
            throw new IllegalArgumentException( "The target dir is null" );
        }

        if ( targetDir.exists() )
        {
            if ( !targetDir.isDirectory() )
            {
                throw new IllegalArgumentException( //
                    String.format( "Specified target path %s is not a directory", targetDir.getAbsolutePath() ) );
            } 
            if ( !targetDir.canWrite() ) 
            {
                throw new IllegalArgumentException( 
                    String.format( "Specified target path %s is not writtable", targetDir.getAbsolutePath() ) );
            }
        }
        else 
        {
            if( !targetDir.mkdirs() ) 
            {
                throw new IllegalArgumentException( "Can't create " + targetDir.getAbsolutePath() );
            }
        }

        Properties info = new Properties();
        
        DateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_DAY_FORMAT );
        
        Map<String, List<Integer>> chunks = getIndexChunks( context, df );

        writeIndexChunks( context, info, MAX_CHUNKS, chunks, df, targetDir );
        
        writeIndexArchive( context, new File( targetDir, IndexingContext.INDEX_FILE + ".zip" ) );

        writeIndexProperties( context, info, new File( targetDir, IndexingContext.INDEX_FILE + ".properties" ) );
    }

    Map<String, List<Integer>> getIndexChunks( IndexingContext context, DateFormat df ) 
        throws IOException
    {
        Map<String, List<Integer>> chunks = new TreeMap<String, List<Integer>>( Collections.<String>reverseOrder() );
        
        IndexReader r = context.getIndexReader();
        
        for ( int i = 0; i < r.numDocs(); i++ )
        {
            if ( !r.isDeleted( i ) )
            {
                Document d = r.document( i );
    
                String lastModified = d.get( ArtifactInfo.LAST_MODIFIED );
                
                if( lastModified != null )
                {
                    Date t = new Date( Long.parseLong( lastModified ) );
                    getChunk( chunks, df.format( t ) ).add( i );
                }
            }
        }
        
        return chunks;
    }

    void writeIndexChunks( IndexingContext context, Properties info, 
        int max, Map<String, List<Integer>> chunks, DateFormat df, File targetDir ) 
        throws IOException
    {
        if ( chunks.size() < 2 )
        {
           return;  // no updates available
        }
        
        RAMDirectory chunkDir = new RAMDirectory();

        IndexingContext chunkContext;
        try 
        {
            chunkContext = new DefaultIndexingContext(
                context.getId(),
                context.getRepositoryId(),
                null,
                chunkDir,
                null,
                null,
                context.getIndexCreators(), 
                false );
        } 
        catch ( UnsupportedExistingLuceneIndexException ex ) 
        {
            throw new IOException( "Can't create temporary indexing context" );
        }

        IndexReader r = context.getIndexReader();
        
        IndexWriter w = chunkContext.getIndexWriter();
        
        int n = 0;
        
        for ( Entry<String, List<Integer>> e : chunks.entrySet() ) 
        {
            String key = e.getKey();
          
            for ( int i : e.getValue() )
            {
                context.copyDocument( r.document( i ), w );
            }
            
            w.flush();
            w.optimize();
            
            try
            {
                info.put( IndexingContext.INDEX_DAY_PREFIX + n, format( df.parse( key ) ) );
            }
            catch ( ParseException ex ) 
            {
            }
            
            writeIndexArchive( chunkContext, new File( targetDir, IndexingContext.INDEX_FILE + "." + key + ".zip" ));
        
            n++;
            
            if ( max <= n || n == chunks.size() - 1 ) 
            {
                break;
            }
        }
        
        w.close();
        
        chunkContext.close( /* delete files */ false );

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

    void writeIndexProperties( IndexingContext context, Properties info, File propertiesFile )
        throws IOException
    {
        info.setProperty( IndexingContext.INDEX_ID, context.getId() );

        Date timestamp = context.getTimestamp();
        
        if( timestamp == null )
        {
            timestamp = new Date( 0 );  // never updated
        }
        
        info.setProperty( IndexingContext.INDEX_TIMESTAMP, format( timestamp ) );

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
        return new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT ).format( d );
    }

    private List<Integer> getChunk( Map<String, List<Integer>> chunks, String key ) 
    {
        List<Integer> chunk = chunks.get( key );
        if( chunk == null) 
        {
            chunk = new ArrayList<Integer>();
            chunks.put( key, chunk );
        }
        return chunk;
    }
    
}

