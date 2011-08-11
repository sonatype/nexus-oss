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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility.Version;

public class NexusMergeOperation
    implements MetadataOperation
{
    private Metadata sourceMetadata;

    private Version sourceModelVersion;

    public NexusMergeOperation( final MetadataOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    /**
     * always return true (known issue)
     */
    public boolean perform( Metadata targetMetadata )
        throws MetadataException
    {
        if ( sourceMetadata == null || targetMetadata == null )
        {
            return false;
        }

        // we first record the versioning.lastUpdated since it might be changed by MetadataOpersions
        String lastUpdated = null;

        if ( !hasLastUpdatedSet( sourceMetadata ) && !hasLastUpdatedSet( targetMetadata ) )
        {
            // neither has set, set it to now
            lastUpdated = Long.toString( System.currentTimeMillis() );
        }
        else if ( !hasLastUpdatedSet( sourceMetadata ) && hasLastUpdatedSet( targetMetadata ) )
        {
            lastUpdated = targetMetadata.getVersioning().getLastUpdated();
        }
        else if ( !hasLastUpdatedSet( targetMetadata ) && hasLastUpdatedSet( sourceMetadata ) )
        {
            lastUpdated = sourceMetadata.getVersioning().getLastUpdated();
        }
        else if ( hasLastUpdatedSet( targetMetadata ) && hasLastUpdatedSet( sourceMetadata ) )
        {
            long sourceLU = -1;

            long targetLU = -1;

            try
            {
                sourceLU = Long.parseLong( sourceMetadata.getVersioning().getLastUpdated() );
            }
            catch ( NumberFormatException e )
            {
                // nothing, bad metadata
                // TODO: we should do something here, but surely not die
            }

            try
            {
                targetLU = Long.parseLong( targetMetadata.getVersioning().getLastUpdated() );
            }
            catch ( NumberFormatException e )
            {
                // nothing, bad metadata
                // TODO: we should do something here, but surely not die
            }

            lastUpdated = sourceLU >= targetLU ? Long.toString( sourceLU ) : Long.toString( targetLU );
        }

        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        // plugins
        for ( Plugin plugin : sourceMetadata.getPlugins() )
        {
            ops.add( new AddPluginOperation( new PluginOperand( sourceModelVersion, plugin ) ) );
        }

        // gav
        if ( targetMetadata.getGroupId() == null )
        {
            targetMetadata.setGroupId( sourceMetadata.getGroupId() );
        }
        if ( targetMetadata.getArtifactId() == null )
        {
            targetMetadata.setArtifactId( sourceMetadata.getArtifactId() );
        }
        if ( targetMetadata.getVersion() == null )
        {
            targetMetadata.setVersion( sourceMetadata.getVersion() );
        }

        if ( sourceMetadata.getGroupId() != null && targetMetadata.getGroupId() != null
            && !sourceMetadata.getGroupId().equals( targetMetadata.getGroupId() ) )
        {
            throw new MetadataException( "Could not merge metadata with different groupId: '"
                + sourceMetadata.getGroupId() + "' and '" + targetMetadata.getGroupId() + "'" );
        }
        if ( sourceMetadata.getArtifactId() != null && targetMetadata.getArtifactId() != null
            && !sourceMetadata.getArtifactId().equals( targetMetadata.getArtifactId() ) )
        {
            throw new MetadataException( "Could not merge metadata with different artifactId: '"
                + sourceMetadata.getArtifactId() + "' and '" + targetMetadata.getArtifactId() + "'" );
        }

        // versioning

        if ( sourceMetadata.getVersioning() != null )
        {
            // versioning.verions
            // merge all versions together
            for ( String version : sourceMetadata.getVersioning().getVersions() )
            {
                ops.add( new AddVersionOperation( new StringOperand( sourceModelVersion, version ) ) );
            }

            // versioning.snapshot
            // use the snapshot with newest timestamp
            Snapshot sourceSnapshot = sourceMetadata.getVersioning().getSnapshot();

            if ( sourceSnapshot != null )
            {
                long timestamp = -1;

                if ( targetMetadata.getVersioning() != null && targetMetadata.getVersioning().getSnapshot() != null
                    && targetMetadata.getVersioning().getSnapshot().getTimestamp() != null )
                {
                    try
                    {
                        timestamp =
                            Long.parseLong( targetMetadata.getVersioning().getSnapshot().getTimestamp().replace( ".",
                                "" ) );
                    }
                    catch ( NumberFormatException e )
                    {
                    }
                }

                if ( sourceSnapshot.getTimestamp() != null )
                {
                    long sourceTimestamp = -1;

                    try
                    {
                        sourceTimestamp = Long.parseLong( sourceSnapshot.getTimestamp().replace( ".", "" ) );
                    }
                    catch ( NumberFormatException e )
                    {
                    }

                    if ( sourceTimestamp > timestamp )
                    {
                        ops.add( new SetSnapshotOperation( new SnapshotOperand( sourceModelVersion,
                            sourceSnapshot.getTimestamp().replace( ".", "" ), sourceSnapshot,
                            sourceMetadata.getVersioning().getSnapshotVersions() ) ) );
                    }
                }
            }
        }

        MetadataBuilder.changeMetadata( targetMetadata, ops );

        // versioning.lastUpdate
        // choose the latest
        if ( targetMetadata.getVersioning() != null && lastUpdated != null )
        {
            targetMetadata.getVersioning().setLastUpdated( lastUpdated );
        }

        return true;
    }

    public void setOperand( AbstractOperand data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof MetadataOperand ) )
        {
            throw new MetadataException( "Operand is not correct: expected MetadataOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );

        }

        sourceMetadata = ( (MetadataOperand) data ).getOperand();

        sourceModelVersion = ( (MetadataOperand) data ).getOriginModelVersion();
    }

    // ==

    protected boolean hasLastUpdatedSet( Metadata md )
    {
        return md.getVersioning() != null && StringUtils.isNotBlank( md.getVersioning().getLastUpdated() );
    }
}
