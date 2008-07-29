package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.filter.authc.BasicHttpAuthenticationFilter;
import org.sonatype.nexus.Nexus;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    public static final String IS_REJECTED = "request.is.rejected";

    private Nexus nexus;

    protected Nexus getNexus()
    {
        if ( nexus == null )
        {
            PlexusContainer plexus = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );

            try
            {
                nexus = (Nexus) plexus.lookup( Nexus.class );
            }
            catch ( ComponentLookupException e )
            {
                log.error( "Cannot lookup Nexus!", e );

                throw new IllegalStateException( "Cannot lookup Nexus!", e );
            }
        }

        return nexus;
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
            if ( getNexus().isAnonymousAccessEnabled() )
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
        if ( request.getAttribute( IS_REJECTED ) != null )
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
