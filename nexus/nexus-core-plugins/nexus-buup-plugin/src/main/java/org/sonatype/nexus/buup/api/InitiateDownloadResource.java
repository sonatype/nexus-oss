package org.sonatype.nexus.buup.api;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.buup.NexusBuupPlugin;
import org.sonatype.nexus.buup.NexusUpgradeException;
import org.sonatype.nexus.buup.UpgradeProcessStatus;
import org.sonatype.nexus.buup.api.dto.UpgradeFormRequest;
import org.sonatype.nexus.buup.api.dto.UpgradeFormResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "InitiateDownloadResource" )
public class InitiateDownloadResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private NexusBuupPlugin nexusBuupPlugin;

    @Override
    public Object getPayloadInstance()
    {
        return new UpgradeFormRequest();
    }

    public InitiateDownloadResource()
    {
        super();

        setModifiable( true );
    }

    @Override
    public String getResourceUri()
    {
        return "/buup/initiateDownload";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:buup]" );
    }

    /**
     * Returns information about download status. See the enum for value descriptions. Also, response code of 200 or 201
     * shows is download in progress or not.
     */
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return getUpgradeFormResponse( context, request, response );
    }

    /**
     * Initiates checks and bundle upload, and sends form data along.
     */
    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        try
        {
            UpgradeFormRequest form = (UpgradeFormRequest) payload;

            if ( form != null )
            {
                nexusBuupPlugin.initiateBundleDownload( form );

                response.setStatus( Status.SUCCESS_ACCEPTED );

                return getUpgradeFormResponse( context, request, response );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_PRECONDITION_FAILED,
                    "You have to agree to terms and conditions!" );
            }
        }
        catch ( NexusUpgradeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e );
        }
    }

    // ==

    protected UpgradeFormResponse getUpgradeFormResponse( Context context, Request request, Response response )
    {
        UpgradeFormResponse result = new UpgradeFormResponse();

        UpgradeProcessStatus status = nexusBuupPlugin.getUpgradeProcessStatus();

        result.setUpgradeProcessStatus( status.name() );

        switch ( status )
        {
            case FAILED:
                for ( IOException e : nexusBuupPlugin.getFailureReasons() )
                {
                    result.getErrors().add( e.getMessage() );
                }

                break;
        }

        return result;

    }
}
