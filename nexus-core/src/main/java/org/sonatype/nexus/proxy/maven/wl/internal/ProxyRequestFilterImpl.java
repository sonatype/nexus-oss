/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.ProxyRequestFilter;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.maven.wl.events.WLPublishedRepositoryEvent;
import org.sonatype.nexus.proxy.maven.wl.events.WLUnpublishedRepositoryEvent;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

/**
 * Default implementation of the {@link ProxyRequestFilter}.
 * 
 * @author cstamas
 */
@Named
@Singleton
public class ProxyRequestFilterImpl
    extends AbstractLoggingComponent
    implements ProxyRequestFilter
{
    private final ApplicationStatusSource applicationStatusSource;

    private final WLConfig config;

    private final WLManager wlManager;

    /**
     * Constructor.
     * 
     * @param eventBus
     * @param applicationStatusSource
     * @param config
     * @param wlManager
     */
    @Inject
    public ProxyRequestFilterImpl( final EventBus eventBus, final ApplicationStatusSource applicationStatusSource,
                                   final WLConfig config, final WLManager wlManager )
    {
        checkNotNull( eventBus );
        this.applicationStatusSource = checkNotNull( applicationStatusSource );
        this.config = checkNotNull( config );
        this.wlManager = checkNotNull( wlManager );
        eventBus.register( this );
    }

    @Override
    public boolean allowed( final MavenProxyRepository mavenProxyRepository,
                            final ResourceStoreRequest resourceStoreRequest )
    {
        final PathMatcher whitelist = getWhitelistFor( mavenProxyRepository );
        if ( whitelist != null )
        {
            final boolean allowed = whitelist.matches( resourceStoreRequest.getRequestPath() );
            if ( !allowed )
            {
                // flag the request as rejected
                resourceStoreRequest.getRequestContext().put( WLManager.REQUEST_REJECTED_FLAG_KEY, Boolean.TRUE );
            }
        }
        // no WL for a proxy it does not publishes it
        return true;
    }

    // ==

    private final ConcurrentHashMap<String, PathMatcher> whitelists = new ConcurrentHashMap<String, PathMatcher>();

    protected PathMatcher getWhitelistFor( final MavenProxyRepository mavenProxyRepository )
    {
        return whitelists.get( mavenProxyRepository.getId() );
    }

    protected boolean dropWhitelistFor( final MavenProxyRepository mavenProxyRepository )
    {
        return whitelists.remove( mavenProxyRepository.getId() ) != null;
    }

    protected void buildWhitelistFor( final MavenProxyRepository mavenProxyRepository )
    {
        try
        {
            final EntrySource entrySource = wlManager.getEntrySourceFor( mavenProxyRepository );
            if ( entrySource.exists() )
            {
                final PathMatcher whitelist =
                    new PathMatcherImpl( entrySource.readEntries(), config.getWLMatchingDepth() );
                whitelists.put( mavenProxyRepository.getId(), whitelist );
            }
            else
            {
                dropWhitelistFor( mavenProxyRepository );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not build WL!", e );
            dropWhitelistFor( mavenProxyRepository );
        }
    }

    // == Events

    protected boolean isRepositoryHandled( final Repository repository )
    {
        return applicationStatusSource.getSystemStatus().isNexusStarted() && repository != null
            && repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class );
    }

    /**
     * Handler for {@link WLPublishedRepositoryEvent} event.
     * 
     * @param evt
     */
    @Subscribe
    public void onWLPUblishedEvent( WLPublishedRepositoryEvent evt )
    {
        final MavenProxyRepository mavenProxyRepository = evt.getRepository().adaptToFacet( MavenProxyRepository.class );
        if ( isRepositoryHandled( mavenProxyRepository ) )
        {
            buildWhitelistFor( mavenProxyRepository );
        }
    }

    /**
     * Handler for {@link WLUnpublishedRepositoryEvent} event.
     * 
     * @param evt
     */
    @Subscribe
    public void onWLUnpblishedEvent( WLUnpublishedRepositoryEvent evt )
    {
        final MavenProxyRepository mavenProxyRepository = evt.getRepository().adaptToFacet( MavenProxyRepository.class );
        if ( isRepositoryHandled( mavenProxyRepository ) )
        {
            dropWhitelistFor( mavenProxyRepository );
        }
    }
}
