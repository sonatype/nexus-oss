package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.StringOperand;

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

        versioning( md, metadataHelper.gaData.get( path ) );

        return md;
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
            ops.add( new AddVersionOperation( new StringOperand( version ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gaData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( String path )
        throws Exception
    {
        Metadata oldMd = readMetadata( path );

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
