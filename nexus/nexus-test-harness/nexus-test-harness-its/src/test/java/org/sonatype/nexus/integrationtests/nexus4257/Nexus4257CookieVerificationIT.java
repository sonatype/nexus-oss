/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus4257;

import java.io.IOException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class Nexus4257CookieVerificationIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        // disable anonymous access
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        SettingsMessageUtil.save( settings );

    }

    @Test
    public void testCookieForStateFullClient()
        throws HttpException, IOException
    {

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); 
        httpClient.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, password ) );

        GetMethod getMethod = new GetMethod( url );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );
        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNotNull( sessionCookie, "Session Cookie not set" );
        httpClient.getState().clear(); // remove cookies, credentials, etc

        // do not set the cookie, expect failure
        GetMethod failedGetMethod = new GetMethod( url );
        Assert.assertEquals( executeAndRelease( httpClient, failedGetMethod ), 401 );

        // set the cookie expect a 200, If a cookie is set, and cannot be found on the server, the response will fail with a 401
        httpClient.getState().addCookie( sessionCookie );
        getMethod = new GetMethod( url );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );
    }
    
    @Test
    public void testCookieForStateLessClient()
        throws HttpException, IOException
    {

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        Header header = new Header("User-Agent", "Java/1.6" );
        
        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); 
        httpClient.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, password ) );

        GetMethod getMethod = new GetMethod( url );
        getMethod.addRequestHeader( header );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNull( sessionCookie, "Session Cookie is set" );
    }


    @Test
    public void testCookieForStateFullClientForAnonUser()
        throws HttpException, IOException
    {

        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( true );
        SettingsMessageUtil.save( settings );

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); // anonymous access

        GetMethod getMethod = new GetMethod( url );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNotNull( sessionCookie, "Session Cookie not set" );
        
        httpClient.getState().clear(); // remove cookies, credentials, etc
        // set the cookie expect a 200, If a cookie is set, and cannot be found on the server, the response will fail with a 401
        httpClient.getState().addCookie( sessionCookie );
        getMethod = new GetMethod( url );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );
    }


    @Test
    public void testCookieForStateLessClientForAnonUser()
        throws HttpException, IOException
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( true );
        SettingsMessageUtil.save( settings );

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + "content/";

        Header header = new Header("User-Agent", "Java/1.6" );

        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); // anonymous access

        GetMethod getMethod = new GetMethod( url );
        getMethod.addRequestHeader( header );
        Assert.assertEquals( executeAndRelease( httpClient, getMethod ), 200 );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNull( sessionCookie, "Session Cookie is set" );
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
