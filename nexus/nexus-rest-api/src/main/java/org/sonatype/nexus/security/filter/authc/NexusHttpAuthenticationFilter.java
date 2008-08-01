package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.filter.authc.BasicHttpAuthenticationFilter;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    private static final String FAKE_AUTH_SCHEME = "NxBASIC";

    private static final String ANONYMOUS_LOGIN = "nexus.anonynmous";

    private final Log logger = LogFactory.getLog( this.getClass() );

    private boolean fakeAuthScheme;

    protected Log getLogger()
    {
        return logger;
    }

    // TODO: this should be boolean, but see
    // http://issues.jsecurity.org/browse/JSEC-119
    public String isFakeAuthScheme()
    {
        return Boolean.toString( fakeAuthScheme );
    }

    // TODO: this should be boolean, but see
    // http://issues.jsecurity.org/browse/JSEC-119
    public void setFakeAuthScheme( String fakeAuthSchemeStr )
    {
        this.fakeAuthScheme = Boolean.parseBoolean( fakeAuthSchemeStr );

        if ( fakeAuthScheme )
        {
            setAuthcHeaderScheme( FAKE_AUTH_SCHEME );
            setAuthzHeaderScheme( FAKE_AUTH_SCHEME );
        }
        else
        {
            setAuthcHeaderScheme( HttpServletRequest.BASIC_AUTH );
            setAuthzHeaderScheme( HttpServletRequest.BASIC_AUTH );
        }
    }

    protected Nexus getNexus( ServletRequest request )
    {
        return (Nexus) request.getAttribute( Nexus.class.getName() );
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
            if ( getNexus( request ).isAnonymousAccessEnabled() )
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

    protected boolean isLoginAttempt( String authzHeader )
    {
        // handle BASIC in the same way as our faked one
        String authzHeaderScheme = getAuthzHeaderScheme().toLowerCase();

        if ( authzHeader.toLowerCase().startsWith( HttpServletRequest.BASIC_AUTH.toLowerCase() ) )
        {
            return true;
        }
        else
        {
            return super.isLoginAttempt( authzHeaderScheme );
        }
    }

    protected boolean executeAnonymousLogin( ServletRequest request, ServletResponse response )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Attempting to authenticate Subject as Anonymous request..." );
        }

        Subject subject = getSubject( request, response );

        Nexus nexus = getNexus( request );

        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( nexus.getAnonymousUsername(), nexus
            .getAnonymousPassword() );

        try
        {
            subject.login( usernamePasswordToken );

            request.setAttribute( ANONYMOUS_LOGIN, Boolean.TRUE );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully logged in as anonymous" );
            }

            return true;
        }
        catch ( AuthenticationException ae )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Unable to log in subject as anonymous", ae );
            }
        }

        // always default to false. If we've made it to this point in the code, that
        // means the authentication attempt either never occured, or wasn't successful:
        return false;
    }

    public void postHandle( ServletRequest request, ServletResponse response )
        throws Exception
    {
        Subject subject = getSubject( request, response );

        if ( request.getAttribute( ANONYMOUS_LOGIN ) != null
            || request.getAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) != null )
        {
            subject.logout();
        }

        // we should check is the user anonymous or not?
        if ( request.getAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Request processing is rejected coz lacking of perms/roles, rechallenging..." );
            }

            sendChallenge( request, response );
        }
    }
}
