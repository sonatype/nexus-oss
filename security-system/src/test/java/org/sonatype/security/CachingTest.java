package org.sonatype.security;

import junit.framework.Assert;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.security.mock.realms.MockRealmB;
import org.sonatype.security.usermanagement.User;

public class CachingTest
    extends AbstractSecurityTest
{

    public void testCacheClearing()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.start();

        MockRealmB mockRealmB = (MockRealmB) this.lookup( Realm.class, "MockRealmB" );

        // cache should be empty to start
        Assert.assertTrue( mockRealmB.getAuthorizationCache().keys().isEmpty() );

        Assert.assertTrue( securitySystem.isPermitted(
            new SimplePrincipalCollection( "jcool", mockRealmB.getName() ),
            "test:heHasIt" ) );

        // now something will be in the cache, just make sure
        Assert.assertFalse( mockRealmB.getAuthorizationCache().keys().isEmpty() );

        // now if we update a user the cache should be cleared
        User user = securitySystem.getUser( "bburton", "MockUserManagerB" ); // different user, doesn't matter, in the
                                                                             // future we should get a little more fine
                                                                             // grained
        securitySystem.updateUser( user );

        // empty again
        Assert.assertTrue( mockRealmB.getAuthorizationCache().keys().isEmpty() );

    }
}
