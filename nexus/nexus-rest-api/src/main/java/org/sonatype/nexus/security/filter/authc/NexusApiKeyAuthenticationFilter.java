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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.rest.RemoteIPFinder;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

/**
 * {@link AuthenticatingFilter} that looks for credentials in {@link NexusApiKey} HTTP headers.
 */
public class NexusApiKeyAuthenticationFilter
    extends AuthenticatingFilter
{
    private PlexusContainer container;

    private ApplicationEventMulticaster multicaster;

    private NexusConfiguration configuration;

    private Collection<String> keyHints;

    @Override
    protected void onFilterConfigSet()
        throws Exception
    {
        super.onFilterConfigSet();
        try
        {
            container = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );
            multicaster = container.lookup( ApplicationEventMulticaster.class );
            configuration = container.lookup( NexusConfiguration.class );
            keyHints = container.lookupMap( NexusApiKey.class ).keySet();
        }
        catch ( final Exception ignore )
        {
            // ignore...
        }
    }

    @Override
    protected boolean onAccessDenied( final ServletRequest request, final ServletResponse response )
        throws Exception
    {
        try
        {
            return executeLogin( request, response );
        }
        catch ( final Exception ignore )
        {
            return false;
        }
    }

    @Override
    protected AuthenticationToken createToken( final ServletRequest request, final ServletResponse response )
        throws Exception
    {
        final HttpServletRequest http = WebUtils.toHttp( request );
        final Map<String, char[]> keys = new HashMap<String, char[]>();
        final String host = RemoteIPFinder.findIP( http );
        if ( null != keyHints )
        {
            for ( final String h : keyHints )
            {
                final String token = http.getHeader( h );
                if ( null != token && token.length() > 0 )
                {
                    keys.put( h, token.toCharArray() );
                }
            }
        }
        if ( keys.size() > 0 )
        {
            return new NexusApiKeyAuthenticationToken( keys, host );
        }
        if ( null != configuration && configuration.isAnonymousAccessEnabled() )
        {
            return new UsernamePasswordToken( configuration.getAnonymousUsername(),
                                              configuration.getAnonymousPassword(), host );
        }
        return null;
    }

    @Override
    protected boolean onLoginSuccess( final AuthenticationToken token, final Subject subject,
                                      final ServletRequest request, final ServletResponse response )
    {
        return postAuthcEvent( token, WebUtils.toHttp( request ), true );
    }

    @Override
    protected boolean onLoginFailure( final AuthenticationToken token, final AuthenticationException e,
                                      final ServletRequest request, final ServletResponse response )
    {
        return postAuthcEvent( token, WebUtils.toHttp( request ), false );
    }

    private Object getAttribute( final String key )
    {
        return getFilterConfig().getServletContext().getAttribute( key );
    }

    private boolean postAuthcEvent( final AuthenticationToken token, final HttpServletRequest request,
                                    final boolean success )
    {
        if ( null != multicaster )
        {
            final String host;
            if ( token instanceof HostAuthenticationToken )
            {
                host = ( (HostAuthenticationToken) token ).getHost();
            }
            else
            {
                host = RemoteIPFinder.findIP( request );
            }
            final String agent = request.getHeader( "User-Agent" );
            final ClientInfo info = new ClientInfo( String.valueOf( token.getPrincipal() ), host, agent );
            multicaster.notifyEventListeners( new NexusAuthenticationEvent( this, info, success ) );
        }
        return success;
    }
}
