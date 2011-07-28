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
package org.sonatype.nexus.plugins.migration.nexus3301;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus3301MavenDownloadRedirectSecurityIT
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
    }

    @Before
    public void cleanRepo()
        throws IOException
    {
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), getTestId() );
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), "nxcm281" );
    }

    @Test
    public void downloadWithPermition()
        throws Exception
    {
        AbstractMavenNexusIT.cleanRepository( new File( TestProperties.getString( "maven.local.repo" ) ), getTestId() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        replaceUserRole( TEST_USER_NAME, "nx-admin" );

        download();
    }

    @Test( expected = VerificationException.class )
    public void downloadWithoutPermition()
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
