/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security;

import java.io.Serializable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.filter.authc.NexusHttpAuthenticationFilter;

/**
 * @deprecated replaced with 'noSessionCreation' filter in Shiro 1.2.
 */
@Deprecated
public class StatelessAndStatefulWebSessionManager
    extends DefaultWebSessionManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    public static final String NO_SESSION_HEADER = "X-Nexus-Session";
    public static final String DO_NOT_STORE_SESSION_KEY = "NO_SESSION";

    private SessionIdGenerator fakeSessionIdGenerator = new JavaUuidSessionIdGenerator();

    public StatelessAndStatefulWebSessionManager()
    {
        log.info( "@Deprecated replaced with 'noSessionCreation' filter in Shiro 1.2" );
    }

    protected Session doCreateSession( SessionContext context )
    {
        Session session;
        if( WebUtils.isHttp( context ) )
        {
            session = newSessionInstance( context );
            if ( log.isTraceEnabled() )
            {
                log.trace( "Creating session for host {}", session.getHost() );
            }

            HttpServletRequest request = WebUtils.getHttpRequest( context );

            if ( isStatelessClient( request ) )
            {
                // we still need to set the session id, WHY?
                ( (SimpleSession) session ).setId( fakeSessionIdGenerator.generateId( session ) );
                log.debug( "Stateless client session {} is not persisted.", session.getId() );
                session.setAttribute( DO_NOT_STORE_SESSION_KEY, Boolean.TRUE );
            }
            else
            {
                create( session );
            }

            // add a little more logging.
            if ( log.isTraceEnabled() )
            {
                log.trace( "Session {} was created for User-Agent {}", session.getId(), getUserAgent( request ) );
            }
        }
        else
        {
            log.trace( "Non http request, falling back to default implementation." );
            session = super.doCreateSession( context );
        }

        return session;
    }

    /**
     * Does NOT store the session on change if the DO_NOT_STORE_SESSION_KEY is set as an attribute.
     * @param session
     */
    @Override
    protected void onChange( Session session )
    {
        if( !(SimpleSession.class.isInstance( session ) && Boolean.TRUE == session.getAttribute( DO_NOT_STORE_SESSION_KEY )  ) )
        {
            super.onChange( session );
        }
    }

    /**
     * If this Session has the DO_NOT_STORE_SESSION_KEY it will be returned as is so it can be directly attached to the subject, otherwise we just call super.
     * @param session
     * @param context
     */
    @Override
    protected Session createExposedSession(Session session, SessionContext context)
    {
        if( (SimpleSession.class.isInstance( session ) && Boolean.TRUE == session.getAttribute( DO_NOT_STORE_SESSION_KEY )  ) )
        {
            return session;
        }

        return super.createExposedSession( session, context );
    }

    @Override
    protected void onStart( Session session, SessionContext context )
    {
        if ( !WebUtils.isHttp( context ) )
        {
            log.debug( "SessionContext argument is not HTTP compatible or does not have an HTTP request/response "
                + "pair. No session ID cookie will be set." );
            return;

        }
        HttpServletRequest request = WebUtils.getHttpRequest( context );
        HttpServletResponse response = WebUtils.getHttpResponse( context );

        if ( isSessionIdCookieEnabled( request, response ) )
        {
            Serializable sessionId = session.getId();
            storeSessionId( sessionId, request, response );
        }
        else
        {
            log.debug( "Session ID cookie is disabled.  No cookie has been set for new session with id {}",
                       session.getId() );
        }

        request.removeAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE );
        request.setAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE );
    }

    public boolean isSessionIdCookieEnabled( ServletRequest request, ServletResponse response )
    {
        return isSessionIdCookieEnabled() && !isStatelessClient( request );
    }

    // //////////
    // access private methods
    // //////////

    protected Serializable getSessionId( ServletRequest request, ServletResponse response )
    {
        return getReferencedSessionId( request, response );
    }

    private String getSessionIdCookieValue( ServletRequest request, ServletResponse response )
    {
        if ( !isSessionIdCookieEnabled( request, response ) )
        {
            log.debug( "Session ID cookie is disabled - session id will not be acquired from a request cookie." );
            return null;
        }
        if ( !( request instanceof HttpServletRequest ) )
        {
            log.debug( "Current request is not an HttpServletRequest - cannot get session ID cookie.  Returning null." );
            return null;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return getSessionIdCookie().readValue( httpRequest, WebUtils.toHttp( response ) );
    }

    private Serializable getReferencedSessionId( ServletRequest request, ServletResponse response )
    {

        String id = getSessionIdCookieValue( request, response );
        if ( id != null )
        {
            request.setAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                                  ShiroHttpServletRequest.COOKIE_SESSION_ID_SOURCE );
        }
        else
        {
            // not in a cookie, or cookie is disabled - try the request params as a fallback (i.e. URL rewriting):
            id = request.getParameter( ShiroHttpSession.DEFAULT_SESSION_ID_NAME );
            if ( id == null )
            {
                // try lowercase:
                id = request.getParameter( ShiroHttpSession.DEFAULT_SESSION_ID_NAME.toLowerCase() );
            }
            if ( id != null )
            {
                request.setAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                                      ShiroHttpServletRequest.URL_SESSION_ID_SOURCE );
            }
        }
        if ( id != null )
        {
            request.setAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_ID, id );
            // automatically mark it valid here. If it is invalid, the
            // onUnknownSession method below will be invoked and we'll remove the attribute at that time.
            request.setAttribute( ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE );
        }
        return id;
    }

    private void storeSessionId( Serializable currentId, HttpServletRequest request, HttpServletResponse response )
    {
        if ( currentId == null )
        {
            String msg = "sessionId cannot be null when persisting for subsequent requests.";
            throw new IllegalArgumentException( msg );
        }
        Cookie template = getSessionIdCookie();
        Cookie cookie = new SimpleCookie( template );
        String idString = currentId.toString();
        cookie.setValue( idString );
        cookie.saveTo( request, response );
        log.trace( "Set session ID cookie for session with id {}", idString );
    }

    protected boolean isStatelessClient( final ServletRequest request )
    {
        if( hasNoSessionHeader( request ) )
        {
            return true;
        }

        if( Boolean.TRUE.equals( request.getAttribute( NexusHttpAuthenticationFilter.ANONYMOUS_LOGIN ) ) )
        {
            return true;
        }

        final String userAgent = getUserAgent( request );

        log.trace( "Found User-Agent: {} in request", userAgent );

        if ( userAgent != null && userAgent.trim().length() > 0 )
        {
            // maven 2.0.10+
            if ( userAgent.startsWith( "Apache-Maven" ) )
            {
                return true;
            }

            // maven pre 2.0.10 and all Java based clients relying on java.net.UrlConnection
            if ( userAgent.startsWith( "Java/" ) )
            {
                return true;
            }

            // ivy
            if ( userAgent.startsWith( "Apache Ivy/" ) )
            {
                return true;
            }

            // curl
            if ( userAgent.startsWith( "curl/" ) )
            {
                return true;
            }

            // wget
            if ( userAgent.startsWith( "Wget/" ) )
            {
                return true;
            }

            // Nexus
            if ( userAgent.startsWith( "Nexus/" ) )
            {
                return true;
            }

            // Artifactory
            if ( userAgent.startsWith( "Artifactory/" ) )
            {
                return true;
            }

            // Apache Archiva
            if ( userAgent.startsWith( "Apache Archiva/" ) )
            {
                return true;
            }

            // M2Eclipse
            if ( userAgent.startsWith( "M2Eclipse/" ) )
            {
                return true;
            }

            // Aether
            if ( userAgent.startsWith( "Aether/" ) )
            {
                return true;
            }
        }

        // we can't decided for sure, let's return the safest
        return false;
    }

    /**
     * Checks for the no session header: "X-Nexus-Session: none".
     * @param request
     * @return true if header 'X-Nexus-Session' is 'none', false otherwise.
     */
    private boolean hasNoSessionHeader( final ServletRequest request )
    {
        //"X-Nexus-Session: none"
        return "none".equals( getHeaderValue( NO_SESSION_HEADER, request ) );
    }

    /**
     * Returns the User-Agent of the request, or null if not set.
     * @param request
     * @return
     */
    private String getUserAgent( final ServletRequest request )
    {
        return getHeaderValue( "User-Agent", request );
    }

    /**
     * Gets a header value from the request if the ServletRequest is a HttpServletRequest.
     *
     * @param headerName
     * @param request
     * @return null if request is not HttpServletRequest, or header is not found.
     */
    private String getHeaderValue( String headerName, final ServletRequest request )
    {
        if ( request instanceof HttpServletRequest )
        {
            final String headerValue = ( (HttpServletRequest) request ).getHeader( headerName );
            return headerValue;
        }
        return null;
    }

}
