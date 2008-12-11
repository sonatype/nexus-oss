/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jsecurity.subject.Subject;
import org.jsecurity.web.filter.authc.AuthenticationFilter;

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
