package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RoleListResourceResponse;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RoleMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    public RoleMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RoleResource createRole( RoleResource role )
        throws IOException
    {
        Response response = this.sendMessage( Method.POST, role );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );

        Assert.assertEquals( role.getDescription(), responseResource.getDescription() );
        Assert.assertEquals( role.getName(), responseResource.getName() );
        Assert.assertEquals( role.getSessionTimeout(), responseResource.getSessionTimeout() );
        Assert.assertEquals( role.getPrivileges(), responseResource.getPrivileges() );
        Assert.assertEquals( role.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyRole( responseResource );

        return responseResource;
    }

    public RoleResource getRole( String roleId )
        throws IOException
    {
        Response response = this.sendMessage( Method.GET, null, roleId );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        return this.getResourceFromResponse( response );
    }

    public Response sendMessage( Method method, RoleResource resource )
        throws IOException
    {
        return this.sendMessage( method, resource, resource.getId() );
    }

    private Response sendMessage( Method method, RoleResource resource, String id )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String roleId = ( method == Method.POST ) ? "" : "/" + id;

        String serviceURI = "service/local/roles" + roleId;

        if ( method == Method.POST || method == Method.PUT )
        {
            RoleResourceRequest userRequest = new RoleResourceRequest();
            userRequest.setData( resource );

            // now set the payload
            representation.setPayload( userRequest );
        }

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<RoleResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( "service/local/roles" ).getEntity().getText();

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        RoleListResourceResponse resourceResponse =
            (RoleListResourceResponse) representation.getPayload( new RoleListResourceResponse() );

        return resourceResponse.getData();

    }

    public RoleResource getResourceFromResponse( Response response )
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

}
