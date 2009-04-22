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
package org.sonatype.security.locators.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jsecurity.realm.Realm;
import org.sonatype.security.locators.RealmLocator;

@Component( role = PlexusUserManager.class )
public class DefaultPlexusUserManager
    implements PlexusUserManager
{

    @Requirement
    private Logger logger;
    
    @Requirement( role = UserManager.class )
    private List<UserManager> userlocators;

    @Requirement
    private RealmLocator realmLocator;

    @Requirement( role = Realm.class )
    private Map<String, Realm> realmMap;

    public Set<PlexusUser> listUsers( String source )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        // FIXME add something that would make the primary user win over the other realms?

        for ( UserManager locator : this.getUserlocators() )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.listUsers() );
            }
        }

        return users;
    }

    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria, String source )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        for ( UserManager locator : this.getUserlocators() )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.searchUsers( criteria ) );
            }
        }

        return users;
    }

    public Set<String> listUserIds( String source )
    {
        Set<String> userIds = new TreeSet<String>();

        for ( UserManager locator : this.getUserlocators() )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                userIds.addAll( locator.listUserIds() );
            }
        }

        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        for ( UserManager locator : this.getUserlocators() )
        {
            PlexusUser user = locator.getUser( userId );

            if ( user != null )
            {
                return user;
            }
        }
        return null;
    }

    public PlexusUser getUser( String userId, String source )
    {
        PlexusUser user = null;
        if ( SOURCE_ALL.equals( source ) )
        {
            user = this.getUser( userId );
        }
        else
        {
            for ( UserManager locator : this.getUserlocators() )
            {
                if ( locator.getSource().equals( source ) )
                {
                    user = locator.getUser( userId );
                }
            }
        }

        return user;
    }

    private List<UserManager> getUserlocators()
    {

        List<UserManager> orderedLocators = new ArrayList<UserManager>();

        List<UserManager> unOrderdLocators = new ArrayList<UserManager>( this.userlocators );

        // get the sorted order of realms from the realm locator
        List<Realm> realms = this.realmLocator.getRealms();

        for ( Realm realm : realms )
        {
            // FIXME: this is dependent on the realms hint, and will NOT work with forname loaded realms

            boolean found = false;

            // find the hint
            for ( Entry<String, Realm> entry : this.realmMap.entrySet() )
            {
                if ( entry.getValue() == realm )
                {
                    found = true;

                    String source = entry.getKey();

                    UserManager userLocator = this.getLocator( source );
                    
                    if( userLocator != null )
                    {
                        //remove from unorderd and add to orderd
                        unOrderdLocators.remove( userLocator );
                        orderedLocators.add( userLocator );
                        break;
                    }
                }
            }
            
            if( !found )
            {
                this.logger.debug( "Could not find a PlexusUserLocator for realm: "+ realm.getClass() );
            }
        }

        // now add all the un-ordered ones to the ordered ones, this way they will be at the end of the ordered list
        orderedLocators.addAll( unOrderdLocators );
        
        return orderedLocators;
    }

    private UserManager getLocator( String source )
    {
        for ( UserManager locator : this.userlocators )
        {
            if( locator.getSource().equals( source ) )
            {
                return locator;
            }
        }
        return null;
    }
}
