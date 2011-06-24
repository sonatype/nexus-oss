package org.sonatype.nexus.security;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.eq;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.junit.Test;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;
import org.sonatype.nexus.security.filter.authc.NexusHttpAuthenticationFilter;

public class NexusHttpAuthenticationFilterTest
{
    /**
     * Test post handles does not throw an exception if the anonymous users session has expired.
     * @throws Exception
     */
    @Test
    public void testPostHandleForExpiredSessions()
        throws Exception
    {
        NexusHttpAuthenticationFilter filter = new NexusHttpAuthenticationFilter();

        // set the user
        DelegatingSubject subject = new DelegatingSubject( new SimplePrincipalCollection( "anonymous", "realmName" ), true, null, new SimpleSession(),  new DefaultSecurityManager() );
        ThreadContext.bind( subject );
        
        Assert.assertNotNull( SecurityUtils.getSubject() );
        subject.getSession().setTimeout( 0 ); // expire the session

        // setup the MOC
        HttpServletRequest request = createNiceMock( HttpServletRequest.class );
        expect( request.getAttribute( eq( NexusHttpAuthenticationFilter.ANONYMOUS_LOGIN ) ) )
            .andReturn( "true" ).anyTimes();
        expect( request.getAttribute( eq( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) ) )
            .andReturn( null ).anyTimes();
        EasyMock.replay( request );
        // end fun with mocks

        ServletResponse response = createNiceMock( ServletResponse.class );

        // Verify this does not throw an exception when the session is expired
        filter.postHandle( request, response );
    }
}
