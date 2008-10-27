package org.sonatype.nexus.rest.authentication;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "LoginPlexusResource" )
public class LoginPlexusResource
    extends AbstractUIPermissionCalculatingPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/authentication/login";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:authentication]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        AuthenticationLoginResource resource = new AuthenticationLoginResource();

        resource.setClientPermissions( getClientPermissionsForCurrentUser( request ) );

        AuthenticationLoginResourceResponse result = new AuthenticationLoginResourceResponse();

        result.setData( resource );

        return result;
    }

}
