package org.sonatype.nexus.integrationtests.nexus477;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

        // URLConnection.set

        // use the test-user
        // this.giveUserPrivilege( "test-user", "T3" ); // the Wagon does a PUT not a POST, so this is correct
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        this.resetTestUserPrivs();

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
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
        // "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + this.getTestId() + "/";
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId();

         Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
         Assert.assertEquals( "Artifact should not have been deleted", 401, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T7" );
        
        // delete implies read
        // we need to check read first...
        response = RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav ), Method.GET );
        Assert.assertEquals( "Could not get artifact", 200, response.getStatus().getCode() );

         response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
         Assert.assertEquals( "Artifact should have been deleted", 204, response.getStatus().getCode() );

    }

    @Test
    public void readTest()
        throws IOException, URISyntaxException, HttpException
    {

        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav );

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should not have been read", 401, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T1" );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should have been read\nresponse:\n"+ response.getEntity().getText(), 200, response.getStatus().getCode());

        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 401, response.getStatus().getCode() );
        
    }

}
