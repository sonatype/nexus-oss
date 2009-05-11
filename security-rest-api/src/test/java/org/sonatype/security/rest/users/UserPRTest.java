package org.sonatype.security.rest.users;

import junit.framework.Assert;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;

public class UserPRTest
    extends AbstractSecurityRestTest
{

    public void testAddUser()
        throws Exception
    {

        PlexusResource resource = this.lookup( PlexusResource.class, "UserListPlexusResource" );

        UserResourceRequest resourceRequest = new UserResourceRequest();
        UserResource userResource = new UserResource();
        resourceRequest.setData( userResource );
        userResource.setEmail( "test@test.com" );
        userResource.setName( "testAddUser" );
        userResource.setStatus( "active" );
        userResource.setUserId( "testAddUser" );
        userResource.addRole( "admin" );

        try
        {

            resource.post( null, this.buildRequest(), null, resourceRequest );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse errorResponse = (ErrorResponse) e.getResultObject();
            ErrorMessage errorMessage = (ErrorMessage) errorResponse.getErrors().get( 0 );
            Assert.fail( e.getMessage() + ": " + errorMessage.getMsg() );
        }

        // now list
        resource.get( null, this.buildRequest(), null, null );

    }

    public void testUpdateUserValidation()
        throws Exception
    {
        // test user creation with NO status

        // add a user
        PlexusResource resource = this.lookup( PlexusResource.class, "UserListPlexusResource" );

        UserResourceRequest resourceRequest = new UserResourceRequest();
        UserResource userResource = new UserResource();
        resourceRequest.setData( userResource );
        userResource.setEmail( "testUpdateUserValidation@test.com" );
        userResource.setName( "testUpdateUserValidation" );
        userResource.setStatus( "active" );
        userResource.setUserId( "testUpdateUserValidation" );
        userResource.addRole( "admin" );

        resource.post( null, this.buildRequest(), null, resourceRequest );

        // remove the status
        userResource.setStatus( "" );

        resource = this.lookup( PlexusResource.class, "UserPlexusResource" );
        try
        {
            resource.put( null, this.buildRequest(), null, resourceRequest );
            Assert.fail( "expected PlexusResourceException" );
        }
        catch ( PlexusResourceException e )
        {
            // expected
        }

    }

    private Request buildRequest()
    {
        Request request = new Request();

        Reference ref = new Reference( "http://localhost:12345/" );

        request.setRootRef( ref );
        request.setResourceRef( new Reference( ref, "users" ) );

        return request;
    }

}
