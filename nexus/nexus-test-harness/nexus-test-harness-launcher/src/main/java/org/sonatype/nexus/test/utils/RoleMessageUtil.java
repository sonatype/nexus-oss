/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleListResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;

import com.thoughtworks.xstream.XStream;

public class RoleMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( RoleMessageUtil.class );

    public RoleMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RoleResource createRole( RoleResource role )
        throws IOException
    {
        Response response = this.sendMessage( Method.POST, role );
        String responseString = response.getEntity().getText();

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() + " - " + responseString );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( responseString );
        
        // make sure the id != null
        Assert.assertNotNull( "Result:\n"+ this.xStream.toXML( responseResource ), responseResource.getId() );

        if ( role.getId() != null )
        {
            Assert.assertEquals( role.getId(), responseResource.getId() );
        }

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
            Assert.fail( "Could not find role: " + roleId + " got: " + response.getStatus() );
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
        
        Response response = RequestFacade.doGetRequest( "service/local/roles" );
        
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Request failed: "+ response.getStatus() +"\n"+ responseText, response.getStatus().isSuccess() );
        
        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RoleListResourceResponse resourceResponse =
            (RoleListResourceResponse) representation.getPayload( new RoleListResourceResponse() );

        return resourceResponse.getData();

    }

    public RoleResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        return getResourceFromResponse( responseString );
    }

    public RoleResource getResourceFromResponse( String responseString )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        RoleResourceRequest roleResourceRequest =
            (RoleResourceRequest) representation.getPayload( new RoleResourceRequest() );

        return roleResourceRequest.getData();
    }

    private static XStream xStream;

    static
    {
        xStream = XStreamFactory.getXmlXStream();
    }

    public static Status update( RoleResource role )
        throws IOException
    {
        RoleResourceRequest request = new RoleResourceRequest();
        request.setData( role );

        XStreamRepresentation representation = new XStreamRepresentation( xStream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        String serviceURI = "service/local/roles/" + role.getId();
        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );

        return response.getStatus();
    }

    public RoleResource findRole( String roleId )
        throws IOException
    {
        Response response = this.sendMessage( Method.GET, null, roleId );

        if ( !response.getStatus().isSuccess() )
        {
            return null;
        }

        // get the Resource object
        return this.getResourceFromResponse( response );
    }

    @SuppressWarnings( "unchecked" )
    public List<ExternalRoleMappingResource> getExternalRoleMap( String source )
        throws IOException
    {
        // external_role_map
        String uriPart = RequestFacade.SERVICE_LOCAL + "external_role_map/" + source;

        Response response =
            RequestFacade.sendMessage( uriPart, Method.GET, new StringRepresentation( "", this.mediaType ) );
        String responseString = response.getEntity().getText();
        Assert.assertTrue( "Status: " + response.getStatus() + "\nResponse:\n" + responseString,
                           response.getStatus().isSuccess() );

        ExternalRoleMappingResourceResponse result =
            (ExternalRoleMappingResourceResponse) this.parseResponseText( responseString,
                                                                          new ExternalRoleMappingResourceResponse() );

        return result.getData();
    }

    @SuppressWarnings( "unchecked" )
    public List<PlexusRoleResource> getRoles( String source )
        throws IOException
    {
        // plexus_roles
        String uriPart = RequestFacade.SERVICE_LOCAL + "plexus_roles/" + source;

        Response response =
            RequestFacade.sendMessage( uriPart, Method.GET, new StringRepresentation( "", this.mediaType ) );
        String responseString = response.getEntity().getText();
        Assert.assertTrue( "Status: " + response.getStatus() + "\nResponse:\n" + responseString,
                           response.getStatus().isSuccess() );

        LOG.debug( "response: " + responseString );

        PlexusRoleListResourceResponse result =
            (PlexusRoleListResourceResponse) this.parseResponseText( responseString,
                                                                     new PlexusRoleListResourceResponse() );

        return result.getData();
    }

    public Object parseResponseText( String responseString, Object responseType )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        return representation.getPayload( responseType );
    }

}
