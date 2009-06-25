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
package org.sonatype.nexus.integrationtests.proxy.nexus635;

import static org.sonatype.nexus.test.utils.FileTestingUtils.compareFileSHA1s;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Tests the expire cache task.
 */
public class Nexus635ExpireCacheTaskTest
    extends AbstractNexusProxyIntegrationTest
{

    private Gav GAV = new Gav(
        "nexus635",
        "artifact",
        "1.0-SNAPSHOT",
        null,
        "jar",
        0,
        0L,
        null,
        true,
        false,
        null,
        false,
        null );

    @BeforeClass
    public static void cleanEnv()
        throws IOException
    {
        cleanWorkDir();
    }

    public Nexus635ExpireCacheTaskTest()
        throws Exception
    {
        super( "tasks-snapshot-repo" );
    }

    public void addSnapshotArtifactToProxy( File fileToDeploy )
        throws Exception
    {
        String repositoryUrl = "file://" + localStorageDir + "/tasks-snapshot-repo";
        MavenDeployer.deploy( GAV, repositoryUrl, fileToDeploy, null );
    }

    @Test
    public void expireCacheTask()
        throws Exception
    {
        /*
         * fetch something from a remote repo, run clearCache from root, on _remote repo_ put a newer timestamped file,
         * and rerequest again the same (the filenames will be the same, only the content/timestamp should change),
         * nexus should refetch it. BUT, this works for snapshot nexus reposes only, release reposes do not refetch!
         */
        File artifact1 = getTestFile( "artifact-1.jar" );
        addSnapshotArtifactToProxy( artifact1 );

        File firstDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "First time, should download artifact 1", //
                           compareFileSHA1s( firstDownload, artifact1 ) );

        File artifact2 = getTestFile( "artifact-2.jar" );
        addSnapshotArtifactToProxy( artifact2 );
        File secondDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "Before ExpireCache should download artifact 1",//
                           compareFileSHA1s( secondDownload, artifact1 ) );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "tasks-snapshot-repo" );

        // prop = new ScheduledServicePropertyResource();
        // prop.setId( "resourceStorePath" );
        // prop.setValue( "/" );

        // This is THE important part
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );
        Assert.assertNotNull( task );

        File thirdDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "After ExpireCache should download artifact 2", //
                           compareFileSHA1s( thirdDownload, artifact2 ) );
    }

}
