package org.sonatype.nexus.integrationtests.nexus477;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus477ArtifactsCrudTests
    extends AbstractPrivilegeTest
{

    @Before
    public void deployArtifact()
        throws IOException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException, ComponentLookupException
    {
//        Gav gav =
//            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
//                     null, false, null );
//
//        // Grab File used to deploy
//        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
//
//        // Now if i deploy with admin first, that will cause problems when i try to deploy with the 'test-user'
//        TestContainer.getInstance().getTestContext().useAdminForRequests();
//        DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy );
    }

    // @Test
    // public void testPost()
    // {
    // // the Wagon deploys using the PUT method
    // }

    // @Test
    // public void testPut()
    // {
    // // This is covered in Nexus429WagonDeployPrivilegeTest.
    // }

    @Test
    public void deleteTest()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        try
        {
            this.deleteFromRepository( this.getTestId() + "/" );
            Assert.fail( "Artifact should not have been deleted" );
        }
        catch ( IOException e )
        {
        }

        this.giveUserPrivilege( "test-user", "T7" );

        // this method validates also
        this.deleteFromRepository( this.getTestId() + "/" );
    }

    @Test
    public void readTest()
    {

    }

}
