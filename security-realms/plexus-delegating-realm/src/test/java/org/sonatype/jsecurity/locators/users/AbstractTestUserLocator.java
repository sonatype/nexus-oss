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
package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTestUserLocator implements UserManager
{


    public PlexusUser getUser( String userId )
    {
        Set<PlexusUser> users = this.listUsers();
        
        for ( PlexusUser plexusUser : users )
        {
            if( plexusUser.getUserId().equals( userId ))
            {
                return plexusUser;
            }
        }
        
        return null;
    }

    public boolean isPrimary()
    {
        return false;
    }

    public Set<String> listUserIds()
    {   
        Set<String> result = new HashSet<String>();
        for ( PlexusUser plexusUser : this.listUsers() )
        {
            result.add( plexusUser.getUserId() );
        }
        return result;
    }

    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria )
    {
        Set<PlexusUser> result = new HashSet<PlexusUser>();
        for ( PlexusUser plexusUser : this.listUsers() )
        {
            if( plexusUser.getUserId().toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
            {
                result.add( plexusUser );
            }
        }
        return result;
    }
    
    protected PlexusRole createFakeRole(String roleId )
    {
        PlexusRole role = new PlexusRole();
        role.setName( roleId );
        role.setRoleId( roleId );
        role.setSource( this.getSource() );
        
        return role;
    }
    
}
