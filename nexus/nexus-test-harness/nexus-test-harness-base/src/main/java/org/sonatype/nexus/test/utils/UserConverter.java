/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
