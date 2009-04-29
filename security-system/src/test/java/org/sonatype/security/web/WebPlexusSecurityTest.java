/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.web;

import java.util.List;

import junit.framework.Assert;

import org.jsecurity.realm.CachingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.realm.SimpleAccountRealm;
import org.sonatype.security.AbstractSecurityTest;
import org.sonatype.security.SecuritySystem;

public class WebPlexusSecurityTest
    extends AbstractSecurityTest
{

    public void testCacheManagerInit()
        throws Exception
    {
        // this is a singleton
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        List<Realm> realms = securitySystem.getRealms();
        realms.clear();
        realms.add( new SimpleAccountRealm() );
        securitySystem.setRealms( realms );

        // now if we grab one of the realms from the Realm locator, it should have its cache set
        CachingRealm cRealm1 = (CachingRealm) securitySystem.getRealms().get( 0 );
        Assert.assertNotNull( "Realm has null cacheManager", cRealm1.getCacheManager() );

        // // so far so good, the cacheManager should be set on all the child realms, but what if we add one after the
        // init method?
        realms.add( new SimpleAccountRealm() );
        securitySystem.setRealms( realms );
        CachingRealm cRealm2 = (CachingRealm) securitySystem.getRealms().get( 1 );
        Assert.assertNotNull( "Realm has null cacheManager", cRealm2.getCacheManager() );

        // and just for kicks try the first one again
        cRealm1 = (CachingRealm) securitySystem.getRealms().get( 0 );
        Assert.assertNotNull( "Realm has null cacheManager", cRealm1.getCacheManager() );

    }

}
