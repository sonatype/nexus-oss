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
package org.sonatype.nexus.integrationtests.nexus260;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * Deploys an artifact multiple times. (this is allowed)  
 */
public class Nexus260MultipleDeployIT
    extends AbstractNexusIntegrationTest
{

    public Nexus260MultipleDeployIT()
    {
        super( "nexus-test-harness-repo" );
    }


    @Test
    public void singleDeployTest()
        throws Exception
    {   
        // file to deploy
        File fileToDeploy = this.getTestFile( "singleDeployTest.xml" );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, "org/sonatype/nexus-integration-tests/multiple-deploy-test/singleDeployTest/1/singleDeployTest-1.xml" );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "singleDeployTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );
    }

    @Test
    public void deploySameFileMultipleTimesTest()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = this.getTestFile("deploySameFileMultipleTimesTest.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deploySameFileMultipleTimesTest/1/deploySameFileMultipleTimesTest-1.xml";

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );
        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deploySameFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

    }

    @Test
    public void deployChangedFileMultipleTimesTest()
        throws Exception
    {
        // files to deploy
        File fileToDeploy1 = this.getTestFile( "deployChangedFileMultipleTimesTest1.xml" );
        File fileToDeploy2 = this.getTestFile(  "deployChangedFileMultipleTimesTest2.xml" );
        File fileToDeploy3 = this.getTestFile(  "deployChangedFileMultipleTimesTest3.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deployChangedFileMultipleTimesTest/1/deployChangedFileMultipleTimesTest-1.xml";

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy1, deployPath );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy2, deployPath );
        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy3, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deployChangedFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy3, artifact ) );

        // this should pass if the above passed
        assertFalse( FileTestingUtils.compareFileSHA1s( fileToDeploy2, artifact ) );

    }    
}

