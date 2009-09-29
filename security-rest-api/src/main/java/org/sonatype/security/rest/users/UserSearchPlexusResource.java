/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.users;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.security.usermanagement.UserSearchCriteria;

@Component( role = PlexusResource.class, hint = "UserSearchPlexusResource" )
public class UserSearchPlexusResource
    extends AbstractUserSearchPlexusResource
{
    public static final String USER_ID_KEY = "userId";

    public static final String USER_SOURCE_KEY = "userSource";

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
        return new PathProtectionDescriptor( "/user_search/**", "authcBasic,perms[security:users]" );
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

        UserSearchCriteria criteria = this.toPlexusSearchCriteria( criteriaResource );
        criteria.setSource( this.getUserSource( request ) );

        return this.search( criteria );
    }

    private UserSearchCriteria toPlexusSearchCriteria( PlexusUserSearchCriteriaResource criteriaResource )
    {
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setUserId( criteriaResource.getUserId() );

        // NOTE: in the future we could expand the REST resource to send back a list of roles, (or a single role)
        // to get a list of all users of Role 'XYZ'
        if ( criteriaResource.isEffectiveUsers() )
        {
            Set<String> roleIds = new HashSet<String>();

            Set<Role> roles = null;
            try
            {
                roles = this.getSecuritySystem().listRoles("default");
            }
            catch ( NoSuchAuthorizationManager e )
            {
                this.getLogger().error( "Cannot find default UserManager,  effective user search may not work properly.", e );
                roles = this.getSecuritySystem().listRoles();
            }
            
            for ( Role role : roles )
            {
                roleIds.add( role.getRoleId() );
            }

            criteria.setOneOfRoleIds( roleIds );
        }

        return criteria;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        UserSearchCriteria criteria = new UserSearchCriteria();

        // match all userIds
        criteria.setUserId( "" );
        criteria.setSource( this.getUserSource( request ) );

        return this.search( criteria );
    }

}
