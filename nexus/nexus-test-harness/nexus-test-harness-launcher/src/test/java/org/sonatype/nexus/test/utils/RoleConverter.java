package org.sonatype.nexus.test.utils;

import java.util.List;

import org.restlet.data.Reference;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.roles.AbstractRoleResourceHandler;


public class RoleConverter
{
    

    public static RoleResource toRoleResource( CRole role )
    {
        //TODO: ultimately this method will take a parameter which is the nexus object
        //and will convert to the rest object
        RoleResource resource = new RoleResource();
        
        resource.setDescription( role.getDescription() );
        resource.setId( role.getId() );
        resource.setName( role.getName() );
        resource.setSessionTimeout( role.getSessionTimeout() );
        
        for ( String roleId : ( List<String>) role.getRoles() )
        {
            resource.addRole( roleId );
        }
        
        for ( String privId : ( List<String>) role.getPrivileges() )
        {
            resource.addPrivilege( privId );
        }
        
        return resource;
    }
    
    public static CRole toCRole( RoleResource resource )
    {
        CRole role = new CRole();
        
        role.setId( resource.getId()  );
        role.setDescription( resource.getDescription() );
        role.setName( resource.getName() );
        role.setSessionTimeout( resource.getSessionTimeout() );
        
        role.getRoles().clear();        
        for ( String roleId : ( List<String> ) resource.getRoles() )
        {
            role.addRole( roleId );
        }
        
        role.getPrivileges().clear();
        for ( String privId : ( List<String> ) resource.getPrivileges() )
        {
            role.addPrivilege( privId );
        }
        
        return role;
    }


}
