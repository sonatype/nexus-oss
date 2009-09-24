/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.component;

import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
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
{
    public static final String ROLE_ID = "role";

    @Requirement
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

        try
        {
            Map<String, Object> components = container.lookupMap( role );

            if ( components == null || components.isEmpty() )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }

            for ( String hint : components.keySet() )
            {
                ComponentDescriptor componentDescriptor = container.getComponentDescriptor( role, hint );

                PlexusComponentListResource resource = new PlexusComponentListResource();

                resource.setRoleHint( componentDescriptor.getRoleHint() );
                resource.setDescription( ( StringUtils.isNotEmpty( componentDescriptor.getDescription() ) )
                    ? componentDescriptor.getDescription()
                    : componentDescriptor.getRoleHint() );

                // add it to the collection
                result.addData( resource );
            }

        }
        catch ( ComponentLookupException e )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Unable to look up plexus component with role '" + "1" + "'.", e );
            }
            
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        return result;
    }
}
