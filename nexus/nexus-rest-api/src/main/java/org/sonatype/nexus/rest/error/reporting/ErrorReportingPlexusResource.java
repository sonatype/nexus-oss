package org.sonatype.nexus.rest.error.reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ErrorReportRequest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.ErrorReportResponseDTO;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ErrorReportingPlexusResource" )
public class ErrorReportingPlexusResource
    extends AbstractNexusPlexusResource
{
    public ErrorReportingPlexusResource()
    {
        setModifiable( true );
        setReadable( false );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new ErrorReportRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/error_reporting", "authcBasic,perms[nexus:settings]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/error_reporting";
    }
    
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        ErrorReportRequest dto = ( ErrorReportRequest ) payload;
        
        if ( StringUtils.isBlank( dto.getData().getTitle() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "A Title for the report is required." );
        }
        
        ErrorReportingManager manager =
            (ErrorReportingManager) context.getAttributes().get( ErrorReportingManager.class.getName() );

        if ( manager == null )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to retrieve error reporting manager." );
        }
        
        org.sonatype.nexus.error.reporting.ErrorReportRequest genReq = new org.sonatype.nexus.error.reporting.ErrorReportRequest();
        genReq.setTitle( dto.getData().getTitle() );
        genReq.setDescription( dto.getData().getDescription() );
        genReq.getContext().putAll( context.getAttributes() );
        
        try
        {
            ErrorReportResponse dtoResponse = new ErrorReportResponse();
            dtoResponse.setData( new ErrorReportResponseDTO() );
            
            org.sonatype.nexus.error.reporting.ErrorReportResponse genRes = manager.handleError( genReq );
            
            if ( !genRes.isSuccess() )
            {
                getLogger().debug( "Unable to submit jira ticket." );
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to submit jira ticket." );
            }
            
            dtoResponse.getData().setJiraUrl( genRes.getJiraUrl() );
            
            return dtoResponse;
        }
        catch ( IssueSubmissionException e )
        {
            getLogger().debug( "Unable to submit jira ticket.", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to submit jira ticket.", e );
        }
        catch ( IOException e )
        {
            getLogger().debug( "Unable to submit jira ticket.", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to submit jira ticket.", e );
        }
        catch ( GeneralSecurityException e )
        {
            getLogger().debug( "Unable to submit jira ticket.", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to submit jira ticket.", e );
        }
    }
}
