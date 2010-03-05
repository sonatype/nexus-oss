package org.sonatype.nexus.proxy.maven.metadata.mercury;

import java.util.Arrays;

import org.apache.maven.mercury.repository.metadata.NexusMergeOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataOperand;
import org.apache.maven.mercury.repository.metadata.Versioning;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.StringUtils;

public class MergeOperationTest
    extends PlexusTestCase
{
    public void testMergeNoLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( false );

        Metadata md2 = getTarget( false );

        NexusMergeOperation mergeOp = new NexusMergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, false, false );
    }

    public void testMergeTargetLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( false );

        Metadata md2 = getTarget( true );

        NexusMergeOperation mergeOp = new NexusMergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, true );
    }

    public void testMergeSourceLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( true );

        Metadata md2 = getTarget( false );

        NexusMergeOperation mergeOp = new NexusMergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, false );
    }

    public void testMergeBothLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( true );

        Metadata md2 = getTarget( true );

        NexusMergeOperation mergeOp = new NexusMergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, true );
    }

    public void testMergeReleaseAndSnapshot()
        throws Exception
    {
        Metadata release = getReleaseMetadata();
        Metadata snapshot = getSnapshotMetadata();
        NexusMergeOperation mergeOp = new NexusMergeOperation( new MetadataOperand( release ) );
        mergeOp.perform( snapshot );
        
        //check the snapshot metadata, which should now be merged
        assertEquals( "test", snapshot.getArtifactId() );
        assertEquals( "test", snapshot.getGroupId() );
        assertTrue( snapshot.getPlugins().isEmpty() );
        assertNull( snapshot.getVersion() );
        assertNotNull( snapshot.getVersioning() );
        assertEquals( "1234568", snapshot.getVersioning().getLastUpdated() );
        assertEquals( "1.2-SNAPSHOT", snapshot.getVersioning().getLatest() );
        assertEquals( "1.1", snapshot.getVersioning().getRelease() );
        assertNull( snapshot.getVersioning().getSnapshot() );
        assertNotNull( snapshot.getVersioning().getVersions() );
        assertTrue( snapshot.getVersioning().getVersions().containsAll( Arrays.asList( "1.1", "1.1-SNAPSHOT", "1.2-SNAPSHOT" ) ) );
        
        //now do the merge in reverse
        release = getReleaseMetadata();
        snapshot = getSnapshotMetadata();
        mergeOp = new NexusMergeOperation( new MetadataOperand( snapshot ) );
        mergeOp.perform( release );
        
        //check the release metadata, which should now be merged
        assertEquals( "test", release.getArtifactId() );
        assertEquals( "test", release.getGroupId() );
        assertTrue( release.getPlugins().isEmpty() );
        assertNull( release.getVersion() );
        assertNotNull( release.getVersioning() );
        assertEquals( "1234568", release.getVersioning().getLastUpdated() );
        assertEquals( "1.2-SNAPSHOT", release.getVersioning().getLatest() );
        assertEquals( "1.1", release.getVersioning().getRelease() );
        assertNull( release.getVersioning().getSnapshot() );
        assertNotNull( release.getVersioning().getVersions() );
        assertTrue( release.getVersioning().getVersions().containsAll( Arrays.asList( "1.1", "1.1-SNAPSHOT", "1.2-SNAPSHOT" ) ) );
    }
    
    private Metadata getReleaseMetadata()
    {
        Metadata releaseMetadata = new Metadata();
        releaseMetadata.setArtifactId( "test" );
        releaseMetadata.setGroupId( "test" );
        
        Versioning versioning = new Versioning();
        versioning.addVersion( "1.1" );
        versioning.setLatest( "1.1" );
        versioning.setRelease( "1.1" );
        versioning.setLastUpdated( "1234567" );
        
        releaseMetadata.setVersioning( versioning );
        
        return releaseMetadata;
    }
    
    private Metadata getSnapshotMetadata()
    {
        Metadata snapshotMetadata = new Metadata();
        snapshotMetadata.setArtifactId( "test" );
        snapshotMetadata.setGroupId( "test" );
        
        Versioning versioning = new Versioning();
        versioning.addVersion( "1.1-SNAPSHOT" );
        versioning.addVersion( "1.2-SNAPSHOT" );
        versioning.setLatest( "1.2-SNAPSHOT" );
        versioning.setRelease( "" );
        versioning.setLastUpdated( "1234568" );
        
        snapshotMetadata.setVersioning( versioning );
        
        return snapshotMetadata;
    }

    private Metadata getSource( boolean setLastUpdate )
    {
        Metadata md = new Metadata();
        md.setArtifactId( "log4j" );
        md.setGroupId( "log4j" );
        md.setVersion( "1.1.3" );

        Versioning versioning = new Versioning();
        versioning.setVersions( Arrays.asList( "1.1.3" ) );

        if ( setLastUpdate )
        {
            versioning.setLastUpdated( "1234567" );
        }

        md.setVersioning( versioning );

        return md;
    }

    private Metadata getTarget( boolean setLastUpdate )
    {
        Metadata md = new Metadata();
        md.setArtifactId( "log4j" );
        md.setGroupId( "log4j" );
        md.setVersion( "1.1.3" );

        Versioning versioning = new Versioning();
        versioning.setVersions( Arrays.asList( "1.1.3", "1.2.4", "1.2.5", "1.2.6", "1.2.7", "1.2.8", "1.2.11", "1.2.9",
                                               "1.2.12", "1.2.13" ) );

        if ( setLastUpdate )
        {
            versioning.setLastUpdated( "7654321" );
        }

        md.setVersioning( versioning );

        return md;
    }

    private void validate( Metadata md, boolean setLastUpdate, boolean targetLastUpdate )
    {
        assertTrue( md.getVersioning().getVersions().containsAll(
                                                                  Arrays.asList( "1.2.4", "1.2.5", "1.2.6", "1.2.7",
                                                                                 "1.2.8", "1.2.11", "1.2.9", "1.2.12",
                                                                                 "1.2.13" ) ) );

        if ( setLastUpdate )
        {
            if ( targetLastUpdate )
            {
                assertEquals( "7654321", md.getVersioning().getLastUpdated() );
            }
            else
            {
                assertEquals( "1234567", md.getVersioning().getLastUpdated() );
            }
        }
        else
        {
            // it should contain "now", but not be blank
            assertTrue( StringUtils.isNotBlank( md.getVersioning().getLastUpdated() ) );
        }
    }
}
