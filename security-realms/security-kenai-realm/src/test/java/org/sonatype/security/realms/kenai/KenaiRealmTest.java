/**
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

import com.sonatype.security.realms.kenai.config.model.Configuration;
import junit.framework.Assert;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.jettytestsuite.ServletInfo;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.jettytestsuite.WebappContext;
import org.sonatype.plexus.appevents.EventMulticaster;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;

import java.io.File;
import java.io.FileFilter;
import java.net.ServerSocket;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class KenaiRealmTest
    extends AbstractKenaiRealmTest
{

    protected int getTotalNumberOfProjects()
    {
        return 302;
    }

    private Realm getRealm()
        throws Exception
    {
        Realm kenaiRealm = (KenaiRealm) this.lookup( Realm.class, "kenai" );
        return kenaiRealm;
    }

    public void testAuthenticate()
        throws Exception
    {
        Realm kenaiRealm = this.getRealm();

        AuthenticationInfo info = kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info );
    }

    public void testAuthorize()
        throws Exception
    {
        Realm kenaiRealm = this.getRealm();

        // check all roles
        for ( int ii = 0; ii < getTotalNumberOfProjects(); ii++ )
        {
            kenaiRealm.checkRole( new SimplePrincipalCollection( username, kenaiRealm.getName() ), "project-" + ii );
        }

    }

    public void testAuthFail()
        throws Exception
    {
        Realm kenaiRealm = this.getRealm();

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
        Realm kenaiRealm = this.getRealm();

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
