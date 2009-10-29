package org.sonatype.security.legacyadapter.test;

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
        AuthorizationManager roleLocator = this.lookup( AuthorizationManager.class, "legacy" );

        Set<String> roleIds = new HashSet<String>();
        for ( Role role : roleLocator.listRoles() )
        {
            roleIds.add( role.getRoleId() );
        }

        Assert.assertTrue( roleIds.contains( "role-xyz" ) );
        Assert.assertTrue( roleIds.contains( "role-abc" ) );
        Assert.assertTrue( roleIds.contains( "role-123" ) );
    }

}
