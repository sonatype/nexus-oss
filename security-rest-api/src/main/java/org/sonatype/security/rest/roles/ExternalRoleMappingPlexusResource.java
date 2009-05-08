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
package org.sonatype.security.rest.roles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager;

@Component( role = PlexusResource.class, hint = "ExternalRoleMappingPlexusResource" )
public class ExternalRoleMappingPlexusResource
    extends AbstractRolePlexusResource
{
    public static final String SOURCE_ID_KEY = "sourceId";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/external_role_map/*", "authcBasic,perms[security:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/external_role_map/{" + SOURCE_ID_KEY + "}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String source = this.getSourceId( request );

        try
        {
            // get roles for the source
            Set<Role> roles = this.getSecuritySystem().listRoles( source );

            if ( roles == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Role Source '" + source
                    + "' could not be found." );
            }

            Set<Role> defaultRoles = this.getSecuritySystem().listRoles( SecurityXmlUserManager.SOURCE );

            Map<Role, Set<Role>> roleMap = new HashMap<Role, Set<Role>>();

            for ( Role defaultRole : defaultRoles )
            {
                for ( Role role : roles )
                {
                    // if the roleId matches (and the source doesn't)
                    if ( !StringUtils.equals( defaultRole.getSource(), role.getSource() )
                        && StringUtils.equals( defaultRole.getRoleId(), role.getRoleId() ) )
                    {
                        Set<Role> mappedRoles = roleMap.get( defaultRole );
                        // if we don't have any currently mapped roles, add it to the map,
                        // if we do then just add to the set

                        if ( mappedRoles == null )
                        {
                            mappedRoles = new HashSet<Role>();
                            mappedRoles.add( role );
                            roleMap.put( defaultRole, mappedRoles );
                        }
                        else
                        {
                            // just add this new role to the current set
                            mappedRoles.add( role );
                        }

                        roleMap.put( defaultRole, mappedRoles );
                    }
                }
            }

            // now put this in a resource
            ExternalRoleMappingResourceResponse result = new ExternalRoleMappingResourceResponse();

            for ( Role defaultRole : roleMap.keySet() )
            {
                ExternalRoleMappingResource resource = new ExternalRoleMappingResource();
                result.addData( resource );
                resource.setDefaultRole( this.securityToRestModel( defaultRole ) );

                for ( Role mappedRole : roleMap.get( defaultRole ) )
                {
                    resource.addMappedRole( this.securityToRestModel( mappedRole ) );
                }
            }

            return result;

        }
        catch ( NoSuchAuthorizationManager e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Role Source '" + source
                + "' could not be found." );
        }

    }

    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }
}
