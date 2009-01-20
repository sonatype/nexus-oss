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
import org.sonatype.nexus.rest.model.PlexusUserListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PlexusUserResourceResponse;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class UserMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( UserMessageUtil.class );

    public UserMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public UserResource createUser( UserResource user )
        throws IOException
    {

        Response response = this.sendMessage( Method.POST, user );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create user: " + response.getStatus() + ":\n" + responseText );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( "User ID shouldn't be null: " + response.getEntity().getText(),
                              responseResource.getUserId() );
        user.setUserId( responseResource.getUserId() );

        Assert.assertEquals( user.getName(), responseResource.getName() );
        Assert.assertEquals( user.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( user.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( user.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( user.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( user );

        return user;
    }

    public UserResource getUser( String userId )
        throws IOException
    {

        String responseText = RequestFacade.doGetRequest( "service/local/users/" + userId ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        UserResourceRequest resourceResponse =
            (UserResourceRequest) representation.getPayload( new UserResourceRequest() );

        return resourceResponse.getData();
    }

    public UserResource updateUser( UserResource user )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, user );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update user: " + response.getStatus() + "\n" + responseText );
        }

        // get the Resource object
        UserResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( user.getName(), responseResource.getName() );
        Assert.assertEquals( user.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( user.getStatus(), responseResource.getStatus() );
        Assert.assertEquals( user.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( user.getRoles(), responseResource.getRoles() );

        SecurityConfigUtil.verifyUser( user );
        return responseResource;
    }

    public Response sendMessage( Method method, UserResource resource )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String userId = ( method == Method.POST ) ? "" : "/" + resource.getUserId();

        String serviceURI = "service/local/users" + userId;

        UserResourceRequest userRequest = new UserResourceRequest();
        userRequest.setData( resource );

        // now set the payload
        representation.setPayload( userRequest );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     *
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<UserResource> getList()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/users" );
        String responseText = response.getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        // must use the XML xstream even if we 'thought' we wanted to use JSON, because REST client doesn't listen to
        // the MediaType in some situations.
        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        // make sure we have a success
        Assert.assertTrue( "Status: " + response.getStatus() + "\n" + responseText, response.getStatus().isSuccess() );

        UserListResourceResponse resourceResponse =
            (UserListResourceResponse) representation.getPayload( new UserListResourceResponse() );

        return resourceResponse.getData();

    }

    public UserResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        UserResourceRequest resourceResponse =
            (UserResourceRequest) representation.getPayload( new UserResourceRequest() );

        return resourceResponse.getData();
    }

    public Object parseResponseText( String responseString, Object responseType )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        return representation.getPayload( responseType );
    }

    @SuppressWarnings( "unchecked" )
    public List<PlexusUserResource> getPlexusUsers( String source )
        throws IOException
    {
        // plexus_users
        String uriPart = RequestFacade.SERVICE_LOCAL + "plexus_users/" + source;

        Response response =
            RequestFacade.sendMessage( uriPart, Method.GET, new StringRepresentation( "", this.mediaType ) );
        String responseString = response.getEntity().getText();
        Assert.assertTrue( "Status: " + response.getStatus() + "\nResponse:\n" + responseString,
                           response.getStatus().isSuccess() );

        PlexusUserListResourceResponse result =
            (PlexusUserListResourceResponse) this.parseResponseText( responseString,
                                                                     new PlexusUserListResourceResponse() );

        return result.getData();
    }

    public PlexusUserResource getPlexusUser( String source, String userId )
        throws IOException
    {
        String sourcePart = ( source != null ) ? source + "/" : "";

        // plexus_user
        String uriPart = RequestFacade.SERVICE_LOCAL + "plexus_user/" + sourcePart + userId;

        Response response =
            RequestFacade.sendMessage( uriPart, Method.GET, new StringRepresentation( "", this.mediaType ) );
        String responseString = response.getEntity().getText();
        Assert.assertTrue( "Status: " + response.getStatus() + "\nResponse:\n" + responseString,
                           response.getStatus().isSuccess() );

        PlexusUserResourceResponse result =
            (PlexusUserResourceResponse) this.parseResponseText( responseString, new PlexusUserResourceResponse() );

        return result.getData();
    }

    @SuppressWarnings( "unchecked" )
    public List<PlexusUserResource> searchPlexusUsers( String source, String userId )
        throws IOException
    {
        // user_search
        String uriPart = RequestFacade.SERVICE_LOCAL + "user_search/" + source + "/" + userId;

        Response response =
            RequestFacade.sendMessage( uriPart, Method.GET, new StringRepresentation( "", this.mediaType ) );
        String responseString = response.getEntity().getText();
        Assert.assertTrue( "Status: " + response.getStatus() + "\nResponse:\n" + responseString,
                           response.getStatus().isSuccess() );

        PlexusUserListResourceResponse result =
            (PlexusUserListResourceResponse) this.parseResponseText( responseString,
                                                                     new PlexusUserListResourceResponse() );

        return result.getData();
    }

    /**
     * @param userId
     * @return Returns true when the user was deleted and false when it was not deleted
     * @throws Exception
     */
    public static boolean removeUser( String userId )
        throws IOException
    {

        if ( "anonymous".equals( userId ) )
        {
            throw new IllegalArgumentException( "Unable to delete anonymous user" );
        }

        Status status = RequestFacade.sendMessage( "service/local/users/" + userId, Method.DELETE ).getStatus();
        return status.isSuccess();
    }

    /**
     * @param userId user to be disable
     * @return returns the disabled user instance
     * @throws IOException
     */
    public UserResource disableUser( String userId )
        throws IOException
    {
        UserResource user = getUser( "anonymous" );
        user.setStatus( "disabled" );
        updateUser( user );
        return user;
    }

}
