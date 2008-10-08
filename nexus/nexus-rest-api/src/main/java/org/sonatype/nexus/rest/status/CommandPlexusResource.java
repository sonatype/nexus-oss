package org.sonatype.nexus.rest.status;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

@Component( role = ManagedPlexusResource.class, hint = "CommandPlexusResource" )
public class CommandPlexusResource
    extends AbstractNexusPlexusResource
    implements ManagedPlexusResource
{

    public CommandPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new String();
    }

    @Override
    public String getResourceUri()
    {
        return "/status/command";
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        boolean result = false;

        try
        {
            String cmd = (String) payload;

            if ( "START".equalsIgnoreCase( cmd ) )
            {
                result = getNexusInstance( request ).setState( SystemState.STARTED );
            }
            else if ( "STOP".equalsIgnoreCase( cmd ) )
            {
                result = getNexusInstance( request ).setState( SystemState.STOPPED );
            }
            else if ( "RESTART".equalsIgnoreCase( cmd ) )
            {
                // if running stop it
                if ( SystemState.STARTED.equals( getNexusInstance( request ).getSystemStatus().getState() ) )
                {
                    getNexusInstance( request ).setState( SystemState.STOPPED );
                }

                // and start it
                result = getNexusInstance( request ).setState( SystemState.STARTED );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Unknown COMMAND!" );
            }

            if ( result )
            {
                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            else
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Could not change Nexus state to submitted one! (check logs for more info)" );
            }
        }
        catch ( IllegalArgumentException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Could not change Nexus state to submitted one! (check logs for more info)" );
        }
        // status is 204
        return null;
    }

}
