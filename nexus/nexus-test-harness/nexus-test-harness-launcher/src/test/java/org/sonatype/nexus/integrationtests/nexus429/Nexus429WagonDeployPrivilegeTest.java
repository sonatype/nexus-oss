/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus429;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.MavenDeployer;

/**
 * Test the privilege wagon style deployments.
 */
public class Nexus429WagonDeployPrivilegeTest
    extends AbstractPrivilegeTest
{
    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus429WagonDeployPrivilegeTest()
    {
        super( TEST_RELEASE_REPO );
    }

//    @Test
    public void doReleaseArtifactTest()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        this.setTestRepositoryId( TEST_RELEASE_REPO );
        this.deployPrivWithWagon( gav, this.getNexusTestRepoUrl() );
    }

    @Test
    public void doSnapshotArtifactTest()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0-SNAPSHOT", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );
        this.setTestRepositoryId( TEST_SNAPSHOT_REPO );
        this.deployPrivWithWagon( gav, this.getNexusTestRepoUrl() );
    }

    private void deployPrivWithWagon( Gav gav, String repoUrl )
        throws Exception
    {
        this.resetTestUserPrivs();

        Verifier verifier = null;

        this.deleteFromRepository( this.getTestId() );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // we need to delete the files...
        this.deleteFromRepository( this.getTestId() + "/" );

        // deploy
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // with pom should fail

        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, repoUrl, fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // if this fails it will throw an error
        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, repoUrl, fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T5" );

        // if this fails it will throw an error
        verifier =
            MavenDeployer.deployAndGetVerifier( gav, repoUrl, fileToDeploy, this.getOverridableFile( "settings.xml" ) );
        verifier.verifyErrorFreeLog();

        // do it again as an update, this should fail

        // if this fails it will throw an error
        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, repoUrl, fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // now the user should be able to redeploy
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T3" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        // if this fails it will throw an error
        verifier =
            MavenDeployer.deployAndGetVerifier( gav, repoUrl, fileToDeploy, this.getOverridableFile( "settings.xml" ) );
        verifier.verifyErrorFreeLog();

        // check the services url too, ( just the GET for now )
        // just check the parent dir, incase this is a SNAPSHOT repo
        Response response =
            RequestFacade.sendMessage( new URL( this.getNexusTestRepoServiceUrl() + gav.getGroupId().replace( '.', '/' ) + "/" + gav.getArtifactId() + "/" + gav.getVersion() + "/"),
                                       Method.GET, null );
        Assert.assertEquals( "Artifact should have been downloaded", 200, response.getStatus().getCode() );
        
        
        // make sure delete does not work
        response =
            RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId(),
                                       Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 401, response.getStatus().getCode() );

    }

    // FIXME: refactor AbstractMavenNexusIT and sub class this from there.

    /**
     * Workaround to get some decent logging when tests fail
     * 
     * @throws IOException
     */
    protected void failTest( Verifier verifier )
        throws IOException
    {
        File logFile = new File( verifier.getBasedir(), "log.txt" );
        String log = FileUtils.readFileToString( logFile );
        Assert.fail( log );
    }

}
