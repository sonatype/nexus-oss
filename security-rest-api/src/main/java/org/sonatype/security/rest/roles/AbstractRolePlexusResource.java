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
package org.sonatype.security.rest.roles;

import java.util.List;

import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.RoleResource;

public abstract class AbstractRolePlexusResource
    extends AbstractSecurityPlexusResource
{


    public RoleResource nexusToRestModel( SecurityRole role, Request request )
    {
        // TODO: ultimately this method will take a parameter which is the nexus object
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

    public SecurityRole restToNexusModel( SecurityRole role, RoleResource resource )
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
