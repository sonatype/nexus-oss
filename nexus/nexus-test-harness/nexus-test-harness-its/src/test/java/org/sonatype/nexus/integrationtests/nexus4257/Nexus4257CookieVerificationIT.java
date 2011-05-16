package org.sonatype.nexus.integrationtests.nexus4257;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
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
        Assert.assertEquals( httpClient.executeMethod( getMethod ), 200 );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNotNull( sessionCookie, "Session Cookie not set" );
        
        httpClient.getState().clear(); // remove cookies, credentials, etc
        
        // do not set the cookie, expect failure
        GetMethod failedGetMethod = new GetMethod( url );
        Assert.assertEquals( httpClient.executeMethod( failedGetMethod ), 401 );

        // set the cookie expect greatness
        httpClient.getState().addCookie( sessionCookie );
        getMethod = new GetMethod( url );
        Assert.assertEquals( httpClient.executeMethod( getMethod ), 200 );
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
        Assert.assertEquals( httpClient.executeMethod( getMethod ), 200 );

        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNull( sessionCookie, "Session Cookie is set" );
//        Assert.assertNotNull( sessionCookie, "Session Cookie not set" );
//        
//        httpClient.getState().clear(); // remove cookies, credentials, etc
//        
//        // do not set the cookie, expect failure
//        GetMethod failedGetMethod = new GetMethod( url );
//        failedGetMethod.addRequestHeader( header );
//        Assert.assertEquals( httpClient.executeMethod( failedGetMethod ), 401 );
//
//        // set the cookie expect failure, the cookie is not valid on the server
//        httpClient.getState().addCookie( sessionCookie );
//        getMethod = new GetMethod( url );
//        getMethod.addRequestHeader( header );
//        Assert.assertEquals( httpClient.executeMethod( getMethod ), 401 );
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

}
