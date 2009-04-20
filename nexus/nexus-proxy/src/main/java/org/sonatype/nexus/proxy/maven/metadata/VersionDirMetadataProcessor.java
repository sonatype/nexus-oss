package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.codehaus.plexus.util.StringUtils;

/**
 * Process maven metadata in snapshot version directory
 * 
 * @author juven
 */
public class VersionDirMetadataProcessor
    extends AbstractMetadataProcessor
{
    public VersionDirMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        super( metadataHelper );
    }

    public boolean isPathMatched( String path )
    {
        if ( StringUtils.isEmpty( metadataHelper.currentGroupId )
            || StringUtils.isEmpty( metadataHelper.currentArtifactId )
            || StringUtils.isEmpty( metadataHelper.currentVersion ) )
        {
            return false;
        }
        if ( ( "/" + metadataHelper.currentGroupId.replace( '.', '/' ) + "/" + metadataHelper.currentArtifactId + "/" + metadataHelper.currentVersion )
            .equals( path ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldProcessMetadata( String path )
    {
        if ( !isPathMatched( path ) )
        {
            return false;
        }

        if ( !metadataHelper.currentArtifacts.isEmpty() && metadataHelper.currentVersion.endsWith( "SNAPSHOT" ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public void processMetadata( String path )
        throws Exception
    {
        Metadata md = createMetadata( path );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        metadataHelper.store( mdString, path + AbstractMetadataHelper.METADATA_SUFFIX );
    }

    private Metadata createMetadata( String path )
        throws Exception
    {
        Metadata md = new Metadata();

        md.setGroupId( metadataHelper.currentGroupId );

        md.setArtifactId( metadataHelper.currentArtifactId );

        md.setVersion( metadataHelper.currentVersion );

        versioning( md, metadataHelper.currentArtifacts );

        return md;
    }

    void versioning( Metadata metadata, List<String> artifacts )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String artifact : artifacts )
        {
            ops.add( new SetSnapshotOperation( new StringOperand( getName( artifact ) ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    @Override
    public void postProcessMetadata()
    {
        metadataHelper.currentArtifacts.clear();
    }

    @Override
    protected boolean isMetadataCorrect( String path )
        throws Exception
    {
        Metadata oldMd = readMetadata( path );

        Metadata md = createMetadata( path );

        if ( oldMd.getArtifactId().equals( md.getArtifactId() )
            && oldMd.getGroupId().equals( md.getGroupId() )
            && oldMd.getVersion().equals( md.getVersion() )
            && oldMd.getVersioning().getSnapshot().getTimestamp().equals(
                md.getVersioning().getSnapshot().getTimestamp() )
            && oldMd.getVersioning().getSnapshot().getBuildNumber() == md
                .getVersioning().getSnapshot().getBuildNumber() )
        {
            return true;
        }

        return false;
    }

}
