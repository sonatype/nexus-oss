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

import java.util.List;

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.realms.tools.dao.SecurityRole;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.RoleResource;

public abstract class AbstractRolePlexusResource
    extends AbstractSecurityPlexusResource
{


    public RoleResource securityToRestModel( SecurityRole role, Request request )
    {
        // and will convert to the rest object
        RoleResource resource = new RoleResource();

        resource.setDescription( role.getDescription() );
        resource.setId( role.getId() );
        resource.setName( role.getName() );
        resource.setResourceURI( this.createChildReference( request, this, resource.getId() ).toString() );
        resource.setSessionTimeout( role.getSessionTimeout() );
        resource.setUserManaged( !role.isReadOnly() );

        for ( String roleId : (List<String>) role.getRoles() )
        {
            resource.addRole( roleId );
        }

        for ( String privId : (List<String>) role.getPrivileges() )
        {
            resource.addPrivilege( privId );
        }

        return resource;
    }

    public SecurityRole restToSecurityModel( SecurityRole role, RoleResource resource )
    {
        if ( role == null )
        {
            role = new SecurityRole();
        }

        role.setId( resource.getId() );
        
        role.setDescription( resource.getDescription() );
        role.setName( resource.getName() );
        role.setSessionTimeout( resource.getSessionTimeout() );

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
    
    public void validateRoleContainment( SecurityRole role )
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
