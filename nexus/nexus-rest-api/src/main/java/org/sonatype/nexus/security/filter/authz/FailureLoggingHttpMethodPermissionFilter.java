/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthorizationEvent;
import org.sonatype.nexus.auth.ResourceInfo;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.rest.RemoteIPFinder;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

/**
 * A filter that maps the action from the HTTP Verb.
 * 
 * @author cstamas
 */
public class FailureLoggingHttpMethodPermissionFilter
    extends HttpMethodPermissionFilter
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    // this comes from attributes set by plexus helper listener (nexus-web-utils module)
    private PlexusContainer plexusContainer;

    // this comes from Plexus IoC but we need to "lift" them manually, no injection here
    private Nexus nexus;
    
    // this comes from Plexus IoC but we need to "lift" them manually, no injection here
    private NexusConfiguration nexusConfiguration;

    // this comes from Plexus IoC but we need to "lift" them manually, no injection here
    private ApplicationEventMulticaster applicationEventMulticaster;

    protected void onFilterConfigSet()
        throws Exception
    {
        super.onFilterConfigSet();

        plexusContainer = (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );

        nexus = plexusContainer.lookup( Nexus.class );
        nexusConfiguration = plexusContainer.lookup( NexusConfiguration.class );
        applicationEventMulticaster = plexusContainer.lookup( ApplicationEventMulticaster.class );
    }

    protected Logger getLogger()
    {
        return logger;
    }
    
    protected Nexus getNexus()
    {
        return nexus;
    }

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        recordAuthzFailureEvent( request, response );

        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return false;
    }

    private void recordAuthzFailureEvent( ServletRequest request, ServletResponse response )
    {
        Subject subject = getSubject( request, response );

        if ( nexusConfiguration.getAnonymousUsername().equals( subject.getPrincipal() ) )
        {
            return;
        }

        final Action action = Action.valueOf( getHttpMethodAction( request ) );

        final ClientInfo clientInfo =
            new ClientInfo( String.valueOf( subject.getPrincipal() ),
                RemoteIPFinder.findIP( (HttpServletRequest) request ), "n/a" );
        final ResourceInfo resInfo =
            new ResourceInfo( "HTTP", ( (HttpServletRequest) request ).getMethod(), action,
                ( (HttpServletRequest) request ).getRequestURI() );
        final NexusAuthorizationEvent evt = new NexusAuthorizationEvent( this, clientInfo, resInfo, false );

        applicationEventMulticaster.notifyEventListeners( evt );

    }

    protected Object getAttribute( String key )
    {
        return getFilterConfig().getServletContext().getAttribute( key );
    }
}
