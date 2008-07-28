package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus156RolesCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected RoleMessageUtil messageUtil;

    public Nexus156RolesCrudJsonTests()
    {
        this.messageUtil =
            new RoleMessageUtil( XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                 MediaType.APPLICATION_JSON, this.getBaseNexusUrl() );
    }

    @Test
    public void createTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role" );
        resource.setName( "CreateRole" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );
        resource.addPrivilege( "2" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        // set the id
        resource.setId( responseResource.getId() );

        SecurityConfigUtil.verifyRole( resource );
    }

    @Test
    public void listTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role" );
        resource.setName( "CreateRole" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "1" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // set the id
        resource.setId( responseResource.getId() );
        // make sure it was added
        SecurityConfigUtil.verifyRole( resource );

        // now that we have at least one element stored (more from other tests, most likely)

        // NEED to work around a GET problem with the REST client
        List<RoleResource> roles = this.messageUtil.getList();
        SecurityConfigUtil.verifyRoles( roles );

    }

    public void readTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Read Test Role" );
        resource.setName( "ReadRole" );
        resource.setSessionTimeout( 31 );
        resource.addPrivilege( "3" );
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

        response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Role: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getId(), responseResource.getId() );
        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );
    }

    @Test
    public void updateTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Update Test Role" );
        resource.setName( "UpdateRole" );
        resource.setSessionTimeout( 99999 );
        resource.addPrivilege( "5" );
        resource.addPrivilege( "4" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Role: " + response.getStatus() );
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

        // update the Role
        // TODO: add tests that changes the Id
        resource.setName( "UpdateRole Again" );
        resource.setDescription( "Update Test Role Again" );
        resource.getPrivileges().clear(); // clear the privs
        resource.addPrivilege( "6" );
        resource.setSessionTimeout( 10 );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not update Role: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getId(), responseResource.getId() );
        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );
    }

    @Test
    public void deleteTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Delete Test Role" );
        resource.setName( "deleteRole" );
        resource.setSessionTimeout( 1 );
        resource.addPrivilege( "7" );
        resource.addPrivilege( "8" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure it was added
        SecurityConfigUtil.verifyRole( responseResource );

        // use the new ID
        response = this.messageUtil.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Role: " + response.getStatus() );
        }

        // TODO: check if deleted
        Assert.assertNull( SecurityConfigUtil.getCRole( responseResource.getId() ) );
    }

}
