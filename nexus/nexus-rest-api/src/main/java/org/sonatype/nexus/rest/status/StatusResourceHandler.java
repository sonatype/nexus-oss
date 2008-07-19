/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

/**
 * The status resource handler that returns Nexus status, version and many more.
 * 
 * @author cstamas
 */
public class StatusResourceHandler
    extends AbstractNexusResourceHandler
{

    public StatusResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public boolean allowGet()
    {
        return true;
    }

    public Representation getRepresentationHandler( Variant variant )
    {
        SystemStatus status = getNexus().getSystemStatus();

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

        StatusResourceResponse response = new StatusResourceResponse();

        response.setData( resource );

        return serialize( variant, response );
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
