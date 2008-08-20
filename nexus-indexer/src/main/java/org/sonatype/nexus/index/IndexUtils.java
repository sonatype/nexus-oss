/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamas Cservenak (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.sonatype.nexus.index.context.IndexingContext;

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

                        ii.close();
                    }
                    finally
                    {
                        if ( ii != null )
                        {
                            ii.close();
                        }
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

    public static Date getIndexArchiveTime( InputStream is )
        throws IOException
    {
        ZipInputStream zis = null;
        try
        {
            zis = new ZipInputStream( is );

            long timestamp = -1;

            ZipEntry entry;
            while ( ( entry = zis.getNextEntry() ) != null )
            {
                if ( entry.getName() == IndexUtils.TIMESTAMP_FILE )
                {
                    return new Date( new DataInputStream( zis ).readLong() );
                }
                timestamp = entry.getTime();
            }

            return timestamp == -1 ? null : new Date( timestamp );
        }
        finally
        {
            close( zis );
            close( is );
        }
    }

    /**
     * Unpacks index data into specified Lucene <code>Directory</code>
     * 
     * @param is a <code>ZipInputStream</code> with index data
     * @param directory Lucene <code>Directory</code> to unpack index data to
     * @return {@link Date} of the index update or null if it can't be read
     */
    public static Date unpackIndexArchive( InputStream is, Directory directory )
        throws IOException
    {

        ZipInputStream zis = new ZipInputStream( is );
        try
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
        finally
        {
            close( zis );
        }

        return IndexUtils.getTimestamp( directory );
    }

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

            int haveRead = 0;

            int len = (int) in.length();

            while ( haveRead != len )
            {
                toRead = ( len - haveRead > buf.length ) ? buf.length : len - haveRead;

                in.readBytes( buf, 0, toRead, false );

                zos.write( buf, 0, toRead );

                haveRead = haveRead + toRead;
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
                r.close();
            }

            w = new IndexWriter( directory, new SimpleAnalyzer() );

            w.optimize();

            w.flush();
        }
        finally
        {
            w.close();
        }
    }

    //

    public static void copyAll( final InputStream input, final OutputStream output, byte[] buffer )
        throws IOException
    {
        int n = 0;

        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }

    private static void close( OutputStream os )
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

    private static void close( InputStream is )
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

    private static void close( IndexOutput io )
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

    private static void close( IndexInput in )
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

}
