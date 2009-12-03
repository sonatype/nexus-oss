package org.sonatype.nexus.buup.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.buup.NexusBuupPlugin;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "StartUpgradeProcessResource" )
public class StartUpgradeProcessResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private NexusBuupPlugin nexusBuupPlugin;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/buup/start";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:status]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            if ( !nexusBuupPlugin.initiateUpgradeProcess() )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Cannot start upgrade process!" );
            }

            return new StringRepresentation( "BUUP INVOKED?" );
        }
        catch ( NexusBuupInvocationException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Could not invoke BUUP!", e );
        }
    }
}
