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
package org.sonatype.security.realms.kenai;

import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.sonatype.jettytestsuite.ServletInfo;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.jettytestsuite.WebappContext;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;

import com.sonatype.security.realms.kenai.config.model.Configuration;

public class KenaiRealmTest
    extends AbstractSecurityTestCase
{

    private String username = "test-user";

    private String password = "password123";

    private ServletServer server;

    private final static String DEFAULT_ROLE = "default-url-role";

    private static final String AUTH_APP_NAME = "auth_app";

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

        // this will fail unless the user auth is cached from a login
        try
        {
            kenaiRealm.checkRole( new SimplePrincipalCollection( username, kenaiRealm.getName() ), "lg3d-incubator" );
            Assert.fail( "Expected AuthorizationException" );
        }
        catch ( AuthorizationException e )
        {
            // expected
        }

        AuthenticationInfo info = kenaiRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info );
        kenaiRealm.checkRole( new SimplePrincipalCollection( username, kenaiRealm.getName() ), "lg3d-incubator" );

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

    protected ServletServer getServletServer()
        throws Exception
    {
        ServletServer server = new ServletServer();

        ServerSocket socket = new ServerSocket( 0 );
        int freePort = socket.getLocalPort();
        socket.close();

        server.setPort( freePort );

        WebappContext webapp = new WebappContext();
        server.setWebappContexts( Arrays.asList( webapp ) );

        webapp.setName( "auth_app" );
        org.sonatype.jettytestsuite.AuthenticationInfo authInfo = new org.sonatype.jettytestsuite.AuthenticationInfo();
        webapp.setAuthenticationInfo( authInfo );

        authInfo.setAuthMethod( "BASIC" );
        authInfo.setCredentialsFilePath( getBasedir() + "/target/test-classes/credentials.properties" );

        ServletInfo servletInfo = new ServletInfo();
        webapp.setServletInfos( Arrays.asList( servletInfo ) );

        servletInfo.setMapping( "/*" );
        servletInfo.setServletClass( DefaultServlet.class.getName() );

        Properties params = new Properties();
        servletInfo.setParameters( params );

        params.put( "resourceBase", getBasedir() + "/target/test-classes/data/" );

        server.initialize();

        return server;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        server = this.getServletServer();
        // start the server
        server.start();

        KenaiRealmConfiguration kenaiRealmConfiguration = this.lookup( KenaiRealmConfiguration.class );
        Configuration configuration = kenaiRealmConfiguration.getConfiguration();
        configuration.setDefaultRole( DEFAULT_ROLE );
        configuration.setEmailDomain( "sonateyp.org" );
        configuration.setBaseUrl( server.getUrl( AUTH_APP_NAME ) + "/" ); // add the '/' to the end
        // kenaiRealmConfiguration.updateConfiguration( configuration );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        server.stop();
        super.tearDown();
    }

}
