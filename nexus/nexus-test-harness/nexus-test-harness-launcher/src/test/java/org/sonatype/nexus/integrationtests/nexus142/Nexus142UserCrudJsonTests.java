package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus142UserCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    // this is not a great use of a super class, but its really easy, and its only a test class.
    protected XStream xstream;

    protected MediaType mediaType;

    public Nexus142UserCrudJsonTests()
    {
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @Test
    public void createTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Create User" );
        resource.setUserId( "createUser" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );
    }
    
    
    public void readTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Read User" );
        resource.setUserId( "readUser" );
        resource.setStatus( "expired" );
        resource.setEmail( "read@user.com" );
        resource.addRole( "role1" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );
        
        
        response = this.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }
        
        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( "expired", responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );
    }
    

    @Test
    public void updateTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Update User" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "expired" );
        resource.setEmail( "updateUser@user.com" );
        resource.addRole( "role1" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );

        // update the user
        // TODO: add tests that changes the userId
        resource.setName( "Update UserAgain" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "expired" );
        resource.setEmail( "updateUser@user2.com" );
        resource.getRoles().clear();
        resource.addRole( "role2" );

        response = this.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not update user: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Delete User" );
        resource.setUserId( "deleteUser" );
        resource.setStatus( "expired" );
        resource.setEmail( "deleteUser@user.com" );
        resource.addRole( "role2" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure it was added
        SecurityConfigUtil.verifyUser( responseResource );

        // use the new ID
        response = this.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete User: " + response.getStatus() );
        }

        SecurityConfigUtil.verifyUsers( new ArrayList<UserResource>() );
    }

    private UserResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        UserResourceRequest resourceResponse =
            (UserResourceRequest) representation.getPayload( new UserResourceRequest() );

        return resourceResponse.getData();
    }

    private Response sendMessage( Method method, UserResource resource )
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String userId = ( method == Method.POST ) ? "" : "/" + resource.getUserId();

        String serviceURI = this.getBaseNexusUrl() + "service/local/users" + userId;
        System.out.println( "serviceURI: " + serviceURI );

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( method );

        UserResourceRequest userRequest = new UserResourceRequest();
        userRequest.setData( resource );

        // now set the payload
        representation.setPayload( userRequest );
        request.setEntity( representation );

        Client client = new Client( Protocol.HTTP );

        return client.handle( request );
    }

}
