package org.sonatype.nexus.rest.users;

import org.restlet.data.Request;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;

public abstract class AbstractPlexusUserPlexusResource
    extends AbstractNexusPlexusResource
{
    protected PlexusUserResource nexusToRestModel( PlexusUser user, Request request )
    {
        PlexusUserResource resource = new PlexusUserResource();
        
        resource.setUserId( user.getUserId() );
        resource.setName( user.getName() );
        resource.setEmail( user.getEmailAddress() );
        
        for ( PlexusRole role : user.getRoles() )
        {
            PlexusRoleResource roleResource = new PlexusRoleResource();
            roleResource.setRoleId( role.getRoleId() );
            roleResource.setName( role.getName() );
            roleResource.setSource( role.getSource() );
            
            resource.addRole( roleResource );
        }
        
        return resource;
    }
}
