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
                                 MediaType.APPLICATION_JSON, this.getBaseNexusUrl() );
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
