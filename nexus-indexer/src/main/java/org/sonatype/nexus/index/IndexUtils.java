/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockObtainFailedException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.DefaultArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusAnalyzer;
import org.sonatype.nexus.index.creator.IndexCreator;

public class IndexUtils
{

    public static final String TIMESTAMP_FILE = "timestamp";

    // timestamp

    public static void deleteTimestamp( Directory directory )
        throws IOException
    {
        if ( directory.fileExists( TIMESTAMP_FILE ) )
        {
            directory.deleteFile( TIMESTAMP_FILE );
        }
    }

    public static void updateTimestamp( Directory directory, Date timestamp )
        throws IOException
    {
        synchronized ( directory )
        {
            Date currentTimestamp = getTimestamp( directory );

            if ( timestamp != null && ( currentTimestamp == null || !currentTimestamp.equals( timestamp ) ) )
            {
                deleteTimestamp( directory );

                IndexOutput io = directory.createOutput( TIMESTAMP_FILE );

                try
                {
                    io.writeLong( timestamp.getTime() );

                    io.flush();
                }
                finally
                {
                    close( io );
                }
            }
        }
    }

    public static Date getTimestamp( Directory directory )
    {
        synchronized ( directory )
        {
            Date result = null;
            try
            {
                if ( directory.fileExists( TIMESTAMP_FILE ) )
                {
                    IndexInput ii = null;

                    try
                    {
                        ii = directory.openInput( TIMESTAMP_FILE );

                        result = new Date( ii.readLong() );
                    }
                    finally
                    {
                        close( ii );
                    }
                }
            }
            catch ( IOException ex )
            {
            }

            return result;
        }
    }

    // pack/unpack

    public static void updateDocument( Document doc, Collection<? extends IndexCreator> ics )
    {
        ArtifactInfo ai = IndexUtils.constructArtifactInfo( doc, ics );
        
        if( ai != null )
        {
            ArtifactContext ac = new ArtifactContext( null, null, null, ai, ai.calculateGav() );
            ArtifactIndexingContext aic = new DefaultArtifactIndexingContext( ac );
   
            for ( IndexCreator ic : ics )
            {
                ic.updateDocument( aic, doc );
            }
        }
    }

    

//    public static Date getIndexArchiveTime( InputStream is )
//        throws IOException
//    {
//        ZipInputStream zis = null;
//        try
//        {
//            zis = new ZipInputStream( is );
//
//            long timestamp = -1;
//
//            ZipEntry entry;
//            while ( ( entry = zis.getNextEntry() ) != null )
//            {
//                if ( entry.getName() == IndexUtils.TIMESTAMP_FILE )
//                {
//                    return new Date( new DataInputStream( zis ).readLong() );
//                }
//                timestamp = entry.getTime();
//            }
//
//            return timestamp == -1 ? null : new Date( timestamp );
//        }
//        finally
//        {
//            close( zis );
//            close( is );
//        }
//    }

    /**
     * Unpack legacy index archive into a specified Lucene <code>Directory</code>
     * 
     * @param is a <code>ZipInputStream</code> with index data
     * @param directory Lucene <code>Directory</code> to unpack index data to
     * @return {@link Date} of the index update or null if it can't be read
     */
    public static Date unpackIndexArchive( InputStream is, Directory directory, Collection<? extends IndexCreator> ics )
        throws IOException
    {
        File indexArchive = File.createTempFile( "nexus-index", "" );

        File indexDir = new File( indexArchive.getAbsoluteFile().getParentFile(), indexArchive.getName() + ".dir" );

        indexDir.mkdirs();

        FSDirectory fdir = FSDirectory.getDirectory( indexDir );

        ZipInputStream zis = new ZipInputStream( is );
        try
        {
            unpackDirectory( fdir, zis );
            copyUpdatedDocuments( fdir, directory, ics );
            
            Date timestamp = IndexUtils.getTimestamp( fdir );
            updateTimestamp( directory, timestamp );
            return timestamp;
        }
        finally
        {
            close( zis );
            close( fdir );
            
            indexArchive.delete();

            try
            {
                FileUtils.deleteDirectory( indexDir );
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }
    }

    private static void copyUpdatedDocuments( Directory sourcedir, Directory targetdir,
        Collection<? extends IndexCreator> ics )
        throws CorruptIndexException,
            LockObtainFailedException,
            IOException
    {
        IndexWriter w = null;
        IndexReader r = null;

        try
        {
            r = IndexReader.open( sourcedir );
            w = new IndexWriter( targetdir, false, new NexusAnalyzer(), true );

            for ( int i = 0; i < r.maxDoc(); i++ )
            {
                Document d = r.document( i );
                updateDocument( d, ics );
                w.addDocument( d );
            }

            w.optimize();
            w.flush();
        }
        finally
        {
            close( w );
            close( r );
        }
    }

    private static void unpackDirectory( Directory directory, ZipInputStream zis )
        throws IOException
    {
        byte[] buf = new byte[4096];

        ZipEntry entry;

        while ( ( entry = zis.getNextEntry() ) != null )
        {
            if ( entry.isDirectory() || entry.getName().indexOf( '/' ) > -1 )
            {
                continue;
            }

            IndexOutput io = directory.createOutput( entry.getName() );
            try
            {
                int n = 0;

                while ( ( n = zis.read( buf ) ) != -1 )
                {
                    io.writeBytes( buf, n );
                }
            }
            finally
            {
                close( io );
            }
        }
    }

    /**
     * Pack legacy index archive into a specified output stream
     */
    public static void packIndexArchive( IndexingContext context, OutputStream os )
        throws IOException
    {
        ZipOutputStream zos = null;

        // force the timestamp update
        updateTimestamp( context.getIndexDirectory(), context.getTimestamp() );

        try
        {
            zos = new ZipOutputStream( os );

            zos.setLevel( 9 );

            String[] names = context.getIndexDirectory().list();

            boolean savedTimestamp = false;

            byte[] buf = new byte[8192];

            for ( int i = 0; i < names.length; i++ )
            {
                String name = names[i];

                writeFile( name, zos, context.getIndexDirectory(), buf );

                if ( name.equals( TIMESTAMP_FILE ) )
                {
                    savedTimestamp = true;
                }
            }

            // FSDirectory filter out the foreign files
            if ( !savedTimestamp && context.getIndexDirectory().fileExists( TIMESTAMP_FILE ) )
            {
                writeFile( TIMESTAMP_FILE, zos, context.getIndexDirectory(), buf );
            }
        }
        finally
        {
            close( zos );
        }
    }

    private static void writeFile( String name, ZipOutputStream zos, Directory directory, byte[] buf )
        throws IOException
    {
        ZipEntry e = new ZipEntry( name );

        zos.putNextEntry( e );

        IndexInput in = directory.openInput( name );

        try
        {
            int toRead = 0;

            int bytesLeft = (int) in.length();

            while ( bytesLeft > 0 )
            {
                toRead = ( bytesLeft >= buf.length ) ? buf.length : bytesLeft;
                bytesLeft -= toRead;

                in.readBytes( buf, 0, toRead, false );

                zos.write( buf, 0, toRead );
            }
        }
        finally
        {
            close( in );
        }

        zos.flush();

        zos.closeEntry();
    }

    // filter

    public static void filterDirectory( Directory directory, DocumentFilter filter )
        throws IOException
    {
        // analyzer is unimportant, since we are not adding/searching to/on index, only reading/deleting
        IndexWriter w = null;

        IndexReader r = IndexReader.open( directory );

        try
        {
            try
            {
                int numDocs = r.numDocs();

                for ( int i = 0; i < numDocs; i++ )
                {
                    if ( r.isDeleted( i ) )
                    {
                        continue;
                    }

                    Document d = r.document( i );

                    if ( !filter.accept( d ) )
                    {
                        r.deleteDocument( i );
                    }
                }
            }
            finally
            {
                close( r );
            }

            w = new IndexWriter( directory, new SimpleAnalyzer() );

            w.optimize();

            w.flush();
        }
        finally
        {
            close( w );
        }
    }

    public static ArtifactInfo constructArtifactInfo( Document doc, Collection<? extends IndexCreator> ics )
    {
        boolean res = false;

        ArtifactInfo artifactInfo = new ArtifactInfo();
        
        for ( IndexCreator ic : ics )
        {
            res |= ic.updateArtifactInfo( doc, artifactInfo );
        }

        return res ? artifactInfo : null;
    }
    
    // close helpers

    public static void close( OutputStream os )
    {
        if ( os != null )
        {
            try
            {
                os.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public static void close( InputStream is )
    {
        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public static void close( IndexOutput io )
    {
        if ( io != null )
        {
            try
            {
                io.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public static void close( IndexInput in )
    {
        if ( in != null )
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }
    
    public static void close( IndexReader r )
    {
        if ( r != null )
        {
            try
            {
                r.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public static void close( IndexWriter w )
    {
        if ( w != null )
        {
            try
            {
                w.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public static void close( Directory d )
    {
        if ( d != null )
        {
            try
            {
                d.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

}
