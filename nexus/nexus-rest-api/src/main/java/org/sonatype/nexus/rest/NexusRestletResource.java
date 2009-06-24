package org.sonatype.nexus.rest;

import java.io.IOException;
import java.util.logging.Level;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.RestletResource;

public class NexusRestletResource
    extends RestletResource
{
    public NexusRestletResource( Context context, Request request, Response response, PlexusResource delegate )
    {
        super( context, request, response, delegate );
    }
    
    @Override
    public Representation represent( Variant variant )
        throws ResourceException
    {
        try
        {
            return super.represent( variant );
        }
        catch ( ResourceException e )
        {
            if ( Status.isServerError( e.getStatus().getCode() ) )
            {
                handleError( e );
            }
            
            throw e;
        }
        catch ( RuntimeException e )
        {
            handleError( e );
            
            throw e;
        }
    }
    
    @Override
    public void acceptRepresentation( Representation representation )
        throws ResourceException
    {
        try
        {
            super.acceptRepresentation( representation );
        }
        catch ( ResourceException e )
        {
            if ( Status.isServerError( e.getStatus().getCode() ) )
            {
                handleError( e );
            }
            
            throw e;
        }
        catch ( RuntimeException e )
        {
            handleError( e );
            
            throw e;
        }
    }
    
    @Override
    public void storeRepresentation( Representation representation )
        throws ResourceException
    {
        try
        {
            super.storeRepresentation( representation );
        }
        catch ( ResourceException e )
        {
            if ( Status.isServerError( e.getStatus().getCode() ) )
            {
                handleError( e );
            }
            
            throw e;
        }
        catch ( RuntimeException e )
        {
            handleError( e );
            
            throw e;
        }
    }
    
    @Override
    public void removeRepresentations()
        throws ResourceException
    {
        try
        {
            super.removeRepresentations();
        }
        catch ( ResourceException e )
        {
            if ( Status.isServerError( e.getStatus().getCode() ) )
            {
                handleError( e );
            }
            
            throw e;
        }
        catch ( RuntimeException e )
        {
            handleError( e );
            
            throw e;
        }
    }
    
    protected void handleError( Throwable throwable )
    {
        ErrorReportingManager manager = ( ErrorReportingManager ) getContext().getAttributes().get( ErrorReportingManager.class.getName() );
        
        if ( manager != null )
        {
            ErrorReportRequest request = new ErrorReportRequest();
            
            request.getContext().putAll( getContext().getAttributes() );
            
            request.setThrowable( throwable );
            
            try
            {
                manager.handleError( request );
            }
            catch ( IssueSubmissionException e )
            {
                getLogger().log( Level.SEVERE, "Unable to submit error report to jira", e );
            }
            catch ( IOException e )
            {
                getLogger().log( Level.SEVERE, "Unable to submit error report to jira", e );
            }
        }
    }
}
