package org.sonatype.nexus.proxy.maven.metadata.mercury;

import java.util.Arrays;

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

        MergeOperation mergeOp = new MergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, false, false );
    }

    public void testMergeTargetLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( false );

        Metadata md2 = getTarget( true );

        MergeOperation mergeOp = new MergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, true );
    }

    public void testMergeSourceLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( true );

        Metadata md2 = getTarget( false );

        MergeOperation mergeOp = new MergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, false );
    }

    public void testMergeBothLastUpdate()
        throws Exception
    {
        Metadata md1 = getSource( true );

        Metadata md2 = getTarget( true );

        MergeOperation mergeOp = new MergeOperation( new MetadataOperand( md1 ) );
        mergeOp.perform( md2 );

        validate( md2, true, true );
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
