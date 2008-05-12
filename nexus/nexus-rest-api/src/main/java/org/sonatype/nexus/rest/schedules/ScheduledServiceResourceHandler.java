package org.sonatype.nexus.rest.schedules;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;

public class ScheduledServiceResourceHandler
    extends AbstractScheduledServiceResourceHandler
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getScheduledServiceId()
    {
        return getRequest().getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }

    /**
     * We are handling HTTP GET's.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository route representation.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No such scheduled service" );

        return null;
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a repository route.
     */
    public void put( Representation representation )
    {
        ScheduledServiceResourceResponse response = (ScheduledServiceResourceResponse) deserialize( new ScheduledServiceResourceResponse() );

        if ( response != null )
        {
            ScheduledServiceBaseResource resource = response.getData();
            
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a repository route.
     */
    public void delete()
    {
        getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
    }
}
