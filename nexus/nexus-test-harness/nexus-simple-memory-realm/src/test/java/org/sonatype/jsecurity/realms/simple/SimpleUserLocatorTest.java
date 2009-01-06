package org.sonatype.jsecurity.realms.simple;

import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;

public class SimpleUserLocatorTest
    extends PlexusTestCase
{

    public void testLocatorLookup()
        throws Exception
    {
        // a bit of plexus back ground, this is how you can look up a component from a test class
        this.lookup( PlexusUserLocator.class, "Simple" );
    }

    public void testSearch()
        throws Exception
    {
        PlexusUserLocator userLocator = this.lookup( PlexusUserLocator.class, "Simple" );

        Set<PlexusUser> result = userLocator.searchUserById( "adm" );
        Assert.assertEquals( 1, result.size() );
        // your test could be a bit more robust
        Assert.assertEquals( result.iterator().next().getUserId(), "admin-simple" );
    }

    public void testIdList()
        throws Exception
    {
        PlexusUserLocator userLocator = this.lookup( PlexusUserLocator.class, "Simple" );

        Set<String> ids = userLocator.listUserIds();

        Assert.assertTrue( ids.contains( "admin-simple" ) );
        Assert.assertTrue( ids.contains( "deployment-simple" ) );
        Assert.assertTrue( ids.contains( "anonymous-simple" ) );

        Assert.assertEquals( 3, ids.size() );
    }

    public void testUserList()
        throws Exception
    {
        PlexusUserLocator userLocator = this.lookup( PlexusUserLocator.class, "Simple" );

        Set<PlexusUser> users = userLocator.listUsers();
        // your test could be a bit more robust
        Assert.assertEquals( 3, users.size() );
    }

}
