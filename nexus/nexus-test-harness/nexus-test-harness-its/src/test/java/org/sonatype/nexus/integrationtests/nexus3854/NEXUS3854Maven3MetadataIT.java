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
package org.sonatype.nexus.integrationtests.nexus3854;

import static org.apache.commons.io.FileUtils.copyDirectoryToDirectory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class NEXUS3854Maven3MetadataIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void maven3Metadata()
        throws Exception
    {
        File repo = new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_SNAPSHOT_REPO );
        copyDirectoryToDirectory( getTestFile( "org" ), repo );

        ScheduledServicePropertyResource props = new ScheduledServicePropertyResource();
        props.setKey( RebuildMavenMetadataTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        props.setValue( REPO_TEST_HARNESS_SNAPSHOT_REPO );
        TaskScheduleUtil.runTask( RebuildMavenMetadataTaskDescriptor.ID, props );
        TaskScheduleUtil.waitForAllTasksToStop();

        Metadata m1 = getMetadata( new File( repo, "org/sonatype/nexus/nexus-api/maven-metadata.xml" ) );
        Metadata m2 = getMetadata( getTestFile( "artifactId-maven-metadata.xml" ) );
        equals( m1, m2 );

        Metadata m3 = getMetadata( new File( repo, "org/sonatype/nexus/nexus-api/1.9-SNAPSHOT/maven-metadata.xml" ) );
        Metadata m4 = getMetadata( getTestFile( "version-maven-metadata.xml" ) );
        equals( m3, m4 );
    }

    private void equals( Metadata m1, Metadata m2 )
    {
        assertNotNull( m1 );
        assertNotNull( m2 );

        assertEquals( m1.getArtifactId(), m2.getArtifactId() );
        assertEquals( m1.getGroupId(), m2.getGroupId() );
        assertEquals( m1.getVersion(), m2.getVersion() );

        assertNotNull( m1.getVersioning() );
        assertNotNull( m2.getVersioning() );

        assertEquals( m1.getVersioning().getLatest(), m2.getVersioning().getLatest() );
        assertEquals( m1.getVersioning().getRelease(), m2.getVersioning().getRelease() );

        if ( m1.getVersioning().getSnapshot() != null || m1.getVersioning().getSnapshot() != null )
        {
            assertEquals( m1.getVersioning().getSnapshot().getBuildNumber(),
                m2.getVersioning().getSnapshot().getBuildNumber() );
            assertEquals( m1.getVersioning().getSnapshot().getTimestamp(),
                m2.getVersioning().getSnapshot().getTimestamp() );
        }

        assertEquals( m1.getVersioning().getSnapshotVersions().size(), m2.getVersioning().getSnapshotVersions().size() );
        for ( int i = 0; i < m1.getVersioning().getSnapshotVersions().size(); i++ )
        {
            SnapshotVersion s1 = m1.getVersioning().getSnapshotVersions().get( i );
            SnapshotVersion s2 = get( s1, m2.getVersioning().getSnapshotVersions() );

            assertNotNull( s1 );
            assertNotNull( s2 );
            assertEquals( s1.getClassifier(), s2.getClassifier() );
            assertEquals( s1.getExtension(), s2.getExtension() );
            assertEquals( s1.getUpdated(), s2.getUpdated() );
            assertEquals( s1.getVersion(), s2.getVersion() );
        }
    }

    private SnapshotVersion get( SnapshotVersion s1, List<SnapshotVersion> snapshotVersions )
    {
        for ( SnapshotVersion s2 : snapshotVersions )
        {
            if ( StringUtils.equals( s1.getClassifier(), s2.getClassifier() )
                && StringUtils.equals( s1.getExtension(), s2.getExtension() ) )
            {
                return s2;
            }
        }
        return null;
    }

    private Metadata getMetadata( File metadata )
        throws Exception
    {
        FileInputStream in = new FileInputStream( metadata );
        try
        {
            return MetadataBuilder.read( in );
        }
        finally
        {
            IOUtil.close( in );
        }
    }
}
