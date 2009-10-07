package org.apache.maven.mercury.repository.metadata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.StringUtils;

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
            // use the snapshot with newest timestamp
            Snapshot sourceSnapshot = sourceMetadata.getVersioning().getSnapshot();

            if ( sourceSnapshot != null )
            {
                long timestamp = -1;

                if ( targetMetadata.getVersioning() != null 
                    && targetMetadata.getVersioning().getSnapshot() != null 
                    && targetMetadata.getVersioning().getSnapshot().getTimestamp() != null )
                {
                    try
                    {
                        timestamp = Long.parseLong( targetMetadata.getVersioning().getSnapshot().getTimestamp().replace( ".", "" ) );
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
                        ops.add( new SetSnapshotOperation( new SnapshotOperand( sourceSnapshot ) ) );    
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
