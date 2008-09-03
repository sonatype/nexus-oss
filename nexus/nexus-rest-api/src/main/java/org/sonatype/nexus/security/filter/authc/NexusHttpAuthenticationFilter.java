package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.ExpiredCredentialsException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.WebUtils;
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
            setAuthcScheme( FAKE_AUTH_SCHEME );
            setAuthzScheme( FAKE_AUTH_SCHEME );
        }
        else
        {
            setAuthcScheme( HttpServletRequest.BASIC_AUTH );
            setAuthzScheme( HttpServletRequest.BASIC_AUTH );
        }
    }

    protected Nexus getNexus( ServletRequest request )
    {
        return (Nexus) request.getAttribute( Nexus.class.getName() );
    }

    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
    {
        // this will be true if cookie is sent with request and it is valid
        Subject subject = getSubject( request, response );

        // NEXUS-607: fix for cookies, when sent from client. They will expire once
        // and we are not sending them anymore.
        boolean loggedIn = subject.isAuthenticated();

        if ( loggedIn )
        {
            return true;
        }

        if ( isLoginAttempt( request, response ) )
        {
            loggedIn = executeLogin( request, response );
        }
        else
        {
            // let the user "fall thru" until we get some permission problem
            if ( getNexus( request ).isAnonymousAccessEnabled() )
            {
                loggedIn = executeAnonymousLogin( request, response );
            }
        }

        if ( !loggedIn )
        {
            sendChallenge( request, response );
        }

        return loggedIn;
    }

    protected boolean isLoginAttempt( String authzHeader )
    {
        // handle BASIC in the same way as our faked one
        String authzHeaderScheme = getAuthzScheme().toLowerCase();

        if ( authzHeader.toLowerCase().startsWith( HttpServletRequest.BASIC_AUTH.toLowerCase() ) )
        {
            return true;
        }
        else
        {
            return super.isLoginAttempt( authzHeaderScheme );
        }
    }

    protected boolean isRememberMeEnabled( ServletRequest request )
    {
        if ( request.getAttribute( ANONYMOUS_LOGIN ) == null )
        {
            // it is not an anonymous login
            // return true;
            // NEXUS-607: fix for cookies, when sent from client. They will expire once
            // and we are not sending them anymore.
            return false;
        }
        else
        {
            // it is anon login. no rembemberMe
            return false;
        }
    }

    protected boolean executeLogin( AuthenticationToken token, ServletRequest request, ServletResponse response )
    {
        Subject subject = getSubject( request, response );

        if ( token != null && subject != null )
        {
            try
            {
                subject.login( token );

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Successfully logged in user [" + token.getPrincipal() + "]" );
                }

                return true;
            }
            catch ( AuthenticationException ae )
            {
                getLogger().info( "Unable to authenticate user [" + token.getPrincipal() + "] from address/host [" + request.getRemoteAddr() + "/" + request.getRemoteHost() + "]" );
                
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Unable to log in user [" + token.getPrincipal() + "]", ae );
                }

                onAuthenticationException( token, ae, request, response );
            }
        }

        // always default to false - authentication attempt never occurred or wasn't successful:
        return false;
    }

    protected void onAuthenticationException( AuthenticationToken token, AuthenticationException ae,
        ServletRequest request, ServletResponse response )
    {
        HttpServletResponse httpResponse = WebUtils.toHttp( response );

        if ( ExpiredCredentialsException.class.isAssignableFrom( ae.getClass() ) )
        {
            httpResponse.addHeader( "X-Nexus-Reason", "expired" );
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
            request.setAttribute( ANONYMOUS_LOGIN, Boolean.TRUE );

            subject.login( usernamePasswordToken );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully logged in as anonymous" );
            }

            return true;
        }
        catch ( AuthenticationException ae )
        {
            getLogger().info( "Unable to authenticate user [anonymous] from address/host [" + request.getRemoteAddr() + "/" + request.getRemoteHost() + "]" );
            
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

            if ( HttpServletRequest.class.isAssignableFrom( request.getClass() ) )
            {
                HttpSession session = ( (HttpServletRequest) request ).getSession( false );

                if ( session != null )
                {
                    session.invalidate();
                }
            }
        }

        // is perms elevation needed?
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
