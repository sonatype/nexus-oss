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
package org.sonatype.nexus.plugins.events.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "EventInspectorsPlexusResource" )
public class EventInspectorsPlexusResource
    extends AbstractPlexusResource
{
    private static final String RESOURCE_URI = "/eventInspectors/isCalmPeriod";

    @Requirement
    private EventInspectorHost eventInspectorHost;

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "anon" );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();
        boolean waitForCalm = Boolean.parseBoolean( form.getFirstValue( "waitForCalm" ) );

        if ( waitForCalm )
        {
            for ( int i = 0; i < 100; i++ )
            {
                try
                {
                    Thread.sleep( 500 );
                }
                catch ( InterruptedException e )
                {
                }
                
                if ( eventInspectorHost.isCalmPeriod() )
                {
                    response.setStatus( Status.SUCCESS_OK );
                    return "Ok";
                }
            }
            
            response.setStatus( Status.SUCCESS_ACCEPTED );
            return "Still munching on them...";
        }
        else
        {
            if ( eventInspectorHost.isCalmPeriod() )
            {
                response.setStatus( Status.SUCCESS_OK );
                return "Ok";
            }
            else
            {
                response.setStatus( Status.SUCCESS_ACCEPTED );
                return "Still munching on them...";
            }
        }
    }

}
