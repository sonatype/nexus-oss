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
package org.sonatype.nexus.integrationtests.nxcm970;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nxcm970SimultaneousUploadDownloadIT
    extends AbstractNexusIntegrationTest
{
    private Executor executor = Executors.newSingleThreadExecutor();

    @BeforeClass
    public static void cleanUp()
        throws Exception
    {
        AbstractNexusIntegrationTest.cleanWorkDir();
    }
    
    @Test
    public void testSimultaneousUploadDownload()
        throws Exception
    {
        // preparations
        String baseUrl = getRepositoryUrl( "nexus-test-harness-repo" );
        // add path
        String targetUrl = baseUrl + "nxcm970/artifact/1.0/artifact-1.0.pom";

        // create deployer that we will control how long to "deploy"
        ContinuousDeployer continuousDeployer = new ContinuousDeployer( targetUrl );

        // download the subjectArtifact -- should result in 404
        // downloadSubjectArtifact( false, baseUrl );

        // start deploying the subjectArtifact -- should work on it
        executor.execute( continuousDeployer );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // let it work a lil'
        Thread.sleep( 1000 );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // let it work a lil'
        Thread.sleep( 1000 );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // finish deploying the subjectArtifaft -- should finish succesfully
        continuousDeployer.finishDeploying();

        // wait to finish the HTTP tx, check result
        while ( !continuousDeployer.isFinished() )
        {
            Thread.sleep( 200 );
        }

        Assert.assertTrue( continuousDeployer.getResult() == 201,
                           "Deployment failed: " + continuousDeployer.getResult() );

        // download the subjectArtifact -- should result in 200, found
        downloadSubjectArtifact( true, baseUrl );
    }

    // ==

    protected void downloadSubjectArtifact( boolean shouldSucceed, String baseUrl )
    {
        try
        {
            downloadArtifact( baseUrl, "nxcm970", "artifact", "1.0", "pom", null, "./target/downloaded-jars" );

            if ( !shouldSucceed )
            {
                Assert.fail( "Should not succeed the retrieval!" );
            }
        }
        catch ( IOException e )
        {
            if ( shouldSucceed )
            {
                Assert.fail( "Should succeed the retrieval!" );
            }
        }
    }

}
