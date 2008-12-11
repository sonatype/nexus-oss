/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create role: " + response.getStatus() );
        }

        // get the Resource object
        RoleResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( responseResource.getId() );
        
        if( role.getId() != null)
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
        String responseText = RequestFacade.doGetRequest( "service/local/roles" ).getEntity().getText();

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
}
