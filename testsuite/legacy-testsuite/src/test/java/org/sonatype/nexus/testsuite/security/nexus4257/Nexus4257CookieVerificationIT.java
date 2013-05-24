/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.security.nexus4257;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.security.StatelessAndStatefulWebSessionManager;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;


public class Nexus4257CookieVerificationIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public static void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testCookieForStateFullClient()
        throws Exception
    {
        setAnonymousAccess( false );

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, password ) );

        // stateful clients must login first, since other rest urls create no sessions
        String loginUrl = this.getBaseNexusUrl() + "service/local/authentication/login";
        httpClient.getParams().setAuthenticationPreemptive( true ); // go straight to basic auth
        assertThat( executeAndRelease( httpClient, new GetMethod( loginUrl ) ), equalTo( 200 ) );

        GetMethod getMethod = new GetMethod( url );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );
        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        assertThat( "Session Cookie not set", sessionCookie, notNullValue() );
        httpClient.getState().clear(); // remove cookies, credentials, etc

        // do not set the cookie, expect failure
        GetMethod failedGetMethod = new GetMethod( url );
        assertThat( executeAndRelease( httpClient, failedGetMethod ), equalTo( 401 ) );

        // set the cookie expect a 200, If a cookie is set, and cannot be found on the server, the response will fail with a 401
        httpClient.getState().addCookie( sessionCookie );
        getMethod = new GetMethod( url );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );
    }

    /**
     * Tests that session cookies are not set for the list of known stateless clients.
     * @throws Exception
     */
    @Test
    public void testCookieForStateLessClient()
        throws Exception
    {
        setAnonymousAccess( false );

        String[] statelessUserAgents = {
            "Java",
            "Apache-Maven",
            "Apache Ivy",
            "curl",
            "Wget",
            "Nexus",
            "Artifactory",
            "Apache Archiva",
            "M2Eclipse",
            "Aether"
        };

        for( String userAgent : statelessUserAgents )
        {
            testCookieNotSetForKnownStateLessClients( userAgent );
        }
    }

    /**
     * Makes a request after setting the user agent and verifies that the session cookie is NOT set.
     * @param userAgent
     * @throws Exception
     */
    private void testCookieNotSetForKnownStateLessClients( String userAgent ) throws Exception
    {
        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        Header header = new Header("User-Agent", userAgent + "/1.6" ); // user agent plus some version

        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, password ) );

        GetMethod getMethod = new GetMethod( url );
        getMethod.addRequestHeader( header );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        assertThat( "Session Cookie should not be set for user agent: " + userAgent, sessionCookie, nullValue() );
    }


    /**
     * Tests that an anonymous user with a stateless client does NOT receive a session cookie.
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void testCookieForStateFullClientForAnonUser()
        throws Exception
    {
        setAnonymousAccess( true );

        String url = this.getBaseNexusUrl() + "content/";

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); // anonymous access

        GetMethod getMethod = new GetMethod( url );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        assertThat( "Session Cookie should not be set", sessionCookie, nullValue() );
    }

    /**
     * Tests that an anonymous user with a stateless client does NOT receive a session cookie.
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void testCookieForStateLessClientForAnonUser()
        throws Exception
    {
        setAnonymousAccess( true );

        String url = this.getBaseNexusUrl() + "content/";

        Header header = new Header("User-Agent", "Java/1.6" );

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); // anonymous access

        GetMethod getMethod = new GetMethod( url );
        getMethod.addRequestHeader( header );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        assertThat( "Session Cookie should not be set", sessionCookie, nullValue() );
    }

    /**
     * Verifies that requests with the header: X-Nexus-Session do not have session cookies set.
     * @throws Exception
     */
    @Test
    public void testNoSessionHeader() throws Exception
    {
        setAnonymousAccess( true );

        String url = this.getBaseNexusUrl() + "content/";

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); // anonymous access

        Header header = new Header( StatelessAndStatefulWebSessionManager.NO_SESSION_HEADER, "none" );

        GetMethod getMethod = new GetMethod( url );
        assertThat( executeAndRelease( httpClient, getMethod ), equalTo( 200 ) );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        assertThat( "Session Cookie should not be set", sessionCookie, nullValue() );
    }
    
    private void setAnonymousAccess( boolean enabled ) throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( enabled );
        SettingsMessageUtil.save( settings );
    }

    private Cookie getSessionCookie( Cookie[] cookies )
    {
        for ( Cookie cookie : cookies )
        {
            if ( "JSESSIONID".equals( cookie.getName() ) )
            {
                return cookie;
            }
        }

        return null;

    }

    private int executeAndRelease( HttpClient httpClient, HttpMethodBase method )
        throws IOException
    {
        int status = 0;
        try
        {
            status = httpClient.executeMethod( method );
        }
        finally
        {
            method.releaseConnection();
        }

        return status;
    }

}
