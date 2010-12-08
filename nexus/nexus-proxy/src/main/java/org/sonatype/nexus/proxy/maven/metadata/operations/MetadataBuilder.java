/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.mercury.util.TimeUtil;
import org.codehaus.plexus.util.WriterFactory;

/**
 * utility class to help with de/serializing metadata from/to XML
 * 
 * @author Oleg Gusakov
 * @version $Id: MetadataBuilder.java 740889 2009-02-04 21:13:29Z ogusakov $
 */
public class MetadataBuilder
{
    /**
     * instantiate Metadata from a stream
     * 
     * @param in
     * @return
     * @throws MetadataException
     */
    public static Metadata read( InputStream in )
        throws MetadataException
    {
        try
        {
            return new MetadataXpp3Reader().read( in );
        }
        catch ( Exception e )
        {
            throw new MetadataException( e );
        }
    }

    /**
     * instantiate Metadata from a byte array
     * 
     * @param in
     * @return
     * @throws MetadataException
     */
    public static Metadata getMetadata( byte[] in )
        throws MetadataException
    {
        if ( in == null || in.length < 10 )
        {
            return null;
        }

        try
        {
            return new MetadataXpp3Reader().read( new ByteArrayInputStream( in ) );
        }
        catch ( Exception e )
        {
            throw new MetadataException( e );
        }
    }

    /**
     * serialize metadata into xml
     * 
     * @param metadata to serialize
     * @param out output to this stream
     * @return same metadata as was passed in
     * @throws MetadataException if any problems occurred
     */
    public static Metadata write( Metadata metadata, OutputStream out )
        throws MetadataException
    {
        if ( metadata == null )
        {
            return metadata;
        }

        try
        {
            new MetadataXpp3Writer().write( WriterFactory.newXmlWriter( out ), metadata );

            return metadata;
        }
        catch ( Exception e )
        {
            throw new MetadataException( e );
        }
    }

    /**
     * apply a list of operators to the specified serialized Metadata object
     * 
     * @param metadataBytes - serialized Metadata object
     * @param mutators - operators
     * @return changed serialized object
     * @throws MetadataException
     */
    public static byte[] changeMetadata( byte[] metadataBytes, List<MetadataOperation> mutators )
        throws MetadataException
    {
        if ( mutators == null || mutators.size() < 1 )
        {
            return metadataBytes;
        }

        Metadata metadata;
        boolean changed = false;

        if ( metadataBytes == null || metadataBytes.length < 10 )
        {
            metadata = new Metadata();
        }
        else
        {
            ByteArrayInputStream in = new ByteArrayInputStream( metadataBytes );
            metadata = read( in );
        }

        for ( MetadataOperation op : mutators )
        {
            changed = op.perform( metadata ) || changed;
        }

        // TODO og: does not work - check
        // if( !changed )
        // return metadataBytes;

        return getBytes( metadata );
    }

    /**
     * marshall metadata into a byte array
     * 
     * @param metadata
     * @return
     * @throws MetadataException
     */
    public static byte[] getBytes( Metadata metadata )
        throws MetadataException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write( metadata, out );

        byte[] res = out.toByteArray();

        return res;
    }

    /**
     * apply a list of operators to the specified serialized Metadata object
     * 
     * @param metadataBytes - serialized Metadata object
     * @param mutators - operators
     * @return changed serialized object
     * @throws MetadataException
     */
    public static byte[] changeMetadata( Metadata metadata, List<MetadataOperation> mutators )
        throws MetadataException
    {

        boolean changed = false;

        if ( metadata == null )
        {
            metadata = new Metadata();
        }

        if ( mutators != null && mutators.size() > 0 )
        {
            for ( MetadataOperation op : mutators )
            {
                changed = op.perform( metadata ) || changed;
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write( metadata, out );

        byte[] res = out.toByteArray();

        return res;
    }

    public static byte[] changeMetadata( byte[] metadataBytes, MetadataOperation op )
        throws MetadataException
    {
        ArrayList<MetadataOperation> ops = new ArrayList<MetadataOperation>( 1 );
        ops.add( op );

        return changeMetadata( metadataBytes, ops );
    }

    public static byte[] changeMetadata( Metadata metadata, MetadataOperation op )
        throws MetadataException
    {
        ArrayList<MetadataOperation> ops = new ArrayList<MetadataOperation>( 1 );
        ops.add( op );

        return changeMetadata( metadata, ops );
    }

    /**
     * update snapshot timestamp to now
     * 
     * @param target
     */
    public static void updateTimestamp( Snapshot target )
    {
        target.setTimestamp( TimeUtil.getUTCTimestamp() );
    }

    /**
     * update versioning's lastUpdated timestamp to now
     * 
     * @param target
     */
    public static void updateTimestamp( Versioning target )
    {
        target.setLastUpdated( TimeUtil.getUTCTimestamp() );
    }

    public static Snapshot createSnapshot( String version )
    {
        Snapshot sn = new Snapshot();

        if ( version == null || version.length() < 3 )
        {
            return sn;
        }

        String utc = TimeUtil.getUTCTimestamp();
        sn.setTimestamp( utc );

        if ( version.endsWith( "-SNAPSHOT" ) )
        {
            return sn;
        }

        int pos = version.lastIndexOf( '-' );
        
        if( pos == -1 )
        {
            throw new IllegalArgumentException();
        }

        String sbn = version.substring( pos + 1 );
        
        int bn = Integer.parseInt( sbn );
        sn.setBuildNumber( bn );
        
        String sts = version.substring( 0, pos);
        pos = sts.lastIndexOf( '-' );
        
        if( pos == -1 )
        {
            throw new IllegalArgumentException();
        }
        
        sn.setTimestamp( sts.substring( pos+1 ) );

        return sn;
    }

}
