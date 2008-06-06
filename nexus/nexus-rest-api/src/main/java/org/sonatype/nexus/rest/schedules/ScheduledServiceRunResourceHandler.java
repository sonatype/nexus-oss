package org.sonatype.nexus.rest.schedules;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;

public class ScheduledServiceRunResourceHandler extends AbstractScheduledServiceResourceHandler
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";
    private String scheduledServiceId;
    
    public ScheduledServiceRunResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
        this.scheduledServiceId = getRequest().getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }
    
    protected String getScheduledServiceId()
    {
        return this.scheduledServiceId;
    }
    
    public boolean allowGet()
    {
        return true;
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );
            //TODO: run the task
            getResponse().setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( NoSuchTaskException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }

        return null;
    }
}
