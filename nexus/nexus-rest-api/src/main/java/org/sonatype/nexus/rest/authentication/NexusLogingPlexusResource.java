package org.sonatype.nexus.rest.authentication;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.authentication.AbstractLoginPlexusResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author bdemers
 */
@Component( role = PlexusResource.class, hint = "LoginPlexusResource" )
@Path( AbstractLoginPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class NexusLogingPlexusResource
    extends AbstractLoginPlexusResource
{
    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // this is the ONLY resource using authcNxBasic, as the UI can't receive 401 errors from teh server
        // as the browser login pops up, which is no good in this case
        return new PathProtectionDescriptor( getResourceUri(), "authcNxBasic,perms[nexus:authentication]" );
    }
    
    /**
     * Login to the application, will return a set of permissions available to the specified user.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = AuthenticationLoginResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return super.get( context, request, response, variant );
    }
}
