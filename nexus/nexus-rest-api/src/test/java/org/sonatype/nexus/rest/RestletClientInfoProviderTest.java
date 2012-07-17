package org.sonatype.nexus.rest;

import junit.framework.Assert;

import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.auth.ClientInfo;

public class RestletClientInfoProviderTest
{
    private RestletClientInfoProvider testSubject;

    @Before
    public void setUp()
    {
        this.testSubject = new RestletClientInfoProvider();
    }

    protected void mockSecurity()
    {
        final Subject mock = Mockito.mock( Subject.class );
        Mockito.when( mock.getPrincipal() ).thenReturn( "superman" );
        ThreadContext.bind( mock );
    }

    protected void unmockSecurity()
    {
        ThreadContext.unbindSubject();
    }

    /**
     * Without any Shiro config around, Shiro is not viable. Anyway, from Shiro perspective (and hence, any Shiro using
     * app's perspective), this is a fatal configuration error.
     */
    @Test( expected = UnavailableSecurityManagerException.class )
    public void withoutAnything()
    {
        final ClientInfo ci = testSubject.getCurrentThreadClientInfo();
        Assert.assertNull( ci );
    }

    /**
     * Without Restlet request we expect null.
     */
    @Test
    public void withoutRestletRequest()
    {
        try
        {
            mockSecurity();
            final ClientInfo ci = testSubject.getCurrentThreadClientInfo();
            Assert.assertNull( ci );
        }
        finally
        {
            unmockSecurity();
        }
    }

    /**
     * Without Restlet request we expect null.
     */
    @Test
    public void withRestletRequest()
    {
        try
        {
            // mock security
            mockSecurity();
            // mock Restlet request
            final Request request = new Request( Method.GET, "http://repository.sonatype.org/foo/bar" );
            request.getAttributes().put( "org.restlet.http.headers", new Form() ); // this happens in Restlet HTTP
                                                                                   // connector
            request.getClientInfo().setAgent( "SuperBrowser/1.0" );
            request.getClientInfo().setAddress( "Planet Crypton's IP address in IPv8" ); // this is a free form string
                                                                                         // actually
            Response.setCurrent( new Response( request ) );

            final ClientInfo ci = testSubject.getCurrentThreadClientInfo();
            Assert.assertNotNull( ci );
            Assert.assertEquals( "superman", ci.getUserid() );
            Assert.assertEquals( "SuperBrowser/1.0", ci.getUserAgent() );
            Assert.assertEquals( "Planet Crypton's IP address in IPv8", ci.getRemoteIP() );
        }
        finally
        {
            unmockSecurity();
            Response.setCurrent( null );
        }
    }
}
