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

public class Nexus429DeployProblemTest extends AbstractPrivilegeTest
{

    @Test
    public void goingToFail() throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException, AuthorizationException, ComponentLookupException, TransferFailedException
    {

     // GAV
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // Grab File used to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // Now if i deploy with admin first, that will cause problems when i try to deploy with the 'test-user'
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy, this.getRelitiveArtifactPath( gav ));
        
        
        // Just to be sure lets delete the everything we have uploaded so far
        // we need to delete the files...
        this.deleteFromRepository( this.getTestId()+"/" );
        
        // This will print the users current privileges to the console, so we can make sure i am not crazy
        this.printUserPrivs( "test-user" );
         
        // Change the user/pass to test-user
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // this user only has anonymous role
        try
        {
            // deployment should fail, we are expecting a 401,  if you comment out the admin deploy above this will work fine
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy, this.getRelitiveArtifactPath( gav ));
            Assert.fail( "File should NOT have been deployed" );
        }
        catch ( TransferFailedException e )
        {
            // expected 401
        }
        
        // the rest of this stuff is fine, but we don't make it this far....
                
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
