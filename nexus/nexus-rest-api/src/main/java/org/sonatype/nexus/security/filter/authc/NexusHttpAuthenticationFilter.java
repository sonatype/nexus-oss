/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.security.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.rest.RemoteIPFinder;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    public static final String AUTH_SCHEME_KEY = "auth.scheme";

    public static final String AUTH_REALM_KEY = "auth.realm";

    public static final String FAKE_AUTH_SCHEME = "NxBASIC";

    public static final String ANONYMOUS_LOGIN = "nexus.anonynmous";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private boolean fakeAuthScheme;

    // this comes from attributes set by plexus helper listener (nexus-web-utils module)
    protected PlexusContainer plexusContainer;

    // this comes from Plexus IoC but we need to "lift" them manually, no injection here
    private NexusConfiguration nexusConfiguration;

    // this comes from Plexus IoC but we need to "lift" them manually, no injection here
    private ApplicationEventMulticaster applicationEventMulticaster;

    protected void onFilterConfigSet()
        throws Exception
    {
        super.onFilterConfigSet();

        plexusContainer = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );

        // this might be null, at least the old removed code was prepared for it (why? when? -- cstamas)
        try
        {
            applicationEventMulticaster = plexusContainer.lookup( ApplicationEventMulticaster.class );
        }
        catch ( ComponentLookupException e )
        {
            applicationEventMulticaster = null;
        }

        nexusConfiguration = plexusContainer.lookup( NexusConfiguration.class );
    }

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    protected NexusConfiguration getNexusConfiguration()
    {
        return nexusConfiguration;
    }

    protected Logger getLogger()
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
        getLogger().debug( "Attempting to authenticate Subject as Anonymous request..." );

        boolean anonymousLoginSuccessful = false;

        Subject subject = getSubject( request, response );

        UsernamePasswordToken usernamePasswordToken =
            new UsernamePasswordToken( getNexusConfiguration().getAnonymousUsername(),
                getNexusConfiguration().getAnonymousPassword() );

        try
        {
            request.setAttribute( ANONYMOUS_LOGIN, Boolean.TRUE );

            subject.login( usernamePasswordToken );
            anonymousLoginSuccessful = true;
        }
        catch ( UnknownSessionException e )
        {
            Session anonSession = subject.getSession( false );

            this.getLogger().debug(
                "Unknown session exception while logging in anonymous user: '{}' with principal '{}'",
                new Object[] { anonSession, subject.getPrincipal(), e } );

            if ( anonSession != null )
            {
                // clear the session
                this.getLogger().debug( "Logging out the current anonymous user, to clear the session." );
                try
                {
                    subject.logout();
                }
                catch ( UnknownSessionException expectedException )
                {
                    this.logger.trace(
                        "Forced a logout with an Unknown Session so the current subject would get cleaned up.", e );
                }

                // login again
                this.getLogger().debug( "Attempting to login as anonymous for the second time." );
                subject.login( usernamePasswordToken );

                anonymousLoginSuccessful = true;
            }
        }
        catch ( AuthenticationException ae )
        {
            getLogger().info(
                "Unable to authenticate user [anonymous] from IP Address "
                    + RemoteIPFinder.findIP( (HttpServletRequest) request ) );

            getLogger().debug( "Unable to log in subject as anonymous", ae );
        }

        if ( anonymousLoginSuccessful )
        {
            getLogger().debug( "Successfully logged in as anonymous" );

            postAuthcEvent( request, getNexusConfiguration().getAnonymousUsername(), getUserAgent( request ), true );

            return true;
        }

        // always default to false. If we've made it to this point in the code, that
        // means the authentication attempt either never occured, or wasn't successful:
        return false;
    }

    private void postAuthcEvent( ServletRequest request, String username, String userAgent, boolean success )
    {
        if ( applicationEventMulticaster != null )
        {
            applicationEventMulticaster.notifyEventListeners( new NexusAuthenticationEvent( this, new ClientInfo(
                username, RemoteIPFinder.findIP( (HttpServletRequest) request ), userAgent ), success ) );
        }
    }

    @Override
    protected boolean onLoginSuccess( AuthenticationToken token, Subject subject, ServletRequest request,
                                      ServletResponse response )
    {
        postAuthcEvent( request, token.getPrincipal().toString(), getUserAgent( request ), true );

        return true;
    }

    @Override
    protected boolean onLoginFailure( AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                      ServletResponse response )
    {
        postAuthcEvent( request, token.getPrincipal().toString(), getUserAgent( request ), false );

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
        if ( request.getAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED ) != null )
        {
            if ( request.getAttribute( ANONYMOUS_LOGIN ) != null )
            {
                sendChallenge( request, response );
            }
            else
            {

                if ( getLogger().isDebugEnabled() )
                {
                    final Subject subject = getSubject( request, response );

                    String username;

                    if ( subject != null && subject.isAuthenticated() && subject.getPrincipal() != null )
                    {
                        username = subject.getPrincipal().toString();
                    }
                    else
                    {
                        username = getNexusConfiguration().getAnonymousUsername();
                    }

                    getLogger().debug(
                        "Request processing is rejected because user \"" + username + "\" lacks permissions." );
                }

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

    // Will retrieve authz header. if missing from header, will try
    // to retrieve from request params instead
    @Override
    protected String getAuthzHeader( ServletRequest request )
    {
        String authzHeader = super.getAuthzHeader( request );

        // If in header use it
        if ( !StringUtils.isEmpty( authzHeader ) )
        {
            getLogger().debug( "Using authorization header from request" );
            return authzHeader;
        }
        // otherwise check request params for it
        else
        {
            authzHeader = request.getParameter( "authorization" );

            if ( !StringUtils.isEmpty( authzHeader ) )
            {
                getLogger().debug( "Using authorization from request parameter" );
            }
            else
            {
                getLogger().debug( "No authorization found (header or request parameter)" );
            }

            return authzHeader;
        }
    }

    // work around to accept password with ':' character
    @Override
    protected String[] getPrincipalsAndCredentials( String scheme, String encoded )
    {
        // no credentials, no auth
        if ( StringUtils.isEmpty( encoded ) )
        {
            return null;
        }

        String decoded = Base64.decodeToString( encoded );

        // no credentials, no auth
        if ( StringUtils.isEmpty( encoded ) )
        {
            return null;
        }

        String[] parts = decoded.split( ":" );

        // invalid credentials, no auth
        if ( parts == null || parts.length < 2 )
        {
            return null;
        }

        return new String[] { parts[0], decoded.substring( parts[0].length() + 1 ) };
    }

    // ==

    protected Object getAttribute( String key )
    {
        return getFilterConfig().getServletContext().getAttribute( key );
    }

    private String getUserAgent( final ServletRequest request )
    {
        if ( request instanceof HttpServletRequest )
        {
            final String userAgent = ( (HttpServletRequest) request ).getHeader( "User-Agent" );

            return userAgent;
        }

        return null;
    }
}
