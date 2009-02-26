/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.nexus.index.updater;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An index data reader used to parse transfer index format.
 * 
 * @author Eugene Kuleshov
 */
public class IndexDataReader
{
    private final DataInputStream dis;

    public IndexDataReader( InputStream is )
        throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream( is, 1024 * 8 );
        GZIPInputStream gis = new GZIPInputStream( bis, 2 * 1024 );
        this.dis = new DataInputStream( gis );
    }

    public IndexDataReadResult readIndex( IndexWriter w, IndexingContext context )
        throws IOException
    {
        dis.readByte(); // data format version

        long timestamp = dis.readLong();

        Date date = new Date( timestamp );

        IndexUtils.updateTimestamp( w.getDirectory(), date );

        int n = 0;

        Document doc;
        while ( ( doc = readDocument() ) != null )
        {
            w.addDocument( IndexUtils.updateDocument( doc, context ) );
            
            n++;
        }

        w.flush();
        w.optimize();

        IndexDataReadResult result = new IndexDataReadResult();
        result.setDocumentCount( n );
        result.setTimestamp( date );
        return result;
    }

    public Document readDocument()
        throws IOException
    {
        int fieldCount;
        try
        {
            fieldCount = dis.readInt();
        }
        catch ( EOFException ex )
        {
            return null; // no more documents
        }

        Document doc = new Document();

        for ( int i = 0; i < fieldCount; i++ )
        {
            doc.add( readField() );
        }

        return doc;
    }

    private Field readField()
        throws IOException
    {
        int flags = dis.read();

        Index index = Index.NO;
        if ( ( flags & IndexDataWriter.F_INDEXED ) > 0 )
        {
            boolean isTokenized = ( flags & IndexDataWriter.F_TOKENIZED ) > 0;
            index = isTokenized ? Index.TOKENIZED : Index.UN_TOKENIZED;
        }

        Store store = Store.NO;
        if ( ( flags & IndexDataWriter.F_STORED ) > 0 )
        {
            boolean isCompressed = ( flags & IndexDataWriter.F_COMPRESSED ) > 0;
            store = isCompressed ? Store.COMPRESS : Store.YES;
        }

        String name = dis.readUTF();
        String value = readUTF( dis );

        return new Field( name, value, store, index );
    }

    private final static String readUTF( DataInput in )
        throws IOException
    {
        int utflen = in.readInt();

        byte[] bytearr = new byte[utflen];
        char[] chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        in.readFully( bytearr, 0, utflen );

        while ( count < utflen )
        {
            c = bytearr[count] & 0xff;
            if ( c > 127 )
            {
                break;
            }
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while ( count < utflen )
        {
            c = bytearr[count] & 0xff;
            switch ( c >> 4 )
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;

                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if ( count > utflen )
                    {
                        throw new UTFDataFormatException( "malformed input: partial character at end" );
                    }
                    char2 = bytearr[count - 1];
                    if ( ( char2 & 0xC0 ) != 0x80 )
                    {
                        throw new UTFDataFormatException( "malformed input around byte " + count );
                    }
                    chararr[chararr_count++] = (char) ( ( ( c & 0x1F ) << 6 ) | ( char2 & 0x3F ) );
                    break;

                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if ( count > utflen )
                    {
                        throw new UTFDataFormatException( "malformed input: partial character at end" );
                    }
                    char2 = bytearr[count - 2];
                    char3 = bytearr[count - 1];
                    if ( ( ( char2 & 0xC0 ) != 0x80 ) || ( ( char3 & 0xC0 ) != 0x80 ) )
                    {
                        throw new UTFDataFormatException( "malformed input around byte " + ( count - 1 ) );
                    }
                    chararr[chararr_count++] = (char) ( ( ( c & 0x0F ) << 12 ) | ( ( char2 & 0x3F ) << 6 ) | ( ( char3 & 0x3F ) << 0 ) );
                    break;

                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException( "malformed input around byte " + count );
            }
        }

        // The number of chars produced may be less than utflen
        return new String( chararr, 0, chararr_count );
    }

    /**
     * An index data read result holder
     */
    public static class IndexDataReadResult
    {
        private Date timestamp;

        private int documentCount;

        public void setDocumentCount( int documentCount )
        {
            this.documentCount = documentCount;
        }

        public int getDocumentCount()
        {
            return documentCount;
        }

        public void setTimestamp( Date timestamp )
        {
            this.timestamp = timestamp;
        }

        public Date getTimestamp()
        {
            return timestamp;
        }

    }

}
