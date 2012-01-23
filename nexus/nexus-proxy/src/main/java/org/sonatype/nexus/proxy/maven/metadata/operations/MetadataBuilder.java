/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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
        throws IOException
    {
        try
        {
            return new MetadataXpp3Reader().read( in );
        }
        catch ( NullPointerException e )
        {
            // XPP3 parser throws NPE on some malformed XMLs
            throw new IOException( "Malformed XML!", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( e );
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
        throws IOException
    {
        if ( metadata == null )
        {
            return metadata;
        }

        new MetadataXpp3Writer().write( WriterFactory.newXmlWriter( out ), metadata );

        return metadata;
    }

    /**
     * apply a list of operators to the specified serialized Metadata object
     * 
     * @param metadataBytes - serialized Metadata object
     * @param mutators - operators
     * @return changed serialized object
     * @throws MetadataException
     */
    public static void changeMetadata( Metadata metadata, List<MetadataOperation> mutators )
        throws MetadataException
    {
        boolean changed = false;

        if ( metadata == null )
        {
            metadata = new Metadata();
        }

        // Uncomment these once the fixes are in place
        // Version mdModelVersion = ModelVersionUtility.getModelVersion( metadata );

        if ( mutators != null && mutators.size() > 0 )
        {
            boolean currentChanged = false;

            for ( MetadataOperation op : mutators )
            {
                currentChanged = op.perform( metadata );

                // if (currentChanged) {
                // mdModelVersion = max of mdModelVersion and op.getModelVersion;
                // }

                changed = currentChanged || changed;
            }
        }

        // ModelVersionUtility.setModelVersion( metadata, mdModelVersion );
    }

    public static void changeMetadata( Metadata metadata, MetadataOperation op )
        throws MetadataException
    {
        changeMetadata( metadata, Collections.singletonList( op ) );
    }

    public static void changeMetadata( Metadata metadata, MetadataOperation... ops )
        throws MetadataException
    {
        changeMetadata( metadata, Arrays.asList( ops ) );
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

        if ( pos == -1 )
        {
            throw new IllegalArgumentException();
        }

        String sbn = version.substring( pos + 1 );

        int bn = Integer.parseInt( sbn );
        sn.setBuildNumber( bn );

        String sts = version.substring( 0, pos );
        pos = sts.lastIndexOf( '-' );

        if ( pos == -1 )
        {
            throw new IllegalArgumentException();
        }

        sn.setTimestamp( sts.substring( pos + 1 ) );

        return sn;
    }

}
