package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.Role;

public class SimpleRoleLocatorTest
    extends PlexusTestCase
{

    public void testListRoleIds()
        throws Exception
    {
        AuthorizationManager roleLocator = this.lookup( AuthorizationManager.class, "Simple" );

        Set<String> roleIds = this.toIdSet( roleLocator.listRoles() );
        Assert.assertTrue( roleIds.contains( "role-xyz" ) );
        Assert.assertTrue( roleIds.contains( "role-abc" ) );
        Assert.assertTrue( roleIds.contains( "role-123" ) );
    }

    private Set<String> toIdSet( Set<Role> roles )
    {
        Set<String> ids = new HashSet<String>();

        for ( Role role : roles )
        {
            ids.add( role.getRoleId() );
        }

        return ids;
    }

}
