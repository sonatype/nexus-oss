package org.sonatype.nexus.rest.roles;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RoleResource;

public abstract class AbstractRolePlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private NexusSecurity nexusSecurity;

    protected NexusSecurity getNexusSecurity()
    {
        return nexusSecurity;
    }

    public RoleResource nexusToRestModel( SecurityRole role, Request request )
    {
        // TODO: ultimately this method will take a parameter which is the nexus object
        // and will convert to the rest object
        RoleResource resource = new RoleResource();

        resource.setDescription( role.getDescription() );
        resource.setId( role.getId() );
        resource.setName( role.getName() );
        resource.setResourceURI( this.createChildReference( request, resource.getId() ).toString() );
        resource.setSessionTimeout( role.getSessionTimeout() );
        resource.setReadOnly( role.isReadOnly() );

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

}
