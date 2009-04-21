package org.sonatype.jsecurity.realms.url;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.AbstractPlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;

@Component( role = PlexusUserLocator.class, hint = "test" )
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
