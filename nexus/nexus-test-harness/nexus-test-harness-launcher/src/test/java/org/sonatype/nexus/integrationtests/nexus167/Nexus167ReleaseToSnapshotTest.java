/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus167;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.maven.wagon.TransferFailedException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;

/**
 * Deploy a release artifact to a snapshot repo. (should fail)
 */
public class Nexus167ReleaseToSnapshotTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus167ReleaseToSnapshotTest()
    {
        super( TEST_SNAPSHOT_REPO );
    }

    @Test
    public void deployReleaseToSnapshot()
        throws Exception
    {
        Gav gav = new Gav(
            this.getTestId(),
            "simpleArtifact",
            "1.0.0",
            null,
            "xml",
            0,
            new Date().getTime(),
            "Simple Test Artifact",
            false,
            false,
            null,
            false,
            null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        try
        {
            // deploy it
            // this should fail
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy, this
                .getRelitiveArtifactPath( gav ) );
            Assert.fail( "Should not be able to deploy a releases artifact into a snapshot repo" );
        }
        catch ( TransferFailedException e )
        {
            // cstamas: HTTP 400 is returned from now on
            // this is expected
        }

    }

    @Test
    public void deployUsingRest()
        throws HttpException,
            IOException
    {

        Gav gav = new Gav(
            this.getTestId(),
            "uploadWithGav",
            "1.0.0",
            null,
            "xml",
            0,
            new Date().getTime(),
            "Simple Test Artifact",
            false,
            false,
            null,
            false,
            null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // the method we are calling
        PostMethod filePost = new PostMethod( uploadURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_SNAPSHOT_REPO, gav, fileToDeploy );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Upload attempt should have returned a 400, it returned:  " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
            // download it
            downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );
    }

    @Test
    public void deploywithPomUsingRest()
        throws HttpException,
            IOException
    {

        Gav gav = new Gav(
            this.getTestId(),
            "uploadWithPom",
            "1.0.0",
            null,
            "xml",
            0,
            new Date().getTime(),
            "Simple Test Artifact",
            false,
            false,
            null,
            false,
            null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingPomWithRest(
            uploadURL,
            TEST_SNAPSHOT_REPO,
            fileToDeploy,
            pomFile,
            null,
            null );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Upload attempt should have returned a 400, it returned:  " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
            // download it
            downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );
    }

}
