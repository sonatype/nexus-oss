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
package org.sonatype.security.web.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;

/**
 * A filter simply to log out.
 * 
 * @author cstamas
 */
public class LogoutAuthenticationFilter
    extends AuthenticationFilter
{
    /**
     * We are letting everyone in.
     */
    @Override
    protected boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
    {
        return true;
    }

    /**
     * We are letting the processing chain to continue (must implement it is abstract in superclass but we will never
     * get here).
     */
    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws Exception
    {
        return true;
    }

    /**
     * On postHandle, if we have subject, log it out.
     */
    @Override
    public void postHandle( ServletRequest request, ServletResponse response )
        throws Exception
    {
        Subject subject = getSubject( request, response );

        if ( subject != null )
        {
            subject.logout();
        }

        if ( HttpServletRequest.class.isAssignableFrom( request.getClass() ) )
        {
            HttpSession session = ( (HttpServletRequest) request ).getSession( false );

            if ( session != null )
            {
                session.invalidate();
            }
        }
    }
}
