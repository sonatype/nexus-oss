/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus3301MavenDeployRedirectSecurityIT
    extends AbstractMigrationPrivilegeTest
{

    @BeforeClass
    public static void start()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        Assert.assertTrue( doMigration().isSuccess() );

        TaskScheduleUtil.waitForAllTasksToStop();
    }

    @Before
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

    @Test( expected = VerificationException.class )
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
