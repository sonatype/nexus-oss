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
package org.sonatype.jsecurity.realms.url;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.AbstractPlexusUserLocator;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;
import org.sonatype.security.locators.users.UserManager;

@Component( role = UserManager.class, hint = "test" )
public class MockUserLocator
    extends AbstractPlexusUserLocator
{
    private Set<String> userIds = new HashSet<String>();

    public MockUserLocator()
    {
        userIds.add( "bob" );
        userIds.add( "jcoder" );
    }

    public String getSource()
    {
        return "test";
    }

    public PlexusUser getUser( String userId )
    {
        if ( this.userIds.contains( userId ) )
        {
            return this.toPlexusUser( userId );
        }
        return null;
    }

    public boolean isPrimary()
    {
        return false;
    }

    public Set<String> listUserIds()
    {
        return userIds;
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        for ( String userId : this.userIds )
        {
            users.add( this.toPlexusUser( userId ) );
        }

        return users;
    }

    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    private PlexusUser toPlexusUser( String userId )
    {
        PlexusUser user = new PlexusUser();

        user.setUserId( userId );
        user.setName( userId );
        user.setEmailAddress( userId + "@foo.com" );
        user.setSource( this.getSource() );

        return user;
    }

}
