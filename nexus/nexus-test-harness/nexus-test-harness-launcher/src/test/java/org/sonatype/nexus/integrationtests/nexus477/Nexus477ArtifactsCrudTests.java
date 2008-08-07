package org.sonatype.nexus.integrationtests.nexus477;

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
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus477ArtifactsCrudTests
    extends AbstractPrivilegeTest
{

    @Before
    public void deployArtifact()
        throws IOException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException, ComponentLookupException
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );

        // Grab File used to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // Now if i deploy with admin first, that will cause problems when i try to deploy with the 'test-user'
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        int status = DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy );
        Assert.assertEquals( "Status", 201, status );
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
        this.printUserPrivs( "test-user" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
            "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + this.getTestId() + "/";

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Artifact should not have been deleted", 401, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T7" );

        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 200, response.getStatus().getCode() );
    }

    @Test
    public void readTest() throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
            "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + this.getTestId() + "/";

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should not have been read", 401, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T7" );

        response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should have been read", 200, response.getStatus().getCode() );
        System.out.println( "response: "+ response.getEntity().getText() );

    }

}
