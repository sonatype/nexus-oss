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
package org.sonatype.nexus.integrationtests.nexus983;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

/**
 * Copy (filesystem copy) a jar to a nexus repo and run reindex to see what happens
 */
public class Nexus983IndexArtifactsWihoutPomIT
    extends AbstractNexusIntegrationTest
{

    protected SearchMessageUtil messageUtil;

    @Before
    public void ini()
    {
        this.messageUtil = new SearchMessageUtil();
    }

    @Test
    public void deployPomlessArtifact()
        throws Exception
    {
        File artifactFile = getTestFile( "artifact.jar" );
        getDeployUtils().deployWithWagon( "http", nexusBaseUrl + "content/repositories/"
            + REPO_TEST_HARNESS_REPO, artifactFile, "nexus983/nexus983-artifact1/1.0.0/nexus983-artifact1-1.0.0.jar" );
        List<NexusArtifact> artifacts = messageUtil.searchFor( "nexus983-artifact1" );
        Assert.assertEquals( "Should find one artifact", 1, artifacts.size() );
    }

    @Test
    public void copyPomlessArtifact()
        throws Exception
    {
        File artifactFile = getTestFile( "artifact.jar" );
        FileUtils.copyFile( artifactFile, new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO
            + "/nexus983/nexus983-artifact2/1.0.0/nexus983-artifact2-1.0.0.jar" ) );
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );

        List<NexusArtifact> artifacts = messageUtil.searchFor( "nexus983-artifact2" );
        Assert.assertEquals( "Should find one artifact", 1, artifacts.size() );
    }

}
