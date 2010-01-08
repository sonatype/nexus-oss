package org.sonatype.nexus.buup.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.buup.NexusUpgradeException;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "StartUpgradeResource" )
public class StartUpgradeResource
    extends AbstractBuupPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/buup/startUpgrade";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:status]" );
    }

    /**
     * Getting this resource -- if all conditions are met -- will kill JVM, hence NO RESPONSE will be sent! If the
     * process is unsuccesful, HTTP 400 is returned.
     * 
     * @TODO: detailed message why HTTP 400
     */
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            nexusBuupPlugin.initiateUpgradeProcess();

            // not returned, since above this JVM is killed OR an exception is thrown
            return new StringRepresentation( "BUUP INVOKED?" );
        }
        catch ( NexusUpgradeException e )
        {
            // this is wrong user interaction (like some FS check failed or bundle is not downloaded)
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e );
        }
        catch ( NexusBuupInvocationException e )
        {
            // this is internal server error!
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Could not invoke BUUP!", e );
        }
    }
}
