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
package org.sonatype.security.realms.url;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.usermanagement.UserManager;

public class URLRealmTest
    extends AbstractSecurityTestCase
{

    private String username = "test-user";

    private String password = "password123";

    private ServletServer server;

    private final static String DEFAULT_ROLE = "default-url-role";

    private static final String AUTH_APP_NAME = "auth_app";

    private URLRealm getRealm()
        throws Exception
    {
        URLRealm urlRealm = (URLRealm) this.lookup( Realm.class, "url" );
        urlRealm.setAuthenticationURL( server.getUrl( AUTH_APP_NAME ) + "/" ); // add the '/' to the end

        // now set the cache
        // PlexusEhCacheWrapper cacheWrapper = (PlexusEhCacheWrapper) this.lookup( PlexusEhCacheWrapper.class );
        // EhCacheManager ehCacheManager = new EhCacheManager();
        // ehCacheManager.setCacheManager( cacheWrapper.getEhCacheManager() );
        // urlRealm.setCacheManager( ehCacheManager );

        return urlRealm;
    }

    public void testAuthenticate()
        throws Exception
    {

        URLRealm urlRealm = this.getRealm();

        AuthenticationInfo info = urlRealm.getAuthenticationInfo( new UsernamePasswordToken( username, password ) );
        Assert.assertNotNull( info );
    }

    public void testAuthorize()
        throws Exception
    {
        // make sure the other user locator is loaded
        Assert.assertNotNull( this.lookup( UserManager.class, "test" ).getUser( "bob" ) );

        URLRealm urlRealm = this.getRealm();

//        AuthorizationInfo info = urlRealm.getAuthorizationInfo( new SimplePrincipalCollection( username, urlRealm
//            .getName() ) );
//        Assert.assertNotNull( info );
//        Assert
//            .assertTrue( "User does not have expected Role: " + DEFAULT_ROLE, info.getRoles().contains( DEFAULT_ROLE ) );

        try
        {
            Assert.assertNull( "Should not have returned an auth info.", urlRealm
                .getAuthorizationInfo( new SimplePrincipalCollection( "bob", urlRealm.getName() ) ) );
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

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        ctx.put( "url-authentication-email-domain", "sonateyp.org" );
        ctx.put( "url-authentication-default-role", DEFAULT_ROLE );
        ctx.put( "authentication-url", "NOT_SET" ); // we cannot figure this out until after the container starts

    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        server = this.lookup( ServletServer.class );
        // start the server
        server.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        server.stop();
        super.tearDown();
    }

}
