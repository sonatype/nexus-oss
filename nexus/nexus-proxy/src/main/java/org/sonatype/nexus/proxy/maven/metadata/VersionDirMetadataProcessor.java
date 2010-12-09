package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.apache.maven.index.artifact.M2GavCalculator;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.SetSnapshotOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.SnapshotOperand;
import org.sonatype.nexus.proxy.maven.metadata.operations.TimeUtil;

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

        versioning( md, getGavs( path, metadataHelper.gavData.get( path ) ) );

        return md;
    }

    private Collection<Gav> getGavs( String path, Collection<String> items )
        throws IllegalArtifactCoordinateException
    {
        if ( !path.endsWith( "/" ) )
        {
            path = path + "/";
        }
        M2GavCalculator calc = new M2GavCalculator();

        List<Gav> gavs = new ArrayList<Gav>();
        for ( String item : items )
        {
            Gav gav = calc.pathToGav( path + item );
            gavs.add( gav );
        }

        return gavs;
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

    void versioning( Metadata metadata, Collection<Gav> artifactNames )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( Gav gav : artifactNames )
        {
            ops.add( new SetSnapshotOperation( new SnapshotOperand( buildSnapshot( gav ), buildVersion( gav ) ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    private SnapshotVersion[] buildVersion( Gav gav )
        throws MetadataException
    {
        if ( gav.getBaseVersion().equals( gav.getVersion() ) )
        {
            return new SnapshotVersion[0];
        }

        SnapshotVersion snap = new SnapshotVersion();
        snap.setClassifier( gav.getClassifier() );
        snap.setExtension( gav.getExtension() );
        snap.setVersion( gav.getVersion() );

        Snapshot timestamp = buildSnapshot( gav );
        if ( timestamp != null )
        {
            snap.setUpdated( timestamp.getTimestamp().replace( ".", "" ) );
        }
        else
        {
            snap.setUpdated( TimeUtil.getUTCTimestamp().replace( ".", "" ) );
        }
        return new SnapshotVersion[] { snap };
    }

    private Snapshot buildSnapshot( Gav gav )
    {
        Snapshot result = new Snapshot();

        final String version = gav.getVersion();

        if ( version.equals( gav.getBaseVersion() ) )
        {
            return null;
        }

        int lastHyphenPos = version.lastIndexOf( '-' );

        int buildNumber = Integer.parseInt( version.substring( lastHyphenPos + 1 ) );

        String timestamp = version.substring( gav.getBaseVersion().length() - 8, lastHyphenPos );

        result.setLocalCopy( false );

        result.setBuildNumber( buildNumber );

        result.setTimestamp( timestamp );

        return result;
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gavData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( Metadata oldMd, String path )
        throws Exception
    {
        if ( oldMd.getArtifactId() == null || oldMd.getGroupId() == null || oldMd.getVersion() == null
            || oldMd.getVersioning() == null || oldMd.getVersioning().getSnapshot() == null
            || oldMd.getVersioning().getSnapshot().getTimestamp() == null )
        {
            return false;
        }

        Metadata md = createMetadata( path );

        if ( StringUtils.equals( oldMd.getArtifactId(), md.getArtifactId() )
            && StringUtils.equals( oldMd.getGroupId(), md.getGroupId() )
            && StringUtils.equals( oldMd.getVersion(), md.getVersion() )
            && md.getVersioning() != null
            && md.getVersioning().getSnapshot() != null
            && StringUtils.equals( oldMd.getVersioning().getSnapshot().getTimestamp(),
                md.getVersioning().getSnapshot().getTimestamp() )
            && oldMd.getVersioning().getSnapshot().getBuildNumber() == md.getVersioning().getSnapshot().getBuildNumber() )
        {
            return true;
        }

        return false;
    }

}
