package org.sonatype.nexus.rest.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.rest.authentication.AbstractUIPermissionCalculatingPlexusResource;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

@Component( role = ManagedPlexusResource.class, hint = "StatusPlexusResource" )
public class StatusPlexusResource
    extends AbstractUIPermissionCalculatingPlexusResource implements ManagedPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/status";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {

        SystemStatus status = getNexusInstance( request ).getSystemStatus();

        StatusResource resource = new StatusResource();

        resource.setVersion( status.getVersion() );

        resource.setInitializedAt( status.getInitializedAt() );

        resource.setStartedAt( status.getStartedAt() );

        resource.setLastConfigChange( status.getLastConfigChange() );

        resource.setFirstStart( status.isFirstStart() );

        resource.setInstanceUpgraded( status.isInstanceUpgraded() );

        resource.setConfigurationUpgraded( status.isConfigurationUpgraded() );

        resource.setState( status.getState().toString() );

        resource.setOperationMode( status.getOperationMode().toString() );

        resource.setErrorCause( spit( status.getErrorCause() ) );

        if ( status.getConfigurationValidationResponse() != null )
        {
            resource.setConfigurationValidationResponse( new StatusConfigurationValidationResponse() );

            resource.getConfigurationValidationResponse().setValid(
                status.getConfigurationValidationResponse().isValid() );

            resource.getConfigurationValidationResponse().setModified(
                status.getConfigurationValidationResponse().isModified() );

            for ( ValidationMessage msg : status.getConfigurationValidationResponse().getValidationErrors() )
            {
                resource.getConfigurationValidationResponse().addValidationError( msg.toString() );
            }
            for ( ValidationMessage msg : status.getConfigurationValidationResponse().getValidationWarnings() )
            {
                resource.getConfigurationValidationResponse().addValidationWarning( msg.toString() );
            }
        }

        resource.setClientPermissions( getClientPermissionsForCurrentUser( request ) );

        StatusResourceResponse result = new StatusResourceResponse();

        result.setData( resource );

        return result;
    }

    private String spit( Throwable t )
    {
        if ( t == null )
        {
            return null;
        }
        else
        {
            StringWriter sw = new StringWriter();

            t.printStackTrace( new PrintWriter( sw ) );

            return sw.toString();
        }

    }
}
