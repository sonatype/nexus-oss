package org.sonatype.nexus.rest.status;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;

public class CommandResourceHandler
    extends AbstractNexusResourceHandler
{
    public CommandResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().clear();

        getVariants().add( new Variant( MediaType.TEXT_PLAIN ) );
    }
    
    public boolean allowGet()
    {
        return false;
    }

    public boolean allowPut()
    {
        return true;
    }

    public void put( Representation representation )
    {
        boolean result = false;

        try
        {
            String cmd = representation.getText();

            if ( "START".equalsIgnoreCase( cmd ) )
            {
                result = getNexus().setState( SystemState.STARTED );
            }
            else if ( "STOP".equalsIgnoreCase( cmd ) )
            {
                result = getNexus().setState( SystemState.STOPPED );
            }
            else if ( "RESTART".equalsIgnoreCase( cmd ) )
            {
                // if running stop it
                if ( SystemState.STARTED.equals( getNexus().getSystemStatus().getState() ) )
                {
                    getNexus().setState( SystemState.STOPPED );
                }

                // and start it
                result = getNexus().setState( SystemState.STARTED );
            }
            else
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Unknown COMMAND!" );

                return;
            }

            if ( result )
            {
                getResponse().setStatus( Status.SUCCESS_OK, "OK" );
            }
            else
            {
                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Could not change Nexus state to submitted one! (check logs for more info)" );
            }
        }
        catch ( IllegalArgumentException e )
        {
            getResponse().setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Could not change Nexus state to submitted one! (check logs for more info)" );
        }
        catch ( IOException e )
        {
            getLogger().log( Level.WARNING, "Got IOException during command processing!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }
    }
}
