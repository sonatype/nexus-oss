package org.sonatype.nexus.rest.users;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;

public class MockPlexusUserLocator implements PlexusUserLocator
{
    public String getSource()
    {
        return "MockPlexusUserLocator";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        PlexusUser a = new PlexusUser();
        a.setName( "Joe Coder" );
        a.setEmailAddress( "jcoder@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "jcoder" );
        a.addRole( this.createFakeRole( "Role1" ) );
        a.addRole( this.createFakeRole( "Role2" ) );
        a.addRole( this.createFakeRole( "Role3" ) );

        PlexusUser b = new PlexusUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.addRole( this.createFakeRole( "Role2" ) );
        b.addRole( this.createFakeRole( "Role3" ) );

        PlexusUser c = new PlexusUser();
        c.setName( "Patricia P. Peralez" );
        c.setEmailAddress( "pperalez@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "pperalez" );
        c.addRole( this.createFakeRole( "Role1" ) );
        c.addRole( this.createFakeRole( "Role2" ) );

        PlexusUser d = new PlexusUser();
        d.setName( "Danille S. Knudsen" );
        d.setEmailAddress( "dknudsen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "dknudsen" );
        d.addRole( this.createFakeRole( "Role4" ) );
        d.addRole( this.createFakeRole( "Role2" ) );

        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );

        return users;
    }

    public boolean isPrimary()
    {
        return true;
    }

    public PlexusUser getUser( String userId )
    {
        Set<PlexusUser> users = this.listUsers();

        for ( PlexusUser plexusUser : users )
        {
            if ( plexusUser.getUserId().equals( userId ) )
            {
                return plexusUser;
            }
        }

        return null;
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

    public Set<PlexusUser> searchUserById( String userId )
    {
        // TODO: not sure what this should actually be... regex?

        Set<PlexusUser> result = new HashSet<PlexusUser>();
        for ( PlexusUser plexusUser : this.listUsers() )
        {
            if ( plexusUser.getUserId().toLowerCase().startsWith( userId.toLowerCase() ) )
            {
                result.add( plexusUser );
            }
        }
        return result;
    }

    protected PlexusRole createFakeRole( String roleId )
    {
        PlexusRole role = new PlexusRole();
        role.setName( roleId );
        role.setRoleId( roleId );
        role.setSource( this.getSource() );

        return role;
    }

}
