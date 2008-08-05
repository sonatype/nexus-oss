package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus156RolesValidationTests extends AbstractNexusIntegrationTest
{
    
    protected RoleMessageUtil messageUtil;

    public Nexus156RolesValidationTests()
    {
        this.messageUtil =
            new RoleMessageUtil( XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                 MediaType.APPLICATION_JSON );
    }

    @Test
    public void roleWithNoPrivsTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoPrivsTest" );
        resource.setName( "roleWithNoPrivsTest" );
        resource.setSessionTimeout( 30 );
//        resource.addPrivilege( "priv1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void roleWithNoName()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoName" );
//        resource.setName( "roleWithNoName" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void createWithNoTimeout()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "roleWithNoName" );
        resource.setName( "roleWithNoName" );
//        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void createRecursiveContainment()
        throws IOException
    {
        RoleResource resourceA = new RoleResource();
        resourceA.setName( "recursive1" );
        resourceA.setSessionTimeout( 60 );
        resourceA.addPrivilege( "1" );
        
        Response response = this.messageUtil.sendMessage( Method.POST, resourceA );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceA = this.messageUtil.getResourceFromResponse( response );
        
        RoleResource resourceB = new RoleResource();
        resourceB = new RoleResource();
        resourceB.setName( "recursive2" );
        resourceB.setSessionTimeout( 60 );
        resourceB.addRole( responseResourceA.getId() );
        
        response = this.messageUtil.sendMessage( Method.POST, resourceB );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceB = this.messageUtil.getResourceFromResponse( response );
        
        RoleResource resourceC = new RoleResource();
        resourceC = new RoleResource();
        resourceC.setName( "recursive2" );
        resourceC.setSessionTimeout( 60 );
        resourceC.addRole( responseResourceB.getId() );
        
        response = this.messageUtil.sendMessage( Method.POST, resourceC );
        
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should have been created: " + response.getStatus() );
        }
        
        // get the Resource object
        RoleResource responseResourceC = this.messageUtil.getResourceFromResponse( response );
        
        resourceA.setId( responseResourceA.getId() );
        resourceA.getRoles().clear();
        resourceA.addRole( responseResourceC.getId() );
        
        response = this.messageUtil.sendMessage( Method.PUT, resourceA );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
    }
    
    @Test
    public void updateValidationTests() throws IOException
    {
        RoleResource resource = new RoleResource();

        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        resource.setId( responseResource.getId() );

        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );

        
        /*
         * NO Name
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( null );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );


        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );        
        
        
        
        /*
         * NO Privs
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.getPrivileges().clear();


        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
        /*
         * INVALID Privs
         */
        resource.setDescription( "updateValidationTests" );
        resource.setName( "updateValidationTests" );
        resource.setSessionTimeout( 99999 );
        resource.getPrivileges().clear();
        resource.getPrivileges().add( "junk" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Role should not have been updated: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );
        
    }
    
    
}
