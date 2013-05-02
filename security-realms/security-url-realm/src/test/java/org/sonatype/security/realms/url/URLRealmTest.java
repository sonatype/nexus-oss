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
package org.sonatype.security.realms.url;

import java.util.Arrays;
import java.util.Properties;

import javax.inject.Provider;

import junit.framework.Assert;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.sonatype.jettytestsuite.ServletInfo;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.jettytestsuite.WebappContext;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.realms.url.config.UrlRealmConfiguration;
import org.sonatype.security.usermanagement.UserManager;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.sonatype.security.realms.url.config.model.Configuration;

public class URLRealmTest
    extends AbstractSecurityTestCase
{

    private String username = "test-user";

    private String password = "password123";

    private ServletServer server;

    private final static String DEFAULT_ROLE = "default-url-role";

    private static final String AUTH_APP_NAME = "auth_app";

    public void configure( final Binder binder )
    {
        super.configure( binder );
        binder.bind( HttpClient.class ).toInstance( new DefaultHttpClient() );
    }

    private URLRealm getRealm()
        throws Exception
    {
        URLRealm urlRealm = (URLRealm) this.lookup( Realm.class, "url" );
        return urlRealm;
    }

    public void testAuthenticate()
        throws Exception
    {

        URLRealm urlRealm = this.getRealm();

        AuthenticationInfo info = urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info );
    }

    public void testAuthenticateThenStopServerToTestCache()
        throws Exception
    {

        URLRealm urlRealm = this.getRealm();

        AuthenticationInfo info1 = urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info1 );

        this.server.stop();

        AuthenticationInfo info2 = urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info2 );

        // cache implementation specific
        Assert.assertEquals( info1, info2 );

        // make sure we cannot login with an invalid password
        try
        {
            urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, "INVALID-PASSWORD" ) );
            Assert.fail( "expected AuthenticationException" );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    public void testAuthorize()
        throws Exception
    {
        // make sure the other user locator is loaded
        Assert.assertNotNull( this.lookup( UserManager.class, "test" ).getUser( "bob" ) );

        URLRealm urlRealm = this.getRealm();

        // AuthorizationInfo info = urlRealm.getAuthorizationInfo( new SimplePrincipalCollection( username, urlRealm
        // .getName() ) );
        // Assert.assertNotNull( info );
        // Assert
        // .assertTrue( "User does not have expected Role: " + DEFAULT_ROLE, info.getRoles().contains( DEFAULT_ROLE ) );

        try
        {
            urlRealm.isPermitted( new SimplePrincipalCollection( "bob", urlRealm.getName() ),
                                  new WildcardPermission( "*" ) );
            Assert.fail( "Expected AuthorizationException" );
        }
        catch ( AuthorizationException e )
        {
            // expected
        }

    }

    public void testAuthFail()
        throws Exception
    {
        URLRealm urlRealm = this.getRealm();

        try
        {
            urlRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
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
        URLRealm urlRealm = this.getRealm();

        Assert.assertNotNull( urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) ) );

        try
        {
            urlRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }

        Assert.assertNotNull( urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) ) );

        try
        {
            urlRealm.getAuthenticationInfo( new UsernamePasswordToken( "random", "JUNK-PASS" ) );
            Assert.fail( "Expected: AccountException to be thrown" );
        }
        catch ( AccountException e )
        {
            // expected
        }
    }

    // @Override
    // protected void customizeContext( Context ctx )
    // {
    // super.customizeContext( ctx );
    // // ctx.put( "url-authentication-email-domain", "sonateyp.org" );
    // // ctx.put( "url-authentication-default-role", DEFAULT_ROLE );
    // // ctx.put( "authentication-url", "NOT_SET" ); // we cannot figure this out until after the container starts
    //
    // }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        server = getServletServer();
        // start the server
        server.start();

        UrlRealmConfiguration urlRealmConfiguration = this.lookup( UrlRealmConfiguration.class );
        Configuration configuration = urlRealmConfiguration.getConfiguration();
        configuration.setDefaultRole( DEFAULT_ROLE );
        configuration.setEmailDomain( "sonateyp.org" );
        configuration.setUrl( server.getUrl( AUTH_APP_NAME ) + "/" ); // add the '/' to the end
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        server.stop();
        super.tearDown();
    }

    protected ServletServer getServletServer()
        throws Exception
    {
        ServletServer server = new ServletServer();
        server.setPort( 12345 );

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

        params.put( "resourceBase", getBasedir() + "/target/test-classes/" );

        server.initialize();

        return server;
    }

}
