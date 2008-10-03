package org.sonatype.nexus.rest.users;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.nexus.jsecurity.InvalidCredentialsException;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;

/**
 * @author tstevens
 * @plexus.component role-hint="UserChangePasswordPlexusResource"
 */
public class UserChangePasswordPlexusResource
    extends AbstractUserPlexusResource
{

    public UserChangePasswordPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new UserChangePasswordRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/users_changepw";
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserChangePasswordRequest changePasswordRequest = (UserChangePasswordRequest) payload;

        if ( changePasswordRequest != null )
        {
            UserChangePasswordResource resource = changePasswordRequest.getData();

            try
            {
                if ( !isAnonymousUser( resource.getUserId(), request ) )
                {
                    getNexusSecurity( request ).changePassword(
                        resource.getUserId(),
                        resource.getOldPassword(),
                        resource.getNewPassword() );

                    response.setStatus( Status.SUCCESS_ACCEPTED );
                }
                else
                {
                    response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot change password!" );

                    getLogger().debug( "Anonymous user password change is blocked!" );
                }
            }
            catch ( NoSuchUserException e )
            {
                getLogger().debug( "Invalid user ID!", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid credentials supplied." );

            }
            catch ( InvalidCredentialsException e )
            {
                getLogger().debug( "Invalid credentials!", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid credentials supplied." );
            }
        }
        // don't return anything because the status is a 202
        return null;
    }

}
