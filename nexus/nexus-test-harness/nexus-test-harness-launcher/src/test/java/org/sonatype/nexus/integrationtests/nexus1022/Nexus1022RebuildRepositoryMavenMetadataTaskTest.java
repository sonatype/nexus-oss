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
package org.sonatype.nexus.integrationtests.nexus1022;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1022RebuildRepositoryMavenMetadataTaskTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void rebuildMavenMetadata()
        throws Exception
    {
/*        if(true) {
            printKnownErrorButDoNotFail( getClass(), "rebuildMavenMetadata" );
            return;
        }*/
        String releaseRepoPath = "runtime/work/storage/nexus-test-harness-repo/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();

        repo.setId( "repositoryOrGroupId" );

        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask("RebuildMavenMetadata-Nexus1022", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        File artifactDirMd = new File( nexusBaseDir, releaseRepoPath + "nexus1022/foo/bar/artifact/maven-metadata.xml" );
        Assert.assertTrue( "Maven metadata file should be generated after rebuild", artifactDirMd.exists() );

        File groupPluginMd = new File( nexusBaseDir, releaseRepoPath + "nexus1022/foo/bar/plugins/maven-metadata.xml" );
        Assert.assertTrue( "Maven metadata file should be generated after rebuild", groupPluginMd.exists() );

    }

    @BeforeClass
    public static void cleanWorkFolder() throws Exception {
        cleanWorkDir();
    }

}
