/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.ExpiredCredentialsException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.WebUtils;
import org.jsecurity.web.filter.authc.BasicHttpAuthenticationFilter;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.auth.AuthenticationItem;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.rest.RemoteIPFinder;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    public static final String AUTH_SCHEME_KEY = "auth.scheme";

    public static final String AUTH_REALM_KEY = "auth.realm";

    public static final String FAKE_AUTH_SCHEME = "NxBASIC";

    private static final String ANONYMOUS_LOGIN = "nexus.anonynmous";

    private final Log logger = LogFactory.getLog( this.getClass() );

    private boolean fakeAuthScheme;

    private AuthcAuthzEvent currentAuthcEvt;

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

    protected Nexus getNexus()
    {
        return (Nexus) getAttribute( Nexus.class.getName() );
    }

    protected NexusConfiguration getNexusConfiguration()
    {
        return (NexusConfiguration) getAttribute( NexusConfiguration.class.getName() );
    }

    protected PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );
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
            if ( getNexusConfiguration().isAnonymousAccessEnabled() )
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

        UsernamePasswordToken usernamePasswordToken =
            new UsernamePasswordToken( getNexusConfiguration().getAnonymousUsername(),
                                       getNexusConfiguration().getAnonymousPassword() );

        try
        {
            request.setAttribute( ANONYMOUS_LOGIN, Boolean.TRUE );

            subject.login( usernamePasswordToken );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Successfully logged in as anonymous" );
            }
            
            postAuthcEvent( request, getNexusConfiguration().getAnonymousUsername(), true );

            return true;
        }
        catch ( AuthenticationException ae )
        {
            getLogger().info(
                              "Unable to authenticate user [anonymous] from address/host [" + request.getRemoteAddr()
                                  + "/" + request.getRemoteHost() + "]" );

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
    protected boolean onLoginSuccess( AuthenticationToken token, Subject subject, ServletRequest request,
                                      ServletResponse response )
    {
        String msg =
            "Successfully authenticated user [" + token.getPrincipal() + "] from address/host ["
                + request.getRemoteAddr() + "/" + request.getRemoteHost() + "]";

        recordAuthcEvent( request, msg );
        
        postAuthcEvent( request, token.getPrincipal().toString(), true );

        return true;
    }
    
    private void postAuthcEvent( ServletRequest request, String username, boolean success )
    {
        try
        {
            ApplicationEventMulticaster multicaster = getPlexusContainer().lookup( ApplicationEventMulticaster.class );
            
            multicaster.notifyEventListeners( 
                new NexusAuthenticationEvent(
                    this,
                    new AuthenticationItem( 
                        username, 
                        RemoteIPFinder.findIP( ( HttpServletRequest ) request ),
                        success ) ) );
        }
        catch ( ComponentLookupException e )
        {
            getLogger().error( "Unable to lookup component", e );
        }
    }

    private void recordAuthcEvent( ServletRequest request, String msg )
    {
        // to make feeds entries be more concise, ignore similar events which occurs in a small period of time
        if ( isSimilarEvent( msg ) )
        {
            return;
        }

        getLogger().info( msg );

        getLogger().info( msg );

        AuthcAuthzEvent evt = new AuthcAuthzEvent( FeedRecorder.SYSTEM_AUTHC, msg );
        
        if ( HttpServletRequest.class.isAssignableFrom( request.getClass() ) )
        {
            String ip = RemoteIPFinder.findIP( ( HttpServletRequest ) request );
            
            if ( ip != null )
            {
                evt.getEventContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, ip );
            }
        }

        getNexus().addAuthcAuthzEvent( evt );

        currentAuthcEvt = evt;
    }

    private boolean isSimilarEvent( String msg )
    {
        if ( currentAuthcEvt == null )
        {
            return false;
        }

        if ( currentAuthcEvt.getMessage().equals( msg )
            && ( System.currentTimeMillis() - currentAuthcEvt.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }

    @Override
    protected boolean onLoginFailure( AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                      ServletResponse response )
    {
        String msg =
            "Unable to authenticate user [" + token.getPrincipal() + "] from address/host [" + request.getRemoteAddr()
                + "/" + request.getRemoteHost() + "]";

        recordAuthcEvent( request, msg );
        
        postAuthcEvent( request, token.getPrincipal().toString(), false );

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
        if ( request.getAttribute( ANONYMOUS_LOGIN ) != null )
        {
            getSubject( request, response ).logout();

            if ( HttpServletRequest.class.isAssignableFrom( request.getClass() ) )
            {
                HttpSession session = ( (HttpServletRequest) request ).getSession( false );

                if ( session != null )
                {
                    session.invalidate();
                }
            }
        }

        if ( request.getAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Request processing is rejected coz lacking of perms/roles." );
            }

            if ( request.getAttribute( ANONYMOUS_LOGIN ) != null )
            {
                sendChallenge( request, response );
            }
            else
            {
                sendForbidden( request, response );
            }
        }
    }

    /**
     * set http 403 forbidden header for the response
     * 
     * @param request
     * @param response
     */
    protected void sendForbidden( ServletRequest request, ServletResponse response )
    {
        HttpServletResponse httpResponse = WebUtils.toHttp( response );

        httpResponse.setStatus( HttpServletResponse.SC_FORBIDDEN );
    }
}
