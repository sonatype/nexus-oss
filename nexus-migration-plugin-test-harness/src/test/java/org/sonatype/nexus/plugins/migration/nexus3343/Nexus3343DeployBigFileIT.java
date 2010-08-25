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
package org.sonatype.nexus.plugins.migration.nexus3343;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.Base64;
import org.junit.Assert;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;

public class Nexus3343DeployBigFileIT
    extends AbstractBigFileIT
{

    @Override
    public File doTest()
        throws Exception
    {

        String path = "nexus3343/released/1.0/released-1.0.bin";
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
        FileInputStream input = new FileInputStream( getSourceFile() );
        OutputStream out = conn.getOutputStream();
        IOUtils.copy( input, out );
        input.close();
        out.close();

        Assert.assertTrue( Status.isSuccess( conn.getResponseCode() ) );

        return new File( nexusWorkDir, "storage/main-local-releases/nexus3343/released/1.0/released-1.0.bin" );
    }

    @Override
    public File getSourceFile()
    {
        return new File( getTestFile( "." ), "sourceFile.bin" );
    }
}
