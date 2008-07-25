package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus142UserCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected UserMessageUtil messageUtil;
    
    public Nexus142UserCrudJsonTests()
    {
        this.messageUtil = new UserMessageUtil(XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ), MediaType.APPLICATION_JSON, this.getBaseNexusUrl());
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

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

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

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );
        
        
        response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }
        
        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

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

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

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

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not update user: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

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

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure it was added
        SecurityConfigUtil.verifyUser( responseResource );

        // use the new ID
        response = this.messageUtil.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete User: " + response.getStatus() );
        }

        SecurityConfigUtil.verifyUsers( new ArrayList<UserResource>() );
    }

}
