package org.sonatype.nexus.test.utils;

import java.util.List;

import org.sonatype.jsecurity.model.CUser;
import org.sonatype.nexus.rest.model.UserResource;

public class UserConverter
{

    public static UserResource toUserResource( CUser user )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmail() );
        resource.setName( user.getName() );
        resource.setStatus( user.getStatus() );
        resource.setUserId( user.getId() );

        for ( String roleId : (List<String>) user.getRoles() )
        {
            resource.addRole( roleId );
        }

        return resource;
    }

    public static CUser toCUser( UserResource resource )
    {
        CUser user = new CUser();

        user.setEmail( resource.getEmail() );
        user.setName( resource.getName() );
        user.setStatus( resource.getStatus() );
        user.setId( resource.getUserId() );

        user.getRoles().clear();
        for ( String roleId : (List<String>) resource.getRoles() )
        {
            user.addRole( roleId );
        }

        return user;
    }

}
