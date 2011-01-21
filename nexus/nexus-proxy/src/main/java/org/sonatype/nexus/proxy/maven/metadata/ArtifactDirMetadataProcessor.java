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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.sonatype.nexus.proxy.maven.metadata.operations.AddVersionOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;
import org.sonatype.nexus.proxy.maven.metadata.operations.StringOperand;

/**
 * Process maven metadata in artifactId directory
 * 
 * @author juven
 */
public class ArtifactDirMetadataProcessor
    extends AbstractMetadataProcessor
{
    public ArtifactDirMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        super( metadataHelper );
    }

    @Override
    public void processMetadata( String path )
        throws IOException
    {
        Metadata md = createMetadata( path );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        metadataHelper.store( mdString, path + AbstractMetadataHelper.METADATA_SUFFIX );
    }

    private Metadata createMetadata( String path )
        throws IOException
    {
        try
        {
            Metadata md = new Metadata();

            md.setGroupId( calculateGroupId( path ) );

            md.setArtifactId( calculateArtifactId( path ) );

            versioning( md, metadataHelper.gaData.get( path ) );

            ModelVersionUtility.setModelVersion( md, ModelVersionUtility.LATEST_MODEL_VERSION );

            return md;
        }
        catch ( MetadataException e )
        {
            throw new IOException( e );
        }
    }

    private String calculateGroupId( String path )
    {
        return path.substring( 1, path.lastIndexOf( '/' ) ).replace( '/', '.' );
    }

    private String calculateArtifactId( String path )
    {
        return path.substring( path.lastIndexOf( '/' ) + 1 );
    }

    @Override
    public boolean shouldProcessMetadata( String path )
    {
        Collection<String> versions = metadataHelper.gaData.get( path );

        if ( versions != null && !versions.isEmpty() )
        {
            return true;
        }

        return false;
    }

    void versioning( Metadata metadata, Collection<String> versions )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String version : versions )
        {
            ops.add( new AddVersionOperation( new StringOperand( ModelVersionUtility.LATEST_MODEL_VERSION, version ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gaData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( Metadata oldMd, String path )
        throws IOException
    {
        Metadata md = createMetadata( path );

        if ( oldMd.getVersioning().getRelease() == null )
        {
            oldMd.getVersioning().setRelease( "" );
        }

        if ( md.getVersioning().getRelease() == null )
        {
            md.getVersioning().setRelease( "" );
        }

        if ( oldMd.getVersioning().getLatest() == null )
        {
            return false;
        }

        if ( oldMd.getVersioning().getVersions() == null )
        {
            return false;
        }

        if ( oldMd.getArtifactId().equals( md.getArtifactId() ) && oldMd.getGroupId().equals( md.getGroupId() )
            && oldMd.getVersioning().getLatest().equals( md.getVersioning().getLatest() )
            && oldMd.getVersioning().getRelease().equals( md.getVersioning().getRelease() )
            && oldMd.getVersioning().getVersions().equals( md.getVersioning().getVersions() ) )
        {
            return true;
        }

        return false;
    }

}
