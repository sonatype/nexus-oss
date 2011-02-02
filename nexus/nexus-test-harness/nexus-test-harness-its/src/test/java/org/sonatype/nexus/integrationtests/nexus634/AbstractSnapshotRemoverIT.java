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
package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractSnapshotRemoverIT
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    public static Collection<File> listFiles( File directory, String[] extensions, boolean recursive )
    {
        return FileUtils.listFiles( directory, extensions, recursive );
    }

    protected File artifactFolder;

    protected File repositoryPath;

    public AbstractSnapshotRemoverIT()
    {
        super( "nexus-test-harness-snapshot-repo" );
    }

    @BeforeMethod
    public void deploySnapshotArtifacts()
        throws Exception
    {
        initFolders();

        File oldSnapshot = getTestFile( "repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        RepositoryMessageUtil.updateIndexes( "nexus-test-harness-snapshot-repo" );

        // Gav gav =
        // new Gav( "nexus634", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, true, false, null, false, null );
        // File fileToDeploy = getTestFile( "artifact-1.jar" );
        //
        // // Deploying a fresh timestamp artifact
        // MavenDeployer.deployAndGetVerifier( gav, getNexusTestRepoUrl(), fileToDeploy, null );
        //
        // // Artifacts should be deployed here
        // Assert.assertTrue( "nexus643:artifact:1.0-SNAPSHOT folder doesn't exists!", artifactFolder.isDirectory() );
    }

    public void initFolders()
        throws Exception
    {
        repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-snapshot-repo" );
        artifactFolder = new File( repositoryPath, "nexus634/artifact/1.0-SNAPSHOT" );
    }

    protected void runSnapshotRemover( String repositoryId, int minSnapshotsToKeep, int removeOlderThanDays,
                                       boolean removeIfReleaseExists )
        throws Exception
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setKey( "repositoryId" );
        repositoryProp.setValue( repositoryId );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setKey( "minSnapshotsToKeep" );
        keepSnapshotsProp.setValue( String.valueOf( minSnapshotsToKeep ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setKey( "removeOlderThanDays" );
        ageProp.setValue( String.valueOf( removeOlderThanDays ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setKey( "removeIfReleaseExists" );
        removeReleasedProp.setValue( String.valueOf( removeIfReleaseExists ) );

        TaskScheduleUtil.runTask( SnapshotRemovalTaskDescriptor.ID, repositoryProp, keepSnapshotsProp, ageProp,
            removeReleasedProp );
    }

}