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
