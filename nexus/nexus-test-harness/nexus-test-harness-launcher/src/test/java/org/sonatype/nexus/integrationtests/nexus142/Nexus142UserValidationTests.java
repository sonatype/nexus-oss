package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;

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

public class Nexus142UserValidationTests
    extends AbstractNexusIntegrationTest
{

    protected UserMessageUtil messageUtil;

    public Nexus142UserValidationTests()
    {
        this.messageUtil =
            new UserMessageUtil( XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                 MediaType.APPLICATION_JSON, this.getBaseNexusUrl() );
    }
    
    @Test
    public void createUserWithNoRoles() throws IOException
    {
        
        UserResource resource = new UserResource();

        resource.setName( "createUserWithNoRoles" );
        resource.setUserId( "createUserWithNoRoles" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        // no roles
//        resource.addRole( "role1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
    }
    
    @Test
    public void createUserWithNoUserId() throws IOException
    {
        
        UserResource resource = new UserResource();

        resource.setName( "createUserWithNoUserId" );
//        resource.setUserId( "createUserWithNoUserId" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
    }
    
    @Test
    public void createUserWithNoUserName() throws IOException
    {
        
        UserResource resource = new UserResource();

//        resource.setName( "createUserWithNoUserName" );
        resource.setUserId( "createUserWithNoUserName" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
    }
    
    
    @Test
    public void createUserWithNoEmail() throws IOException
    {
        
        UserResource resource = new UserResource();

        resource.setName( "createUserWithNoEmail" );
        resource.setUserId( "createUserWithNoEmail" );
        resource.setStatus( "expired" );
//        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    
    @Test
    public void createUserInvalidRole() throws IOException
    {
        
        UserResource resource = new UserResource();

        resource.setName( "createUserInvalidRole" );
        resource.setUserId( "createUserInvalidRole" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "INVALID-ROLE" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    
    @Test
    public void updateValidation()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "updateValidation" );
        resource.setUserId( "updateValidation" );
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

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( resource.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( resource );
        
     // update the user

        resource.setName( "updateValidation" );
        resource.setUserId( "updateValidation" );
        resource.setStatus( "expired" );
        resource.setEmail( "" );
        resource.addRole( "role1" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        
        /**
         * NO STATUS
         */
        resource.setName( "updateValidation" );
        resource.setUserId( "updateValidation" );
        resource.setStatus( "" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        
        /**
         * NO ROLES
         */
        resource.setName( "updateValidation" );
        resource.setUserId( "updateValidation" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.getRoles().clear();

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        
        
        
        /**
         * INVALID ROLE
         */
        resource.setName( "updateValidation" );
        resource.setUserId( "updateValidation" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "INVALID_ROLE" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        
        
        
        
        /**
         * NO NAME
         */
        resource.setName( "" );
        resource.setUserId( "updateValidation" );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        

        
        
        /**
         * NO USER ID
         */
        resource.setName( "updateValidation" );
        resource.setUserId( null );
        resource.setStatus( "expired" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );
        

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "User should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    

}
