/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
