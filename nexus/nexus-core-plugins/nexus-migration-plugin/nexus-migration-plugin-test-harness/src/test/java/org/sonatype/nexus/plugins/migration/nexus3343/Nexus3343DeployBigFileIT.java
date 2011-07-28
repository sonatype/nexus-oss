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
