/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
    public static final String AUTH_SCHEME_KEY = "auth.scheme";

    public static final String AUTH_REALM_KEY = "auth.realm";

    public static final String FAKE_AUTH_SCHEME = "NxBASIC";

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

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws Exception
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
            try
            {
                loggedIn = executeLogin( request, response );
            }
            // if no username or password is supplied, an IllegalStateException (runtime)
            // is thrown, so if anything fails in executeLogin just assume failed login
            catch ( Exception e )
            {
                getLogger().error( "Unable to login", e );
                loggedIn = false;
            }
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
        else
        {
            request.setAttribute( AUTH_SCHEME_KEY, getAuthcScheme() );

            request.setAttribute( AUTH_REALM_KEY, getApplicationName() );
        }

        return loggedIn;
    }

    @Override
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

    @Override
    protected boolean isRememberMe( ServletRequest request )
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
            getLogger().info(
                "Unable to authenticate user [anonymous] from address/host [" + request.getRemoteAddr() + "/"
                    + request.getRemoteHost() + "]" );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Unable to log in subject as anonymous", ae );
            }
        }

        // always default to false. If we've made it to this point in the code, that
        // means the authentication attempt either never occured, or wasn't successful:
        return false;
    }

    @Override
    protected boolean onLoginFailure( AuthenticationToken token, AuthenticationException ae, ServletRequest request,
        ServletResponse response )
    {
        HttpServletResponse httpResponse = WebUtils.toHttp( response );

        if ( ExpiredCredentialsException.class.isAssignableFrom( ae.getClass() ) )
        {
            httpResponse.addHeader( "X-Nexus-Reason", "expired" );
        }

        return false;
    }

    @Override
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
