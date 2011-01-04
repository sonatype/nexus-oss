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
package org.sonatype.nexus.integrationtests.nexus260;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void singleDeployTest()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = this.getTestFile( "singleDeployTest.xml" );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy,
                                          "org/sonatype/nexus-integration-tests/multiple-deploy-test/singleDeployTest/1/singleDeployTest-1.xml" );

        // download it
        File artifact =
            downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "singleDeployTest", "1",
                              "xml", null, "./target/downloaded-jars" );

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
        File fileToDeploy = this.getTestFile( "deploySameFileMultipleTimesTest.xml" );

        String deployPath =
            "org/sonatype/nexus-integration-tests/multiple-deploy-test/deploySameFileMultipleTimesTest/1/deploySameFileMultipleTimesTest-1.xml";

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy, deployPath );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy, deployPath );
        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy, deployPath );

        // download it
        File artifact =
            downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test",
                              "deploySameFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

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
        File fileToDeploy2 = this.getTestFile( "deployChangedFileMultipleTimesTest2.xml" );
        File fileToDeploy3 = this.getTestFile( "deployChangedFileMultipleTimesTest3.xml" );

        String deployPath =
            "org/sonatype/nexus-integration-tests/multiple-deploy-test/deployChangedFileMultipleTimesTest/1/deployChangedFileMultipleTimesTest-1.xml";

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy1, deployPath );

        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy2, deployPath );
        // deploy it
        getDeployUtils().deployWithWagon( "http", this.getNexusTestRepoUrl(), fileToDeploy3, deployPath );

        // download it
        File artifact =
            downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test",
                              "deployChangedFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy3, artifact ) );

        // this should pass if the above passed
        assertFalse( FileTestingUtils.compareFileSHA1s( fileToDeploy2, artifact ) );

    }
}
