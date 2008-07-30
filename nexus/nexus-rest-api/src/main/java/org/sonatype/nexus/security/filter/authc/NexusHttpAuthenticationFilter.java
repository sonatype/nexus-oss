package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.filter.authc.BasicHttpAuthenticationFilter;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    protected boolean isAnonymousAccessAllowed( ServletRequest request )
    {
        Nexus nexus = (Nexus) request.getAttribute( Nexus.class.getName() );

        if ( nexus == null )
        {
            return false;
        }
        else
        {
            return nexus.isAnonymousAccessEnabled();
        }

    }

    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
    {
        boolean loggedIn = false; // false by default or we wouldn't be in this method

        if ( isLoginAttempt( request, response ) )
        {
            loggedIn = executeLogin( request, response );
        }

        if ( !loggedIn )
        {
            // let the user "fall thru" until we get some permission problem
            if ( isAnonymousAccessAllowed( request ) )
            {
                loggedIn = executeAnonymousLogin( request, response );
            }

            if ( !loggedIn )
            {
                sendChallenge( request, response );
            }
        }

        return loggedIn;
    }

    protected boolean executeAnonymousLogin( ServletRequest request, ServletResponse response )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Attempting to authenticate Subject as Anonymous request..." );
        }

        Subject subject = getSubject( request, response );

        // TODO: make this configurable
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( "anonymous", "anonymous" );

        try
        {
            subject.login( usernamePasswordToken );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Successfully logged in as anonymous" );
            }

            return true;
        }
        catch ( AuthenticationException ae )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Unable to log in subject as anonymous", ae );
            }
        }

        // always default to false. If we've made it to this point in the code, that
        // means the authentication attempt either never occured, or wasn't successful:
        return false;
    }

    public void postHandle( ServletRequest request, ServletResponse response )
        throws Exception
    {
        // we should check is the user anonymous or not?
        if ( request.getAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) != null )
        {
            Subject subject = getSubject( request, response );

            subject.logout();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Request processing is rejected coz lacking of perms/roles, rechallenging..." );
            }

            sendChallenge( request, response );
        }
    }
}
