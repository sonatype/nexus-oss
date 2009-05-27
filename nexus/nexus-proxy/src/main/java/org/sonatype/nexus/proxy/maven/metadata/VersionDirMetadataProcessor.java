package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.StringOperand;

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

    @Override
    public boolean shouldProcessMetadata( String path )
    {

        Collection<String> names = metadataHelper.gavData.get( path );

        if ( names != null && !names.isEmpty() )
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

        md.setGroupId( calculateGroupId( path ) );

        md.setArtifactId( calculateArtifactId( path ) );

        md.setVersion( calculateVersion( path ) );

        versioning( md, metadataHelper.gavData.get( path ) );

        return md;
    }

    private String calculateGroupId( String path )
    {
        String gaPath = path.substring( 0, path.lastIndexOf( '/' ) );

        return gaPath.substring( 1, gaPath.lastIndexOf( '/' ) ).replace( '/', '.' );
    }

    private String calculateArtifactId( String path )
    {
        String gaPath = path.substring( 0, path.lastIndexOf( '/' ) );

        return gaPath.substring( gaPath.lastIndexOf( '/' ) + 1 );
    }

    private String calculateVersion( String path )
    {
        return path.substring( path.lastIndexOf( '/' ) + 1 );
    }

    void versioning( Metadata metadata, Collection<String> artifactNames )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String artifactName : artifactNames )
        {
            ops.add( new SetSnapshotOperation( new StringOperand( artifactName ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gavData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( String path )
        throws Exception
    {
        Metadata oldMd = readMetadata( path );

        if ( oldMd.getArtifactId() == null || oldMd.getGroupId() == null || oldMd.getVersion() == null
            || oldMd.getVersioning() == null || oldMd.getVersioning().getSnapshot() == null
            || oldMd.getVersioning().getSnapshot().getTimestamp() == null )
        {
            return false;
        }

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
