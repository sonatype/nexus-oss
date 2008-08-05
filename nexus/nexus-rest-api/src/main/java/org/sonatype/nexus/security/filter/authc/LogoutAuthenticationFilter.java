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
