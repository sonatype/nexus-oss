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
