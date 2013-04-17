/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms.kenai;

import java.util.Collections;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.sonatype.security.SecuritySystem;

public class KenaiClearCacheTest
    extends AbstractKenaiRealmTest
{

    public void testClearCache()
        throws Exception
    {
        // so here is the problem, we clear the authz cache when ever config changes happen

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.setRealms( Collections.singletonList( "kenai" ) );
        securitySystem.start();

        // now log the user in
        Subject subject1 = securitySystem.login( new UsernamePasswordToken( username, password ) );
        // check authz
        subject1.checkRole( DEFAULT_ROLE );

        // clear the cache
        KenaiRealm realm = (KenaiRealm) this.lookup( Realm.class, "kenai" );
        realm.getAuthorizationCache().clear();

        // user should still have the role
        subject1.checkRole( DEFAULT_ROLE );

        // the user should be able to login again as well
        Subject subject2 = securitySystem.login( new UsernamePasswordToken( username, password ) );
        subject2.checkRole( DEFAULT_ROLE );
    }
}
