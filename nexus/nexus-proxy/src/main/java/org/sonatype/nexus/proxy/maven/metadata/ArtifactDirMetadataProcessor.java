package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.codehaus.plexus.util.StringUtils;

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

        md.setGroupId( metadataHelper.currentGroupId );

        md.setArtifactId( metadataHelper.currentArtifactId );

        versioning( md, metadataHelper.currentVersions );

        return md;
    }

    public boolean isPathMatched( String path )
    {
        if ( StringUtils.isEmpty( metadataHelper.currentGroupId )
            || StringUtils.isEmpty( metadataHelper.currentArtifactId ) )
        {
            return false;
        }
        if ( ( "/" + metadataHelper.currentGroupId.replace( '.', '/' ) + "/" + metadataHelper.currentArtifactId )
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

        if ( !metadataHelper.currentVersions.isEmpty() )
        {
            return true;
        }
        return false;
    }

    void versioning( Metadata metadata, List<String> versions )
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
    public void postProcessMetadata()
    {
        metadataHelper.currentVersions.clear();
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
