/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import java.util.List;

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.RoleResource;

public abstract class AbstractRolePlexusResource
    extends AbstractSecurityPlexusResource
{

    protected static final String ROLE_SOURCE = "default";

    public RoleResource securityToRestModel( Role role, Request request, boolean appendResourceId )
    {
        // and will convert to the rest object
        RoleResource resource = new RoleResource();

        resource.setDescription( role.getDescription() );
        resource.setId( role.getRoleId() );
        resource.setName( role.getName() );

        String resourceId = "";
        if ( appendResourceId )
        {
            resourceId = resource.getId();
        }
        resource.setResourceURI( this.createChildReference( request, resourceId ).toString() );

        resource.setUserManaged( !role.isReadOnly() );

        for ( String roleId : role.getRoles() )
        {
            resource.addRole( roleId );
        }

        for ( String privId : role.getPrivileges() )
        {
            resource.addPrivilege( privId );
        }

        return resource;
    }

    public Role restToSecurityModel( Role role, RoleResource resource )
    {
        if ( role == null )
        {
            role = new Role();
        }

        role.setRoleId( resource.getId() );
        
        role.setDescription( resource.getDescription() );
        role.setName( resource.getName() );

        role.getRoles().clear();
        for ( String roleId : (List<String>) resource.getRoles() )
        {
            role.addRole( roleId );
        }

        role.getPrivileges().clear();
        for ( String privId : (List<String>) resource.getPrivileges() )
        {
            role.addPrivilege( privId );
        }

        return role;
    }
    
    public void validateRoleContainment( Role role )
        throws ResourceException
    {
        if ( role.getRoles().size() == 0 
            && role.getPrivileges().size() == 0)
        {
            throw new PlexusResourceException( 
                Status.CLIENT_ERROR_BAD_REQUEST, 
                "Configuration error.", 
                getErrorResponse( 
                    "privileges", 
                    "One or more roles/privilegs are required." ) );
        }
    }

}
