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

import junit.framework.Assert;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;

public class KenaiRealmTest
    extends AbstractKenaiRealmTest
{
    private KenaiRealm getRealm()
        throws Exception
    {
        KenaiRealm kenaiRealm = (KenaiRealm) this.lookup( Realm.class, "kenai" );
        return kenaiRealm;
    }

    public void testAuthenticate()
        throws Exception
    {
        KenaiRealm kenaiRealm = this.getRealm();

        AuthenticationInfo info = kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info );
    }

    public void testAuthorize()
        throws Exception
    {
        KenaiRealm kenaiRealm = this.getRealm();
        kenaiRealm.checkRole( new SimplePrincipalCollection( username, kenaiRealm.getName() ), DEFAULT_ROLE );
    }

    public void testAuthFail()
        throws Exception
    {
        KenaiRealm kenaiRealm = this.getRealm();

        try
        {
            kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }
    }

    public void testAuthFailAuthFail()
        throws Exception
    {
        KenaiRealm kenaiRealm = this.getRealm();

        try
        {
            Assert.assertNotNull( kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( "unknown-user-foo-bar",
                                                                                               "invalid" ) ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }

        try
        {
            kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }

        Assert.assertNotNull( kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) ) );

        try
        {
            kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }
    }
}
