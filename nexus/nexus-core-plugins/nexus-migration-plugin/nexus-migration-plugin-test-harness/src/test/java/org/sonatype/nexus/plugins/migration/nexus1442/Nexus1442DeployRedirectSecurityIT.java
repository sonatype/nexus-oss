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
package org.sonatype.nexus.plugins.migration.nexus1442;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.Base64;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1442DeployRedirectSecurityIT
    extends AbstractMigrationIntegrationTest
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
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void deployWithPermition()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        int returnCode = deploy();
        Assert.assertTrue( "Unable to deploy artifact " + returnCode, Status.isSuccess( returnCode ) );
    }

    @Test
    public void deployWithoutPermition()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "dummy" );

        Assert.assertEquals( "Unable to deploy artifact ", 401, deploy() );
    }

    private int deploy()
        throws IOException
    {
        File artifact = getTestFile( "artifact.jar" );
        String path = "nxcm281/deploy/released/1.0/released-1.0.jar";
        String deployUrl = "http://localhost:" + nexusApplicationPort + "/artifactory/main-local/";
        // wagon is not sending authentication
        // DeployUtils.deployWithWagon( this.container, "http", deployUrl, artifact, path );

        URL url = new URL( deployUrl + path );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        TestContext testContext = TestContainer.getInstance().getTestContext();
        String userPassword = testContext.getUsername() + ":" + testContext.getPassword();
        userPassword = Base64.encodeToString( userPassword.getBytes() );
        userPassword = "Basic " + userPassword;
        conn.setRequestProperty( "Authorization", userPassword );
        conn.setDoOutput( true );
        conn.setRequestMethod( "PUT" );
        byte[] bytes = FileUtils.readFileToByteArray( artifact );
        OutputStream out = conn.getOutputStream();
        IOUtils.write( bytes, out );
        out.close();

        return conn.getResponseCode();
    }
}
