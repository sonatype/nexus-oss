/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import org.sonatype.security.model.CUser;
import org.sonatype.security.rest.model.UserResource;

public class UserConverter
{

    public static UserResource toUserResource( CUser user )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmail() );
        resource.setFirstName( user.getFirstName() );
        resource.setLastName( user.getLastName() );
        resource.setStatus( user.getStatus() );
        resource.setUserId( user.getId() );

//        for ( String roleId : (List<String>) user.getRoles() )
//        {
//            resource.addRole( roleId );
//        }

        return resource;
    }

    public static CUser toCUser( UserResource resource )
    {
        CUser user = new CUser();

        user.setEmail( resource.getEmail() );
        user.setFirstName( resource.getFirstName() );
        user.setLastName( resource.getLastName() );
        user.setStatus( resource.getStatus() );
        user.setId( resource.getUserId() );

//        user.getRoles().clear();
//        for ( String roleId : (List<String>) resource.getRoles() )
//        {
//            user.addRole( roleId );
//        }

        return user;
    }

}
