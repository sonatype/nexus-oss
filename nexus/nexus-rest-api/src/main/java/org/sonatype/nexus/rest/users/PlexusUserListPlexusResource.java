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
package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.nexus.rest.model.PlexusUserListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "PlexusUserListPlexusResource" )
public class PlexusUserListPlexusResource
    extends AbstractPlexusUserPlexusResource
{
    public static final String USER_SOURCE_KEY = "userSource";
    
    @Requirement( role = PlexusUserManager.class, hint="additinalRoles" )
    private PlexusUserManager userManager;
    
    public PlexusUserListPlexusResource()
    {
        setModifiable( false );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_users/*", "authcBasic,perms[nexus:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/plexus_users/{" + USER_SOURCE_KEY + "}";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusUserListResourceResponse result = new PlexusUserListResourceResponse();
        
        for ( PlexusUser user : userManager.listUsers( getUserSource( request ) ) )
        {
            PlexusUserResource res = nexusToRestModel( user );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }
    
    protected String getUserSource( Request request )
    {
        return request.getAttributes().get( USER_SOURCE_KEY ).toString();
    }
}
