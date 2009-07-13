package org.sonatype.nexus.proxy.maven.metadata.mercury;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.AddPluginOperation;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperand;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.PluginOperand;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.StringUtils;

/**
 * Actually it's a fixed version of org.apache.maven.mercury.repository.metadata.MergeOperation
 * 
 * @author juven
 */
public class MergeOperation
    implements MetadataOperation
{
    private static final Language LANG = new DefaultLanguage( MergeOperation.class );

    private Metadata sourceMetadata;

    public MergeOperation( MetadataOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    /**
     * always return true (known issue)
     */
    @SuppressWarnings( "unchecked" )
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
        for ( Plugin plugin : (List<Plugin>) sourceMetadata.getPlugins() )
        {
            ops.add( new AddPluginOperation( new PluginOperand( plugin ) ) );
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
            for ( String version : (List<String>) sourceMetadata.getVersioning().getVersions() )
            {
                ops.add( new AddVersionOperation( new StringOperand( version ) ) );
            }

            // versioning.snapshot
            // use the snapshot with highest build number
            Snapshot sourceSnapshot = sourceMetadata.getVersioning().getSnapshot();

            if ( sourceSnapshot != null )
            {
                int buildNumber = -1;

                if ( targetMetadata.getVersioning() != null && targetMetadata.getVersioning().getSnapshot() != null )
                {
                    buildNumber = targetMetadata.getVersioning().getSnapshot().getBuildNumber();
                }

                if ( sourceSnapshot.getBuildNumber() > buildNumber )
                {
                    ops.add( new SetSnapshotOperation( new SnapshotOperand( sourceSnapshot ) ) );
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

    public void setOperand( Object data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof MetadataOperand ) )
        {
            throw new MetadataException( LANG.getMessage( "bad.operand", "MetadataOperand", data == null ? "null"
                            : data.getClass().getName() ) );
        }

        sourceMetadata = ( (MetadataOperand) data ).getOperand();
    }

    // ==

    protected boolean hasLastUpdatedSet( Metadata md )
    {
        return md.getVersioning() != null && StringUtils.isNotBlank( md.getVersioning().getLastUpdated() );
    }
}
