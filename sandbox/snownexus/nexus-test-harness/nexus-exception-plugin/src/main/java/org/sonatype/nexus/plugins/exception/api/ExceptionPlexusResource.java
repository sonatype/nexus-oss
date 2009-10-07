package org.sonatype.nexus.plugins.exception.api;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ExceptionPlexusResource" )
public class ExceptionPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    private ErrorReportingManager manager;
    
    public ExceptionPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/exception";
    }
    
    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        try
        {
            ErrorReportRequest req = new ErrorReportRequest();
            req.getContext().putAll( context.getAttributes() );
            req.getContext().putAll( request.getAttributes() );
            
            manager.assembleBundle( req );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to assemble bundle.", e );
            
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        
        return null;
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();
        
        int requestedStatus = Integer.parseInt( form.getFirstValue( "status" ) );
        
        throw new ResourceException( requestedStatus );
    }
}
