package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;

/**
 * This is a simple implementation that will expose a custom user store as PlexusUsers. A plexusUserLocator exposes
 * users so they can be used for functions other then authentication and authorizing. Users email address, and
 * optionally Roles/Groups from an external source will be looked up this way. For example, user 'jcoder' from a JDBC
 * source might be associated with the group 'projectA-developer', when the user 'jcoder' is returned from this class
 * the association is contained in a PlexusUser object.
 */
// This class must have a role of 'PlexusUserLocator', and the hint, must match the result of getSource() and the hint of the corresponding Realm.
@Component( role = PlexusUserLocator.class, hint = "Simple", description = "Simple User Locator" )
public class SimpleUserLocator
    implements PlexusUserLocator
{

    public static final String SOURCE = "Simple";

    /**
     * This is a very simple in memory user Store.
     */
    private UserStore userStore = new UserStore();

    /* (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.PlexusUserLocator#getSource()
     */
    public String getSource()
    {
        return SOURCE;
    }

    /* (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.PlexusUserLocator#getUser(java.lang.String)
     */
    public PlexusUser getUser( String userId )
    {
        SimpleUser user = this.userStore.getUser( userId );
        if ( user != null )
        {
            return this.toPlexusUser( user );
        }
        // else
        return null;
    }

    /* (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.PlexusUserLocator#isPrimary()
     */
    public boolean isPrimary()
    {
        // Set this to true if this UserLocator should priority over other PlexusUserLocators 
        return true;
    }

    /* (non-Javadoc)
     * @see org.sonatype.jsecurity.locators.users.PlexusUserLocator#listUserIds()
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

    public Set<PlexusUser> listUsers()
    {
        // return all the users in the system
        Set<PlexusUser> users = new HashSet<PlexusUser>();
        for ( SimpleUser user : this.userStore.getAllUsers() )
        {
            users.add( this.toPlexusUser( user ) );
        }
        
        return users;
    }

    public Set<PlexusUser> searchUserById( String userId )
    {
        // this is expected to be a starts with search, so 'jcod' would find 'jcoder'
        Set<PlexusUser> users = new HashSet<PlexusUser>();
        for ( SimpleUser user : this.userStore.getAllUsers() )
        {
            if( user.getUserId().toLowerCase().startsWith( userId.toLowerCase() ))
            {
                users.add( this.toPlexusUser( user ) );
            }
        }
        // return anything that matches
        return users;
    }

    private PlexusUser toPlexusUser( SimpleUser simpleUser )
    {
        // simple conversion of object
        PlexusUser user = new PlexusUser();
        user.setEmailAddress( simpleUser.getEmail() );
        user.setName( simpleUser.getName() );
        user.setUserId( simpleUser.getUserId() );
        // set the source of this user to this
        user.setSource( this.getSource() );

        return user;
    }

}
