/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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

        Assert.assertTrue( securitySystem.isPermitted( new SimplePrincipalCollection( "jcool", mockRealmB.getName() ),
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
