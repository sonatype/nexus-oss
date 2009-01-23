package org.sonatype.jsecurity.realms.simple;

import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;

public class SimpleRoleLocatorTest
    extends PlexusTestCase
{

    public void testListRoleIds() throws Exception
    {
        PlexusRoleLocator roleLocator = this.lookup( PlexusRoleLocator.class, "Simple" );
        
        Set<String> roleIds = roleLocator.listRoleIds();
        Assert.assertTrue( roleIds.contains( "role-xyz" ) );
        Assert.assertTrue( roleIds.contains( "role-abc" ) );
        Assert.assertTrue( roleIds.contains( "role-123" ) );
    }

}
