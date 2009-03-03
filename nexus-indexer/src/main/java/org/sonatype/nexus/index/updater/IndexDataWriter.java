/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.nexus.index.updater;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An index data writer used to write transfer index format.
 * 
 * @author Eugene Kuleshov
 */
public class IndexDataWriter
{
    static final int VERSION = 1;

    static final int F_INDEXED = 1;

    static final int F_TOKENIZED = 2;

    static final int F_STORED = 4;

    static final int F_COMPRESSED = 8;

    private DataOutputStream dos;

    private GZIPOutputStream gos;

    private BufferedOutputStream bos;

    public IndexDataWriter( OutputStream os )
        throws IOException
    {
        bos = new BufferedOutputStream( os, 1024 * 8 );
        gos = new GZIPOutputStream( bos, 1024 * 2 );
        dos = new DataOutputStream( gos );
    }

    public int write( IndexingContext context, List<Integer> docIndexes )
        throws IOException
    {
        writeHeader( context );

        int n = writeDocuments( context.getIndexReader(), docIndexes );

        close();

        return n;
    }

    public void close()
        throws IOException
    {
        dos.flush();

        gos.flush();
        gos.finish();

        bos.flush();
    }

    public void writeHeader( IndexingContext context )
        throws IOException
    {
        dos.writeByte( VERSION );
        
        Date timestamp = context.getTimestamp();
        dos.writeLong( timestamp == null ? -1 : timestamp.getTime() );
    }

    public int writeDocuments( IndexReader r, List<Integer> docIndexes )
        throws IOException
    {
        int n = 0;

        if ( docIndexes == null )
        {
            for ( int i = 0; i < r.numDocs(); i++ )
            {
                if ( !r.isDeleted( i ) )
                {
                    writeDocument( r.document( i ) );
                    n++;
                }
            }
        }
        else
        {
            for ( int i : docIndexes )
            {
                if ( !r.isDeleted( i ) )
                {
                    writeDocument( r.document( i ) );
                    n++;
                }
            }
        }

        return n;
    }

    public void writeDocument( Document document )
        throws IOException
    {
        @SuppressWarnings( "unchecked" )
        List<Field> fields = document.getFields();

        int fieldCount = 0;

        for ( Field field : fields )
        {
            if ( field.isStored() )
            {
                fieldCount++;
            }
        }

        dos.writeInt( fieldCount );

        for ( Field field : fields )
        {
            if ( field.isStored() )
            {
                writeField( field );
            }
        }
    }

    public void writeField( Field field )
        throws IOException
    {
        int flags = ( field.isIndexed() ? F_INDEXED : 0 ) //
            + ( field.isTokenized() ? F_TOKENIZED : 0 ) //
            + ( field.isStored() ? F_STORED : 0 ) //
            + ( field.isCompressed() ? F_COMPRESSED : 0 );

        String name = field.name();
        String value = field.stringValue();

        dos.write( flags );
        dos.writeUTF( name );
        writeUTF( value, dos );
    }

    private final static void writeUTF( String str, DataOutput out )
        throws IOException
    {
        int strlen = str.length();
        int utflen = 0;
        int c;

        // use charAt instead of copying String to char array
        for ( int i = 0; i < strlen; i++ )
        {
            c = str.charAt( i );
            if ( ( c >= 0x0001 ) && ( c <= 0x007F ) )
            {
                utflen++;
            }
            else if ( c > 0x07FF )
            {
                utflen += 3;
            }
            else
            {
                utflen += 2;
            }
        }

        // TODO optimize storing int value
        out.writeInt( utflen );

        byte[] bytearr = new byte[utflen];

        int count = 0;

        int i = 0;
        for ( ; i < strlen; i++ )
        {
            c = str.charAt( i );
            if ( !( ( c >= 0x0001 ) && ( c <= 0x007F ) ) )
            {
                break;
            }
            bytearr[count++] = (byte) c;
        }

        for ( ; i < strlen; i++ )
        {
            c = str.charAt( i );
            if ( ( c >= 0x0001 ) && ( c <= 0x007F ) )
            {
                bytearr[count++] = (byte) c;

            }
            else if ( c > 0x07FF )
            {
                bytearr[count++] = (byte) ( 0xE0 | ( ( c >> 12 ) & 0x0F ) );
                bytearr[count++] = (byte) ( 0x80 | ( ( c >> 6 ) & 0x3F ) );
                bytearr[count++] = (byte) ( 0x80 | ( ( c >> 0 ) & 0x3F ) );
            }
            else
            {
                bytearr[count++] = (byte) ( 0xC0 | ( ( c >> 6 ) & 0x1F ) );
                bytearr[count++] = (byte) ( 0x80 | ( ( c >> 0 ) & 0x3F ) );
            }
        }

        out.write( bytearr, 0, utflen );
    }

}
