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
