package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.jsecurity.NoSuchEmailException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserForgotIdPlexusResource" )
public class UserForgotIdPlexusResource
    extends AbstractUserPlexusResource
{

    public UserForgotIdPlexusResource()
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
        return "/users_forgotid/{" + USER_EMAIL_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/users_forgotid/*", "authcBasic,perms[nexus:usersforgotid]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String email = request.getAttributes().get( USER_EMAIL_KEY ).toString();
        try
        {
            getNexusSecurity().forgotUsername( email );

            response.setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( NoSuchEmailException e )
        {
            getLogger().debug( "Invalid email received: " + email, e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Email address not found!" );

        }
        // don't return anything because we are setting the status to 202
        return null;
    }

}
