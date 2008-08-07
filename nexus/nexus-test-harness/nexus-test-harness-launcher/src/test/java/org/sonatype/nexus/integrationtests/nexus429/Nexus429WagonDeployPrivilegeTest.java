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
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.DeployUtils;

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
        throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException, AuthorizationException, ComponentLookupException, TransferFailedException
    {
        this.resetTestUserPrivs();
        
        // GAV
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );
        
        // we need to delete the files...

        this.deleteFromRepository( this.getTestId()+"/" );
        
        this.printUserPrivs( "test-user" );
         
        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // with pom should fail
        
        try
        {
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy, this.getRelitiveArtifactPath( gav ));
            Assert.fail( "File should NOT have been deployed" );
        }
        catch ( TransferFailedException e )
        {
            // expected 401
        }
                
        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();
//        this.giveUserPrivilege( "test-user", "T5" );
        this.giveUserPrivilege( "test-user", "T3" ); // the Wagon does a PUT not a POST, so this is correct
        
        // try again
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy, this.getRelitiveArtifactPath( gav ));
        
    }
    

}
