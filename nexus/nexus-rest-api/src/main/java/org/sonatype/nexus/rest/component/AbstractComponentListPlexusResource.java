/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.component;

import java.util.List;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;

public abstract class AbstractComponentListPlexusResource
    extends AbstractNexusPlexusResource
    implements Contextualizable
{
    public static final String ROLE_ID = "role";

    private PlexusContainer container;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    protected String getRole( Request request )
    {
        return request.getAttributes().get( ROLE_ID ).toString();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusComponentListResourceResponse result = new PlexusComponentListResourceResponse();

        // get role from request
        String role = getRole( request );

        // get component descriptors
        List<ComponentDescriptor> componentMap = this.container.getComponentDescriptorList( role );

        // check if valid role
        if ( componentMap == null || componentMap.isEmpty() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        // loop and convert all objects of this role to a PlexusComponentListResource
        for ( ComponentDescriptor componentDescriptor : componentMap )
        {
            PlexusComponentListResource resource = new PlexusComponentListResource();

            resource.setRoleHint( componentDescriptor.getRoleHint() );
            resource.setDescription( ( StringUtils.isNotEmpty( componentDescriptor.getDescription() ) )
                ? componentDescriptor.getDescription()
                : componentDescriptor.getRoleHint() );
            
            // add it to the collection
            result.addData( resource );
        }

        return result;
    }

    public void contextualize( org.codehaus.plexus.context.Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );

    }

}
