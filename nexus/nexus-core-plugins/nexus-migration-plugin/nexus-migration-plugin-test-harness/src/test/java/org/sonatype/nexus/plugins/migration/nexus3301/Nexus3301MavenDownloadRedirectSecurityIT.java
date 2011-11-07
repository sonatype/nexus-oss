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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus3301MavenDownloadRedirectSecurityIT
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
        assertThat( "Migration is successful", doMigration().isSuccess() );
    }

    @BeforeMethod
    public void cleanRepo()
        throws IOException
    {
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), getTestId() );
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), "nxcm281" );
    }

    @Test
    public void downloadWithPermission()
        throws Exception
    {
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), getTestId() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        replaceUserRole( TEST_USER_NAME, "nx-admin" );

        download();
    }

    @Test( expectedExceptions = VerificationException.class )
    public void downloadWithoutPermission()
        throws Exception
    {
        download();
    }

    private void download()
        throws Exception
    {

        Verifier v =
            AbstractMavenNexusIT.createMavenVerifier( getTestFile( "project" ), getOverridableFile( "settings.xml" ),
                getTestId() );

        v.executeGoal( "install" );
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactoryBackup.zip" );
    }

}
