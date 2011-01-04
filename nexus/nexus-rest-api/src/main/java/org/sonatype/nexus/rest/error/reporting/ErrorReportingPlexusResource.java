/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.error.reporting;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
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
import org.sonatype.nexus.rest.model.ErrorReportRequestDTO;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.ErrorReportResponseDTO;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ErrorReportingPlexusResource" )
@Path( ErrorReportingPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class ErrorReportingPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/error_reporting";

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
        return RESOURCE_URI;
    }

    /**
     * Generate a new error report, will return a url that can be used to access the error ticket.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = ErrorReportRequest.class, output = ErrorReportResponse.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        ErrorReportRequestDTO dto = ( (ErrorReportRequest) payload ).getData();

        if ( StringUtils.isBlank( dto.getTitle() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "A Title for the report is required." );
        }

        ErrorReportingManager manager =
            (ErrorReportingManager) context.getAttributes().get( ErrorReportingManager.class.getName() );

        if ( manager == null )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to retrieve error reporting manager." );
        }

        ErrorReportingSettings settings = dto.getErrorReportingSettings();

        if ( dto.isSaveErrorReportingSettings() )
        {
            if ( settings == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                             "Jira settings must be provided when set to save as default." );
            }

            manager.setJIRAUsername( settings.getJiraUsername() );
            manager.setJIRAPassword( getActualPassword( settings.getJiraPassword(), manager.getJIRAPassword() ) );
            manager.setUseGlobalProxy( true );

            try
            {
                getNexusConfiguration().saveConfiguration();
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception during update of Nexus configuration.", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
            }
        }

        org.sonatype.nexus.error.reporting.ErrorReportRequest genReq =
            new org.sonatype.nexus.error.reporting.ErrorReportRequest();
        genReq.setTitle( dto.getTitle() );
        genReq.setDescription( dto.getDescription() );
        genReq.getContext().putAll( context.getAttributes() );

        try
        {
            ErrorReportResponse dtoResponse = new ErrorReportResponse();
            dtoResponse.setData( new ErrorReportResponseDTO() );

            org.sonatype.nexus.error.reporting.ErrorReportResponse genRes;
            if ( settings != null && !dto.isSaveErrorReportingSettings()
                && !StringUtils.isEmpty( settings.getJiraUsername() ) )
            {
                genRes =
                    manager.handleError( genReq, settings.getJiraUsername(), settings.getJiraPassword(),
                                         manager.isUseGlobalProxy() );
            }
            else
            {
                genRes = manager.handleError( genReq );
            }

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
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e );
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
