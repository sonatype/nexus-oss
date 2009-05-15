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
package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

/**
 * This is a simple implementation that will expose a custom user store as Users. A UserLocator exposes
 * users so they can be used for functions other then authentication and authorizing. Users email address, and
 * optionally Roles/Groups from an external source will be looked up this way. For example, user 'jcoder' from a JDBC
 * source might be associated with the group 'projectA-developer', when the user 'jcoder' is returned from this class
 * the association is contained in a User object.
 */
// This class must have a role of 'UserLocator', and the hint, must match the result of getSource() and the hint
// of the corresponding Realm.
@Component( role = UserManager.class, hint = "Simple", description = "Simple User Locator" )
public class SimpleUserManager extends AbstractReadOnlyUserManager
{

    public static final String SOURCE = "Simple";

    /**
     * This is a very simple in memory user Store.
     */
    private UserStore userStore = new UserStore();

    /*
     * (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.UserLocator#getSource()
     */
    public String getSource()
    {
        return SOURCE;
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.UserLocator#getUser(java.lang.String)
     */
    public User getUser( String userId )
    {
        SimpleUser user = this.userStore.getUser( userId );
        if ( user != null )
        {
            return this.toUser( user );
        }
        // else
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.UserLocator#isPrimary()
     */
    public boolean isPrimary()
    {
        // Set this to true if this UserLocator should priority over other UserLocators
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.UserLocator#listUserIds()
     */
    public Set<String> listUserIds()
    {
        // just return the userIds, if you can optimize for speed, do so
        Set<String> userIds = new HashSet<String>();
        for ( SimpleUser user : this.userStore.getAllUsers() )
        {
            userIds.add( user.getUserId() );
        }

        return userIds;
    }

    public Set<User> listUsers()
    {
        // return all the users in the system
        Set<User> users = new HashSet<User>();
        for ( SimpleUser user : this.userStore.getAllUsers() )
        {
            users.add( this.toUser( user ) );
        }

        return users;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    private User toUser( SimpleUser simpleUser )
    {
        // simple conversion of object
        User user = new DefaultUser();
        user.setEmailAddress( simpleUser.getEmail() );
        user.setName( simpleUser.getName() );
        user.setUserId( simpleUser.getUserId() );
        user.setStatus( UserStatus.active );
        for ( String role : simpleUser.getRoles() )
        {
            RoleIdentifier plexusRole = new RoleIdentifier( this.getSource(), role );
            user.addRole( plexusRole );
        }
        // set the source of this user to this
        user.setSource( this.getSource() );

        return user;
    }

}
