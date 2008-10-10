package org.sonatype.nexus.rest.users;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * @author tstevens
 * @plexus.component role-hint="UserListPlexusResource"
 */
public class UserListPlexusResource
    extends AbstractUserPlexusResource
{

    public UserListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/users";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:users]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        UserListResourceResponse result = new UserListResourceResponse();

        for ( SecurityUser user : getNexusSecurity(request).listUsers() )
        {
            UserResource res = nexusToRestModel( user, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserResourceRequest requestResource = (UserResourceRequest) payload;
        UserResourceResponse result = null;

        if ( requestResource != null )
        {
            UserResource resource = requestResource.getData();

            SecurityUser user = restToNexusModel( null, resource );

            try
            {
                getNexusSecurity( request ).createUser( user );

                result = new UserResourceResponse();

                // Update the status, as that may have changed
                resource.setStatus( user.getStatus() );

                resource.setResourceURI( createChildReference( request, resource.getUserId() ).toString() );

                result.setData( resource );

            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return result;
    }

}
