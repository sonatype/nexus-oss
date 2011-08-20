/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.nexus3301;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.it.VerificationException;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus3301MavenDeployRedirectSecurityIT
    extends AbstractMigrationPrivilegeTest
{

    @BeforeClass
    public void start()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        assertThat( "Migration is succesful", doMigration().isSuccess() );

        TaskScheduleUtil.waitForAllTasksToStop();
    }

    @BeforeMethod
    public void cleanRepo()
        throws IOException
    {
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), getTestId() );
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), "nxcm281" );
    }

    @Test
    public void deployWithPermition()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        replaceUserRole( TEST_USER_NAME, "nx-admin" );

        deploy();
    }

    @Test( expectedExceptions = VerificationException.class )
    public void deployWithoutPermition()
        throws Exception
    {
        deploy();
    }

    private void deploy()
        throws Exception
    {
        File artifact = getTestFile( "artifact.jar" );

        String deployUrl = "http://localhost:" + nexusApplicationPort + "/artifactory/main-local/";

        Gav gav = GavUtil.newGav( "nexus3301", "released", "1.0" );
        MavenDeployer.deployAndGetVerifier( gav, deployUrl, artifact, getOverridableFile( "settings.xml" ) );
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactoryBackup.zip" );
    }
}
