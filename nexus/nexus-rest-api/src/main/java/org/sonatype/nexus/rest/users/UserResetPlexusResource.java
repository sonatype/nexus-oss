package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserResetPlexusResource" )
public class UserResetPlexusResource
    extends AbstractUserPlexusResource
{

    public UserResetPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/users_reset/{" + USER_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users_reset/*", "authcBasic,perms[nexus:usersreset]" );
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {

        String userId = request.getAttributes().get( USER_ID_KEY ).toString();

        try
        {
            if ( !isAnonymousUser( userId, request ) )
            {
                getNexusSecurity().resetPassword( userId );

                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            else
            {
                getLogger().debug( "Anonymous user password reset is blocked!" );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot reset password!" );
            }
        }
        catch ( NoSuchUserException e )
        {
            getLogger().debug( "Invalid userid: " + userId, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "User ID not found!" );
        }
    }

}
