package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class UserMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

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
        System.out.println( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

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
        String responseText = RequestFacade.doGetRequest( "service/local/users" ).getEntity().getText();
        System.out.println( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        UserListResourceResponse resourceResponse =
            (UserListResourceResponse) representation.getPayload( new UserListResourceResponse() );

        return resourceResponse.getData();

    }

    public UserResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        UserResourceRequest resourceResponse =
            (UserResourceRequest) representation.getPayload( new UserResourceRequest() );

        return resourceResponse.getData();
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
