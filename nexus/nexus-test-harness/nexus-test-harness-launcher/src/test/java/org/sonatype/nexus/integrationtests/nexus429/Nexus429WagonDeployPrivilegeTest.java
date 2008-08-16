package org.sonatype.nexus.integrationtests.nexus429;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.MavenDeployer;

public class Nexus429WagonDeployPrivilegeTest
    extends AbstractPrivilegeTest
{
    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus429WagonDeployPrivilegeTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void deployPrivWithWagon()
        throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException,
        AuthorizationException, ComponentLookupException, TransferFailedException, InterruptedException,
        CommandLineException
    {
        this.resetTestUserPrivs();

        // GAV
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        
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
            // DeployUtils.forkDeployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy,
            // this.getRelitiveArtifactPath( gav ));
            MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
            Assert.fail( "File should NOT have been deployed" );
        }
        // catch ( TransferFailedException e )
        // {
        // // expected 401
        // }
        catch ( CommandLineException e )
        {
            // expected 401
            // MavenDeployer, either fails or not, we can't check the cause of the problem
        }

        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        try
        {
            // if this fails it will throw an error
            MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
        }
        catch ( CommandLineException e )
        {
            // expected 401
            // MavenDeployer, either fails or not, we can't check the cause of the problem
            // the user now needs create priv for new artifacts
        }

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T5" );

        // if this fails it will throw an error
        MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy, this.getOverridableFile( "settings.xml" ) );
        
        // do it again as an update, this should fail
        try
        {
            // if this fails it will throw an error
            MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
        }
        catch ( CommandLineException e )
        {
            // expected 401
            // MavenDeployer, either fails or not, we can't check the cause of the problem
            // the user now needs create priv for new artifacts
        }
        

        // now the user should be able to redeploy
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T3" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        // if this fails it will throw an error
        MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy, this.getOverridableFile( "settings.xml" ) );

        // make sure delete does not work
        Response response =
            RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId(),
                                       Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 401, response.getStatus().getCode() );

    }

}
