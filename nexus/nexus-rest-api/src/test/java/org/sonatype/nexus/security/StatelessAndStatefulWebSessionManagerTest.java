/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.ehcache.EhCache;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.subject.WebSubject;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;

/**
 * Tests for StatelessAndStatefulWebSessionManager
 */
public class StatelessAndStatefulWebSessionManagerTest
{

    private NexusWebRealmSecurityManager securityManager;
    private StatelessAndStatefulWebSessionManager sessionManager;
    private CachingSessionDAO sessionDAO;

    @Before
    public void setupSecurityObjects()
    {
        // make sure the static securityManager is NOT set
        SecurityUtils.setSecurityManager(null);

        SimpleAccountRealm simpleAccountRealm = new SimpleAccountRealm();
        simpleAccountRealm.addAccount( "user", "user123" );

        securityManager =
            new NexusWebRealmSecurityManager( LoggerFactory.getLogger( getClass() ),
                                              new HashMap<String, RolePermissionResolver>() );
        securityManager.setRealm( simpleAccountRealm );
        securityManager.init();

        sessionManager =
            (StatelessAndStatefulWebSessionManager) securityManager.getSessionManager();
        sessionDAO = (CachingSessionDAO) sessionManager.getSessionDAO();

        setupCacheManager( securityManager );

        initCache( sessionDAO );
    }

    protected void setupCacheManager( NexusWebRealmSecurityManager securityManager )
    {
        // by default use the default implementation
    }

    protected void initCache( CachingSessionDAO sessionDAO )
    {
        // init the cache safely by calling GetActiveSessions
        sessionDAO.getActiveSessions();

        // verify 0 active sessions to start
        MatcherAssert.assertThat( sessionDAO.getActiveSessions().size(), equalTo( 0 ));
    }

    /**
     * Verifies a session is NOT stored in the when a stateless client logs in.  (uses the default MapCache impl)
     */
    @Test
    public void testStatelessSession()
    {
        // mock a stateless client connection
        HttpServletRequest request = Mockito.mock( HttpServletRequest.class );
        Mockito.when( request.getHeader( "X-Nexus-Session" ) ).thenReturn( "none" );
        HttpServletResponse response = Mockito.mock( HttpServletResponse.class );

        // create a user and login
        WebSubject subject = new WebSubject.Builder( securityManager, request, response ).buildWebSubject();
        subject.login( new UsernamePasswordToken( "user", "user123" ) );

        verifyNoSessionStored();

        // verify accessing the session does not blow up
        subject.getSession().getAttributeKeys();

        // verify the session is NOT stored in a cache
        try
        {
            sessionManager.getSession(
                new DefaultSessionKey( subject.getSession().getId() ) ); // again using the sessionManager
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }

        // force clearing the ehcache
        sessionDAO.getActiveSessionsCache().clear();



        // verify accessing the session does not blow up
        subject.getSession().getAttributeKeys();

        // using the sessionManager API will fail
        try
        {
            sessionManager.getSession(
                new DefaultSessionKey( subject.getSession().getId() ) ); // again using the sessionManager
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }
    }


    /**
     * Verifies a session IS stored in the when a state-full client logs in.  (uses the default MapCache impl)
     */
    @Test
    public void testStateFullSession()
    {
        // mock a state-full client connection
        HttpServletRequest request = Mockito.mock( HttpServletRequest.class );
        HttpServletResponse response = Mockito.mock( HttpServletResponse.class );

        // create a user and login
        WebSubject subject = new WebSubject.Builder( securityManager, request, response ).buildWebSubject();
        subject.login( new UsernamePasswordToken( "user", "user123" ) );

        // verify 1 active sessions
        verifySingleSessionStored( subject.getSession().getId() );

        // verify accessing the session does not blow up
        subject.getSession().getAttributeKeys(); // directly against the subject object

        // force clearing the ehcache
        sessionDAO.getActiveSessionsCache().clear();

        // now the session should not be found
        try
        {
            subject.getSession().getAttributeKeys(); // directly against the subject object
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }

        try
        {
            sessionManager.getSession(
                new DefaultSessionKey( subject.getSession().getId() ) ); // again using the sessionManager
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }
    }

    /**
     * Verifies a session IS stored for non web clients
     */
    @Test
    public void testNonWebClient()
    {
        // create a user and login
        Subject subject = new Subject.Builder( securityManager ).buildSubject();
        subject.login( new UsernamePasswordToken( "user", "user123" ) );

        // verify 1 active sessions
        verifySingleSessionStored( subject.getSession().getId() );

        // verify accessing the session does not blow up
        subject.getSession().getAttributeKeys(); // directly against the subject object

        // force clearing the ehcache
        sessionDAO.getActiveSessionsCache().clear();

        // now the session should not be found
        try
        {
            subject.getSession().getAttributeKeys(); // directly against the subject object
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }

        try
        {
            sessionManager.getSession(
                new DefaultSessionKey( subject.getSession().getId() ) ); // again using the sessionManager
            Assert.fail( "expected UnknownSessionException" );
        }
        catch ( UnknownSessionException e )
        {
            // expected
        }
    }

    protected void verifyNoSessionStored()
    {
        // verify 0 active sessions
        MatcherAssert.assertThat( sessionDAO.getActiveSessions().size(), equalTo( 0 ));
    }

    protected void verifySingleSessionStored( Serializable sessionId )
    {
        // verify 1 active sessions
        MatcherAssert.assertThat( sessionDAO.getActiveSessions().size(), equalTo( 1 ));

        sessionManager.getSession( new DefaultSessionKey( sessionId ) ); // again using the sessionManager
    }

    @After
    public void clearSecurityThreadLocals()
    {
        ThreadContext.remove();
    }

}
