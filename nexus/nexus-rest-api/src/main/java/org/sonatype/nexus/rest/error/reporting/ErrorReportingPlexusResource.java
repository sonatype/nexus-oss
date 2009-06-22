package org.sonatype.nexus.rest.error.reporting;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ErrorReportingPlexusResource" )
public class ErrorReportingPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private ErrorReportingManager manager;
    
    public ErrorReportingPlexusResource()
    {
        setReadable( false );
        setModifiable( true );
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
        return new PathProtectionDescriptor( "/error_reporting", "authcBasic,perms[nexus:errorreporting]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/error_reporting";
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
            
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        
        return null;
    }
}
