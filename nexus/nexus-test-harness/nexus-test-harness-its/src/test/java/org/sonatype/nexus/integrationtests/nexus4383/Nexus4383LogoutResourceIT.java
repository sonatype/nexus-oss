package org.sonatype.nexus.integrationtests.nexus4383;

import java.io.IOException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests to make sure the session is removed when the logout resource is called.
 *
 */
public class Nexus4383LogoutResourceIT
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
    
    
    /**
     * 1.) Make a get request to set a cookie </BR>
     * 2.) verify cookie works (do not send basic auth) </BR>
     * 3.) do logout  </BR>
     * 4.) repeat step 2 and expect failure.
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void testLogout()
        throws HttpException, IOException
    {

        TestContext context = TestContainer.getInstance().getTestContext();
        String username = context.getAdminUsername();
        String password = context.getPassword();
        String url = this.getBaseNexusUrl() + RequestFacade.SERVICE_LOCAL + "status";
        String logoutUrl = this.getBaseNexusUrl() + RequestFacade.SERVICE_LOCAL + "authentication/logout";

        Header userAgentHeader = new Header("User-Agent", "Something Stateful" );
        
        // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
        HttpClient httpClient = new HttpClient(); 
        httpClient.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, password ) );
        httpClient.getParams().setAuthenticationPreemptive(true);

        GetMethod getMethod = new GetMethod( url );
        getMethod.addRequestHeader( userAgentHeader );
        try
        {
            Assert.assertEquals( httpClient.executeMethod( getMethod ), 200 );
        }
        finally
        {
            getMethod.releaseConnection();
        }
        
        Cookie sessionCookie = this.getSessionCookie( httpClient.getState().getCookies() );
        Assert.assertNotNull( sessionCookie, "Session Cookie not set" );
        
        httpClient.getState().clear(); // remove cookies, credentials, etc
        
        // now with just the cookie
        httpClient.getState().addCookie( sessionCookie );
        getMethod = new GetMethod( url );
        try
        {
            Assert.assertEquals( httpClient.executeMethod( getMethod ), 200 );
        }
        finally
        {
            getMethod.releaseConnection();
        }
        
        // do logout
        GetMethod logoutGetMethod = new GetMethod( logoutUrl );
        try
        {
            Assert.assertEquals( httpClient.executeMethod( logoutGetMethod ), 200 );
            Assert.assertEquals( logoutGetMethod.getResponseBodyAsString(), "OK" );
        }
        finally
        {
            logoutGetMethod.releaseConnection();
        }
        
        // set cookie again
        httpClient.getState().clear(); // remove cookies, credentials, etc
        httpClient.getState().addCookie( sessionCookie );
        GetMethod failedGetMethod = new GetMethod( url );
        try
        {
            Assert.assertEquals( httpClient.executeMethod( failedGetMethod ), 401 );
        }
        finally
        {
            failedGetMethod.releaseConnection();
        }        
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
