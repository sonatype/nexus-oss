/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;

/**
 * @author juven
 */
public abstract class AbstractMetadataProcessor
{
    protected static final String METADATA_SUFFIX = "/maven-metadata.xml";

    protected AbstractMetadataHelper metadataHelper;

    public AbstractMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        this.metadataHelper = metadataHelper;
    }

    /**
     * @param path
     * @return true if got processed, else false
     * @throws Exception
     */
    public boolean process( String path )
        throws IOException
    {
        if ( !shouldProcessMetadata( path ) )
        {
            return false;
        }

        if ( isMetadataExisted( path ) )
        {
            Metadata metadata = readMetadata( path );

            if ( metadata != null && isMetadataCorrect( metadata, path ) )
            {
                postProcessMetadata( path );

                return true;
            }
            else
            {
                removedMetadata( path );
            }
        }

        processMetadata( path );

        postProcessMetadata( path );

        buildMetadataChecksum( path );

        return true;

    }

    protected boolean isMetadataExisted( String path )
        throws IOException
    {
        return metadataHelper.exists( path + METADATA_SUFFIX );
    }

    protected Metadata readMetadata( String path )
        throws IOException
    {
        InputStream mdStream = metadataHelper.retrieveContent( path + METADATA_SUFFIX );

        try
        {
            Metadata md = MetadataBuilder.read( mdStream );

            return md;
        }
        catch ( IOException e )
        {
            if ( metadataHelper.logger.isDebugEnabled() )
            {
                metadataHelper.logger.info( "Failed to parse metadata from '" + path + "'", e );
            }
            else
            {
                metadataHelper.logger.info( "Failed to parse metadata from '" + path + "'" );
            }

            return null;
        }
        finally
        {
            IOUtil.close( mdStream );
        }
    }

    protected void removedMetadata( String path )
        throws IOException
    {
        metadataHelper.remove( path + METADATA_SUFFIX );
    }

    protected void buildMetadataChecksum( String path )
        throws IOException
    {
        metadataHelper.rebuildChecksum( path + METADATA_SUFFIX );
    }

    protected abstract boolean isMetadataCorrect( Metadata metadata, String path )
        throws IOException;

    protected abstract boolean shouldProcessMetadata( String path );

    protected abstract void processMetadata( String path )
        throws IOException;

    protected abstract void postProcessMetadata( String path );
}
