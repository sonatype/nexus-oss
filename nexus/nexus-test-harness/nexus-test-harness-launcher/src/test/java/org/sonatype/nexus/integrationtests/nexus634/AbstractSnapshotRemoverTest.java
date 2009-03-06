/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.SnapshotRemovalTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class AbstractSnapshotRemoverTest
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    public static Collection<File> listFiles( File directory, String[] extensions, boolean recursive )
    {
        return FileUtils.listFiles( directory, extensions, recursive );
    }

    protected File artifactFolder;

    protected File repositoryPath;

    public AbstractSnapshotRemoverTest()
    {
        super( "nexus-test-harness-snapshot-repo" );
    }

    @Before
    public void deploySnapshotArtifacts()
        throws Exception
    {
        initFolders();

        File oldSnapshot = getTestFile( "repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        RepositoryMessageUtil.updateIndexes( "nexus-test-harness-snapshot-repo" );

//        Gav gav =
//            new Gav( "nexus634", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, true, false, null, false, null );
//        File fileToDeploy = getTestFile( "artifact-1.jar" );
//
//        // Deploying a fresh timestamp artifact
//        MavenDeployer.deployAndGetVerifier( gav, getNexusTestRepoUrl(), fileToDeploy, null );
//
//        // Artifacts should be deployed here
//        Assert.assertTrue( "nexus643:artifact:1.0-SNAPSHOT folder doesn't exists!", artifactFolder.isDirectory() );
    }

    public void initFolders()
        throws Exception
    {
        repositoryPath = new File( nexusWorkDir, "storage/nexus-test-harness-snapshot-repo" );
        artifactFolder = new File( repositoryPath, "nexus634/artifact/1.0-SNAPSHOT" );
    }

    protected void runSnapshotRemover( String repositoryOrGroupId, int minSnapshotsToKeep, int removeOlderThanDays,
                                       boolean removeIfReleaseExists )
        throws Exception
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setId( "repositoryOrGroupId" );
        repositoryProp.setValue( repositoryOrGroupId );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setId( "minSnapshotsToKeep" );
        keepSnapshotsProp.setValue( String.valueOf( minSnapshotsToKeep ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setId( "removeOlderThanDays" );
        ageProp.setValue( String.valueOf( removeOlderThanDays ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setId( "removeIfReleaseExists" );
        removeReleasedProp.setValue( String.valueOf( removeIfReleaseExists ) );

        ScheduledServiceListResource task =TaskScheduleUtil.runTask( SnapshotRemovalTaskDescriptor.ID,
                                  repositoryProp, keepSnapshotsProp, ageProp, removeReleasedProp );

        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }

}