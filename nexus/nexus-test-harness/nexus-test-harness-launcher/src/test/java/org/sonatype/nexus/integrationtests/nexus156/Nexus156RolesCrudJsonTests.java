package org.sonatype.nexus.integrationtests.nexus156;

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
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus156RolesCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    // this is not a great use of a super class, but its really easy, and its only a test class.
    protected XStream xstream;

    protected MediaType mediaType;

    public Nexus156RolesCrudJsonTests()
    {
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @Test
    public void createTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Create Test Role" );
        resource.setName( "CreateRole" );
        resource.setSessionTimeout( 30 );
        resource.addPrivilege( "priv1" );
        resource.addPrivilege( "priv2" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

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

    public void readTest()
        throws IOException
    {

        RoleResource resource = new RoleResource();

        resource.setDescription( "Read Test Role" );
        resource.setName( "ReadRole" );
        resource.setSessionTimeout( 31 );
        resource.addPrivilege( "priv3" );
        resource.addPrivilege( "priv4" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        resource.setId( responseResource.getId() );

        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );

        response = this.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Role: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

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
        resource.addPrivilege( "priv5" );
        resource.addPrivilege( "priv4" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        resource.setId( responseResource.getId() );

        Assert.assertEquals( resource.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( resource.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( resource );

        // update the user
        // TODO: add tests that changes the Id
        resource.setName( "UpdateRole Again" );
        resource.setDescription( "Update Test Role Again" );
        resource.getPrivileges().clear(); // clear the privs
        resource.addPrivilege( "priv6" );
        resource.setSessionTimeout( 10 );

        response = this.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not update user: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

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
        resource.addPrivilege( "priv7" );
        resource.addPrivilege( "priv8" );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

        // make sure it was added
        SecurityConfigUtil.verifyRole( responseResource );

        // use the new ID
        response = this.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete User: " + response.getStatus() );
        }

        // TODO: check if deleted
        Assert.assertNull( SecurityConfigUtil.getCRole( responseResource.getId() ) );
    }

    private RoleResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        RoleResourceRequest roleResourceRequest =
            (RoleResourceRequest) representation.getPayload( new RoleResourceRequest() );

        return roleResourceRequest.getData();
    }

    private Response sendMessage( Method method, RoleResource resource )
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String roleId = ( method == Method.POST ) ? "" : "/" + resource.getId();

        String serviceURI = this.getBaseNexusUrl() + "service/local/roles" + roleId;
        System.out.println( "serviceURI: " + serviceURI );

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( method );

        RoleResourceRequest userRequest = new RoleResourceRequest();
        userRequest.setData( resource );

        // now set the payload
        representation.setPayload( userRequest );
        request.setEntity( representation );

        Client client = new Client( Protocol.HTTP );

        return client.handle( request );
    }

}
