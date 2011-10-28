/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus1022;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.scheduling.TaskUtils;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1022RebuildRepositoryMavenMetadataTaskIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void rebuildMavenMetadata()
        throws Exception
    {
        /*
         * if(true) { printKnownErrorButDoNotFail( getClass(), "rebuildMavenMetadata" ); return; }
         */

        String dummyFile = new File( nexusWorkDir, "nexus1022.dummy" ).getAbsolutePath();
        String repoPrefix = "storage/nexus-test-harness-repo/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();

        repo.setKey( "repositoryId" );

        repo.setValue( REPO_TEST_HARNESS_REPO );

        TaskScheduleUtil.runTask( "RebuildMavenMetadata-Nexus1022", RebuildMavenMetadataTaskDescriptor.ID, repo );
        TaskScheduleUtil.waitForAllTasksToStop();

        File artifactDirMd =
            new File( nexusWorkDir, repoPrefix + "nexus1022/foo/bar/artifact/maven-metadata.xml" );
        Assert.assertTrue( artifactDirMd.exists(), "Maven metadata file should be generated after rebuild" );

        File groupPluginMd = new File( nexusWorkDir, repoPrefix + "nexus1022/foo/bar/plugins/maven-metadata.xml" );
        Assert.assertTrue( groupPluginMd.exists(), "Maven metadata file should be generated after rebuild" );

        // just downloading it into dummy, since we are just checking is download possible
        // if not, downloadFile() will fail anyway. The content is not we are interested in.
        downloadFile( new URL( nexusBaseUrl + "content/repositories/nexus-test-harness-repo/"
                                   + "nexus1022/foo/bar/plugins/maven-metadata.xml" ), dummyFile );
        downloadFile(
            new URL( nexusBaseUrl + "content/groups/public/" + "nexus1022/foo/bar/plugins/maven-metadata.xml" ),
            dummyFile );

        downloadFile( new URL( nexusBaseUrl + "content/repositories/nexus-test-harness-repo/"
                                   + "nexus1022/foo/bar/plugins/maven-metadata.xml" + ".sha1" ), dummyFile );
        downloadFile( new URL( nexusBaseUrl + "content/groups/public/" + "nexus1022/foo/bar/plugins/maven-metadata.xml"
                                   + ".sha1" ), dummyFile );
    }

}
