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
package org.sonatype.nexus.rest.roles;

import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleManager;
import org.sonatype.nexus.rest.model.PlexusRoleListResourceResponse;
import org.sonatype.nexus.rest.users.AbstractPlexusUserPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component(role=PlexusResource.class, hint="PlexusRoleListPlexusResource" )
public class PlexusRoleListPlexusResource
    extends AbstractPlexusUserPlexusResource
{
    @Requirement
    private PlexusRoleManager roleManager;

    public static final String SOURCE_ID_KEY = "sourceId";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_roles/*", "authcBasic,perms[nexus:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/plexus_roles/{" + SOURCE_ID_KEY + "}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String source = this.getSourceId( request );

        // get roles for the source
        Set<PlexusRole> roles = this.roleManager.listRoles( source );

        if ( roles == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Role Source '" + source
                + "' could not be found." );
        }
        
        PlexusRoleListResourceResponse resourceResponse = new PlexusRoleListResourceResponse();
        for ( PlexusRole role : roles )
        {
            resourceResponse.addData( this.nexusToRestModel( role ) );
        }

        return resourceResponse;
    }

    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }
}
