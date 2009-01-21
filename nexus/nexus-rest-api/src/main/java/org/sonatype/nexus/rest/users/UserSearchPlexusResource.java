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
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.nexus.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.nexus.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "UserSearchPlexusResource" )
public class UserSearchPlexusResource
    extends AbstractUserSearchPlexusResource
{
    public static final String USER_ID_KEY = "userId";

    public static final String USER_SOURCE_KEY = "userSource";
    
    @Requirement
    private PlexusRoleLocator roleLocator;

    public UserSearchPlexusResource()
    {
        setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new PlexusUserSearchCriteriaResourceRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/user_search/*", "authcBasic,perms[nexus:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/user_search/{" + USER_SOURCE_KEY + "}";
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        PlexusUserSearchCriteriaResource criteriaResource = ( (PlexusUserSearchCriteriaResourceRequest) payload )
            .getData();

        PlexusUserSearchCriteria criteria = this.toPlexusSearchCriteria( criteriaResource );

        return this.search( criteria, this.getUserSource( request ) );
    }
    
    private PlexusUserSearchCriteria toPlexusSearchCriteria( PlexusUserSearchCriteriaResource criteriaResource )
    {
        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        criteria.setUserId( criteriaResource.getUserId() );
        
        // NOTE: in the future we could expand the REST resource to send back a list of roles, (or a single role)
        // to get a list of all users of Role 'XYZ'
        if( criteriaResource.isEffectiveUsers() )
        {
            criteria.setOneOfRoleIds( this.roleLocator.listRoleIds() );
        }
        
        return criteria;
    }

}
