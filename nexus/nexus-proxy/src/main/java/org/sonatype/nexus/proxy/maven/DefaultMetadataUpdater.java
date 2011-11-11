/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.maven.metadata.operations.AddPluginOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.AddVersionOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;
import org.sonatype.nexus.proxy.maven.metadata.operations.PluginOperand;
import org.sonatype.nexus.proxy.maven.metadata.operations.SetSnapshotOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.SnapshotOperand;
import org.sonatype.nexus.proxy.maven.metadata.operations.StringOperand;
import org.sonatype.nexus.proxy.maven.metadata.operations.TimeUtil;

@Component( role = MetadataUpdater.class )
public class DefaultMetadataUpdater
    extends AbstractLoggingComponent
    implements MetadataUpdater
{
    @Requirement
    private MetadataLocator locator;

    public void deployArtifact( ArtifactStoreRequest request )
        throws IOException
    {
        if ( request.getGav().isHash() || request.getGav().isSignature()
            || StringUtils.isNotBlank( request.getGav().getClassifier() ) )
        {
            // hashes and signatures are "meta"
            // artifacts with classifiers do not change metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            // GAV metadata is only meaningful to snapshot artifacts
            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand(
                    ModelVersionUtility.getModelVersion( gavMd ), TimeUtil.getUTCTimestamp(),
                    MetadataBuilder.createSnapshot( request.getVersion() ), buildVersioning( gav ) ) ) );

                MetadataBuilder.changeMetadata( gavMd, operations );

                locator.storeGAVMetadata( request, gavMd );
            }

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( ModelVersionUtility.getModelVersion( gaMd ),
                request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( StringUtils.equals( "maven-plugin", locator.retrievePackagingFromPom( request ) ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand(
                        ModelVersionUtility.getModelVersion( gMd ), pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            throw new LocalStorageException( "Not able to apply changes!", e );
        }
    }

    private SnapshotVersion buildVersioning( Gav gav )
    {
        SnapshotVersion version = new SnapshotVersion();
        version.setClassifier( gav.getClassifier() );
        version.setExtension( gav.getExtension() );
        version.setVersion( gav.getVersion() );
        version.setUpdated( TimeUtil.getUTCTimestamp() );
        return version;
    }

    public void undeployArtifact( ArtifactStoreRequest request )
        throws IOException
    {
        if ( request.getGav().isHash() || request.getGav().isSignature()
            || StringUtils.isNotBlank( request.getGav().getClassifier() ) )
        {
            // hashes and signatures are "meta"
            // artifacts with classifiers do not change metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand(
                    ModelVersionUtility.getModelVersion( gavMd ), TimeUtil.getUTCTimestamp(),
                    MetadataBuilder.createSnapshot( request.getVersion() ), buildVersioning( gav ) ) ) );
            }

            MetadataBuilder.changeMetadata( gavMd, operations );

            locator.storeGAVMetadata( request, gavMd );

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( ModelVersionUtility.getModelVersion( gaMd ),
                request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( StringUtils.equals( "maven-plugin", locator.retrievePackagingFromPom( request ) ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand(
                        ModelVersionUtility.getModelVersion( gMd ), pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            throw new LocalStorageException( "Not able to apply changes!", e );
        }
    }

    // ==

    public void deployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void undeployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void recreateMetadata( StorageCollectionItem coll )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
