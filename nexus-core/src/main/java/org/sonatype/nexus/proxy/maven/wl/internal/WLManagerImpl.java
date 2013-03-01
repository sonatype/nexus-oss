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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.maven.wl.PrefixSource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryConfig;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus.DStatus;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus.PStatus;
import org.sonatype.nexus.proxy.maven.wl.WLStatus;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult.Outcome;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.events.WLPublishedRepositoryEvent;
import org.sonatype.nexus.proxy.maven.wl.events.WLUnpublishedRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.task.LoggingProgressListener;
import org.sonatype.nexus.util.task.executor.ConstrainedExecutor;
import org.sonatype.nexus.util.task.executor.ConstrainedExecutorImpl;
import org.sonatype.nexus.util.task.executor.Statistics;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;

/**
 * Default implementation.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class WLManagerImpl
    extends AbstractLoggingComponent
    implements WLManager
{
    private final EventBus eventBus;

    private final ApplicationStatusSource applicationStatusSource;

    private final ApplicationConfiguration applicationConfiguration;

    private final RepositoryRegistry repositoryRegistry;

    private final WLConfig config;

    private final LocalContentDiscoverer localContentDiscoverer;

    private final RemoteContentDiscoverer remoteContentDiscoverer;

    private final RemoteStrategy quickRemoteStrategy;

    private final EventDispatcher eventDispatcher;

    /**
     * Plain executor for background batch-updates. This executor runs 1 periodic thread (see constructor) that performs
     * periodic remote WL update, but also executes background "force" updates (initiated by user over REST or when
     * repository is added). But, as background threads are bounded by presence of proxy repositories, and introduce
     * hard limit of possible max executions, it protects this instance that is basically unbounded.
     */
    private final ScheduledExecutorService executor;

    /**
     * Executor used to execute update jobs. It is constrained in a way that no two update jobs will run against one
     * repository.
     */
    private final ConstrainedExecutor constrainedExecutor;

    /**
     * Da constructor.
     * 
     * @param eventBus
     * @param applicationStatusSource
     * @param applicationConfiguration
     * @param repositoryRegistry
     * @param config
     * @param localContentDiscoverer
     * @param remoteContentDiscoverer
     * @param quickRemoteStrategy
     */
    @Inject
    public WLManagerImpl( final EventBus eventBus, final ApplicationStatusSource applicationStatusSource,
                          final ApplicationConfiguration applicationConfiguration,
                          final RepositoryRegistry repositoryRegistry, final WLConfig config,
                          final LocalContentDiscoverer localContentDiscoverer,
                          final RemoteContentDiscoverer remoteContentDiscoverer,
                          @Named( RemotePrefixFileStrategy.ID ) final RemoteStrategy quickRemoteStrategy )
    {
        this.eventBus = checkNotNull( eventBus );
        this.applicationStatusSource = checkNotNull( applicationStatusSource );
        this.applicationConfiguration = checkNotNull( applicationConfiguration );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.config = checkNotNull( config );
        this.localContentDiscoverer = checkNotNull( localContentDiscoverer );
        this.remoteContentDiscoverer = checkNotNull( remoteContentDiscoverer );
        this.quickRemoteStrategy = checkNotNull( quickRemoteStrategy );
        this.executor =
            new ScheduledThreadPoolExecutor( 5, new NexusThreadFactory( "wl", "WL-Updater" ),
                new ThreadPoolExecutor.AbortPolicy() );
        this.constrainedExecutor = new ConstrainedExecutorImpl( executor );
        // register event dispatcher
        this.eventDispatcher = new EventDispatcher( this, config.isFeatureActive() );
        this.eventBus.register( this );
    }

    @Override
    public void startup()
    {
        // init WLs of repositories on boot, but only hosted needs immediate update
        // while proxies - if their WL does not exists, like on 1st boot -- will get
        // inited only and not updated (will be marked for noscrape). Their update will
        // be commenced a bit later, due to timing issues in Pro + Secure Central, where
        // RemoteRepositoryStorage for it is not yet ready (is getting prepped for same this
        // event: NexusStartedEvent).
        // All this is important for 1st boot only, as on subsequent boot WLs will be alredy
        // present.
        {
            final ArrayList<MavenRepository> initableRepositories = new ArrayList<MavenRepository>();
            initableRepositories.addAll( repositoryRegistry.getRepositoriesWithFacet( MavenHostedRepository.class ) );
            initableRepositories.addAll( repositoryRegistry.getRepositoriesWithFacet( MavenProxyRepository.class ) );
            for ( MavenRepository mavenRepository : initableRepositories )
            {
                if ( isMavenRepositorySupported( mavenRepository ) )
                {
                    initializeWhitelist( true, mavenRepository );
                }
            }
        }
        // schedule the "updater" that ping hourly the mayUpdateProxyWhitelist method
        // but wait 1 minute for boot to calm down and then start
        this.executor.scheduleAtFixedRate( new Runnable()
        {
            @Override
            public void run()
            {
                mayUpdateAllProxyWhitelists();
            }
        }, TimeUnit.MINUTES.toMillis( 1 ), TimeUnit.HOURS.toMillis( 1 ), TimeUnit.MILLISECONDS );
        // register event dispatcher, to start receiving events
        eventBus.register( eventDispatcher );
    }

    @Override
    public void shutdown()
    {
        eventBus.unregister( eventDispatcher );
        executor.shutdown();
        constrainedExecutor.cancelAllJobs();
        try
        {
            executor.awaitTermination( 60L, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            getLogger().debug( "Could not cleanly shut down.", e );
        }
    }

    @Override
    public void initializeWhitelist( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        initializeWhitelist( true, mavenRepository );
    }

    protected void initializeWhitelist( final boolean doUpdateIfNeeded, final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        getLogger().debug( "Initializing WL of {}.", RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
        final PrefixSource prefixSource = getPrefixSourceFor( mavenRepository );
        try
        {
            if ( prefixSource.exists() )
            {
                // good, we assume is up to date, which should be unless user tampered with it
                // in that case, just delete it + update and should be fixed.
                publish( mavenRepository, prefixSource );
                getLogger().info( "Existing white-list of {} initialized.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            else
            {
                // mark it for noscrape if not marked yet
                // this is mainly important on 1st boot or newly added reposes
                unpublish( mavenRepository );
                if ( doUpdateIfNeeded )
                {
                    updateWhitelist( mavenRepository );
                    getLogger().info( "Updating white-list of {}, it does not exists yet.",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                }
                else
                {
                    getLogger().info( "White-list of {} not exists yet, marked for noscrape.",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Problem during white-list update of {}",
                RepositoryStringUtils.getHumanizedNameString( mavenRepository ), e );
            try
            {
                unpublish( mavenRepository );
            }
            catch ( IOException ioe )
            {
                // silently
            }
        }
    }

    /**
     * Method meant to be invoked on regular periods (like hourly, as we defined "resolution" of WL update period in
     * hours too), and will perform WL update only on those proxy repositories that needs it.
     */
    protected void mayUpdateAllProxyWhitelists()
    {
        final List<MavenProxyRepository> mavenProxyRepositories =
            repositoryRegistry.getRepositoriesWithFacet( MavenProxyRepository.class );
        for ( MavenProxyRepository mavenProxyRepository : mavenProxyRepositories )
        {
            try
            {
                mayUpdateProxyWhitelist( mavenProxyRepository );
            }
            catch ( IllegalStateException e )
            {
                // just neglect it and continue, this one might be auto blocked if proxy or put out of service
                getLogger().trace( "Proxy repository {} is not in state to be updated", mavenProxyRepository.getId() );
            }
        }
    }

    /**
     * Method meant to be invoked on regular periods (like hourly, as we defined "resolution" of WL update period in
     * hours too), and will perform WL update on proxy repository only if needed (WL is stale, or does not exists).
     * 
     * @param mavenProxyRepository
     * @return {@code true} if update has been spawned, {@code false} if no update needed (WL is up to date or remote
     *         discovery is disable for repository).
     * @throws IllegalStateException see {@link #updateWhitelist(MavenRepository)}.
     */
    protected boolean mayUpdateProxyWhitelist( final MavenProxyRepository mavenProxyRepository )
        throws IllegalStateException
    {
        final WLDiscoveryStatus discoveryStatus = getStatusFor( mavenProxyRepository ).getDiscoveryStatus();
        if ( discoveryStatus.getStatus().isEnabled() )
        {
            // only update if any of these below are true:
            // status is ENABLED (never ran before)
            // status is ERROR (hit an error during last discovery)
            // status is SUCCESSFUL or UNSUCCESFUL and WL update period is here
            final WLDiscoveryConfig config = getRemoteDiscoveryConfig( mavenProxyRepository );
            if ( discoveryStatus.getStatus() == DStatus.ENABLED
                || discoveryStatus.getStatus() == DStatus.ERROR
                || ( ( System.currentTimeMillis() - discoveryStatus.getLastDiscoveryTimestamp() ) > config.getDiscoveryInterval() ) )
            {
                if ( discoveryStatus.getStatus() == DStatus.ENABLED )
                {
                    getLogger().debug( "Proxy repository {} has never been discovered before, updating it.",
                        RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                }
                else if ( discoveryStatus.getStatus() == DStatus.ERROR )
                {
                    getLogger().debug( "Proxy repository {} previous discovery hit an error, updating it.",
                        RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                }
                else
                {
                    getLogger().debug( "Proxy repository {} needs periodic remote discovery update, updating it.",
                        RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                }
                final boolean updateSpawned = updateWhitelist( mavenProxyRepository );
                if ( !updateSpawned )
                {
                    // this means that either remote discovery takes too long or user might pressed Force discovery
                    // on UI for moments before this call kicked in. Anyway, warn the user in logs
                    getLogger().info(
                        "Proxy repository's {} periodic remote discovery not spawned, as there is already an ongoing job doing it. Consider raising the update interval for it.",
                        RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                }
                return updateSpawned;
            }
        }
        else
        {
            getLogger().info(
                "Proxy repository {} white-list update requested, but it's remote discovery is disabled, not updating it.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
        }
        return false;
    }

    @Override
    public boolean updateWhitelist( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        return doUpdateWhitelist( false, mavenRepository );
    }

    @Override
    public boolean forceUpdateWhitelist( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        return doUpdateWhitelist( true, mavenRepository );
    }

    @Override
    public void forceProxyQuickUpdateWhitelist( final MavenProxyRepository mavenProxyRepository )
        throws IllegalStateException
    {
        checkUpdateConditions( mavenProxyRepository );
        try
        {
            getLogger().debug( "Quick updating WL of {}.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
            constrainedExecutor.cancelRunningWithKey( mavenProxyRepository.getId() );
            final PrefixSource prefixSource =
                updateProxyWhitelist( mavenProxyRepository, Collections.singletonList( quickRemoteStrategy ) );
            if ( prefixSource != null )
            {
                getLogger().info( "Updated and published white-list of {}.",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                publish( mavenProxyRepository, prefixSource );
            }
            else
            {
                getLogger().info( "Unpublished white-list of {} (and is marked for noscrape).",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                unpublish( mavenProxyRepository );
            }
        }
        catch ( final Exception e )
        {
            try
            {
                unpublish( mavenProxyRepository );
            }
            catch ( IOException ioe )
            {
                // silently
            }
            // propagate original exception
            Throwables.propagate( e );
        }
    }

    @Override
    public boolean isMavenRepositorySupported( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        final MavenShadowRepository mavenShadowRepository = mavenRepository.adaptToFacet( MavenShadowRepository.class );
        if ( mavenShadowRepository != null )
        {
            return false; // shadows unsupported
        }
        if ( !Maven2ContentClass.ID.equals( mavenRepository.getRepositoryContentClass().getId() ) )
        {
            return false; // maven2 layout support only, no maven1 support
        }
        return true;
    }

    protected boolean isMavenRepositoryUpdateable( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        if ( !isMavenRepositorySupported( mavenRepository ) )
        {
            return false;
        }
        final LocalStatus localStatus = mavenRepository.getLocalStatus();
        if ( !localStatus.shouldServiceRequest() )
        {
            return false; // out of service
        }
        final MavenProxyRepository mavenProxyRepository = mavenRepository.adaptToFacet( MavenProxyRepository.class );
        if ( mavenProxyRepository != null )
        {
            final ProxyMode proxyMode = mavenProxyRepository.getProxyMode();
            if ( !proxyMode.shouldProxy() )
            {
                return false; // proxy mode != ALLOW
            }
        }
        return true;
    }

    protected void checkUpdateConditions( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        if ( !isMavenRepositorySupported( mavenRepository ) )
        {
            throw new IllegalStateException( "Maven repository "
                + RepositoryStringUtils.getHumanizedNameString( mavenRepository )
                + " not supported by WL feature (only Maven2 hosted, proxy and group repositories are supported)." );
        }
        if ( !isMavenRepositoryUpdateable( mavenRepository ) )
        {
            throw new IllegalStateException( "Maven repository "
                + RepositoryStringUtils.getHumanizedNameString( mavenRepository )
                + " not in state to be updated (either out of service or if proxy, remote access not allowed)." );
        }
    }

    protected boolean doUpdateWhitelist( final boolean forced, final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        checkUpdateConditions( mavenRepository );
        final WLUpdateRepositoryRunnable updateRepositoryJob =
            new WLUpdateRepositoryRunnable( new LoggingProgressListener( getLogger() ), applicationStatusSource, this,
                mavenRepository );
        if ( forced )
        {
            final boolean canceledPreviousJob =
                constrainedExecutor.mustExecute( mavenRepository.getId(), updateRepositoryJob );
            if ( canceledPreviousJob )
            {
                // this is okay, as forced happens rarely, currently only when proxy repo changes remoteURL
                // (reconfiguration happens)
                getLogger().debug( "Forced white-list update on {} canceled currently running discovery.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            return canceledPreviousJob;
        }
        else
        {
            return constrainedExecutor.mayExecute( mavenRepository.getId(), updateRepositoryJob );
        }
    }

    protected boolean isUpdateWhitelistJobRunning()
    {
        final Statistics statistics = constrainedExecutor.getStatistics();
        getLogger().debug( "Running update jobs for {}.", statistics.getCurrentlyRunningJobKeys() );
        return !statistics.getCurrentlyRunningJobKeys().isEmpty();
    }

    protected void updateAndPublishWhitelist( final MavenRepository mavenRepository, final boolean notify )
        throws IOException
    {
        getLogger().debug( "Updating WL of {}.", RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
        final PrefixSource prefixSource;
        if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenGroupRepository.class ) )
        {
            prefixSource = updateGroupWhitelist( mavenRepository.adaptToFacet( MavenGroupRepository.class ) );
        }
        else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            prefixSource = updateProxyWhitelist( mavenRepository.adaptToFacet( MavenProxyRepository.class ), null );
        }
        else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) )
        {
            prefixSource = updateHostedWhitelist( mavenRepository.adaptToFacet( MavenHostedRepository.class ) );
        }
        else
        {
            getLogger().info( "Repository {} not supported by white-list feature, not updating it.",
                RepositoryStringUtils.getFullHumanizedNameString( mavenRepository ) );
            return;
        }
        if ( prefixSource != null )
        {
            if ( notify )
            {
                getLogger().info( "Updated and published white-list of {}.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            publish( mavenRepository, prefixSource );
        }
        else
        {
            if ( notify )
            {
                getLogger().info( "Unpublished white-list of {} (and is marked for noscrape).",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            unpublish( mavenRepository );
        }
    }

    protected PrefixSource updateProxyWhitelist( final MavenProxyRepository mavenProxyRepository,
                                                 final List<RemoteStrategy> remoteStrategies )
        throws IOException
    {
        if ( !isMavenRepositoryUpdateable( mavenProxyRepository ) )
        {
            getLogger().debug(
                "Proxy repository {} is not updateable (is out of service or proxying is not enabled on it), not updating WL.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
            return null;
        }
        PrefixSource prefixSource = null;
        final WLDiscoveryConfig config = getRemoteDiscoveryConfig( mavenProxyRepository );
        if ( config.isEnabled() )
        {
            final DiscoveryResult<MavenProxyRepository> discoveryResult;
            if ( null == remoteStrategies )
            {
                discoveryResult = remoteContentDiscoverer.discoverRemoteContent( mavenProxyRepository );
            }
            else
            {
                discoveryResult =
                    remoteContentDiscoverer.discoverRemoteContent( mavenProxyRepository, remoteStrategies );
            }

            if ( discoveryResult.isSuccessful() )
            {
                prefixSource = discoveryResult.getPrefixSource();
            }
            else
            {
                getLogger().debug( "{} remote discovery unsuccessful.",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
            }
            final PropfileDiscoveryStatusSource discoveryStatusSource =
                new PropfileDiscoveryStatusSource( mavenProxyRepository );
            final Outcome lastOutcome = discoveryResult.getLastResult();

            final DStatus status;
            if ( lastOutcome.isSuccessful() )
            {
                status = DStatus.SUCCESSFUL;
            }
            else
            {
                if ( lastOutcome.getThrowable() == null )
                {
                    status = DStatus.UNSUCCESSFUL;
                }
                else
                {
                    status = DStatus.ERROR;
                }
            }
            final WLDiscoveryStatus discoveryStatus =
                new WLDiscoveryStatus( status, lastOutcome.getStrategyId(), lastOutcome.getMessage(),
                    System.currentTimeMillis() );
            discoveryStatusSource.write( discoveryStatus );
        }
        else
        {
            getLogger().info( "{} remote discovery disabled.",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
        }
        return prefixSource;
    }

    protected PrefixSource updateHostedWhitelist( final MavenHostedRepository mavenHostedRepository )
        throws IOException
    {
        if ( !isMavenRepositoryUpdateable( mavenHostedRepository ) )
        {
            getLogger().debug( "Hosted repository {} is out of service, not updating WL.",
                RepositoryStringUtils.getHumanizedNameString( mavenHostedRepository ) );
            return null;
        }
        PrefixSource prefixSource = null;
        final DiscoveryResult<MavenHostedRepository> discoveryResult =
            localContentDiscoverer.discoverLocalContent( mavenHostedRepository );
        if ( discoveryResult.isSuccessful() )
        {
            prefixSource = discoveryResult.getPrefixSource();
        }
        else
        {
            getLogger().debug( "{} local discovery unsuccessful.",
                RepositoryStringUtils.getHumanizedNameString( mavenHostedRepository ) );
        }
        return prefixSource;
    }

    protected PrefixSource updateGroupWhitelist( final MavenGroupRepository mavenGroupRepository )
        throws IOException
    {
        if ( !isMavenRepositoryUpdateable( mavenGroupRepository ) )
        {
            getLogger().debug( "Group repository {} is out of service, not updating WL.",
                RepositoryStringUtils.getHumanizedNameString( mavenGroupRepository ) );
            return null;
        }
        PrefixSource prefixSource = null;
        // save merged WL into group's local storage (if all members has WL)
        boolean allMembersHaveWLPublished = true;
        final ArrayList<PrefixSource> memberEntrySources = new ArrayList<PrefixSource>();
        for ( Repository member : mavenGroupRepository.getMemberRepositories() )
        {
            if ( member.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                final PrefixSource memberEntrySource =
                    getPrefixSourceFor( member.adaptToFacet( MavenRepository.class ) );
                if ( !memberEntrySource.exists() )
                {
                    getLogger().debug( "{} group's member {} does not have WL published.",
                        RepositoryStringUtils.getHumanizedNameString( mavenGroupRepository ),
                        RepositoryStringUtils.getHumanizedNameString( member ) );
                    allMembersHaveWLPublished = false;
                    break;
                }
                getLogger().debug( "{} group's member {} does have WL published, merging it in...",
                    RepositoryStringUtils.getHumanizedNameString( mavenGroupRepository ),
                    RepositoryStringUtils.getHumanizedNameString( member ) );
                memberEntrySources.add( memberEntrySource );
            }
        }
        if ( allMembersHaveWLPublished )
        {
            prefixSource = new MergingPrefixSource( memberEntrySources );
        }
        return prefixSource;
    }

    // ==

    @Override
    public WLStatus getStatusFor( final MavenRepository mavenRepository )
    {
        final MavenProxyRepository mavenProxyRepository = mavenRepository.adaptToFacet( MavenProxyRepository.class );
        final boolean remoteDiscoveryEnabled;
        if ( mavenProxyRepository != null )
        {
            final WLDiscoveryConfig discoveryConfig = getRemoteDiscoveryConfig( mavenProxyRepository );
            remoteDiscoveryEnabled = discoveryConfig.isEnabled();
        }
        else
        {
            remoteDiscoveryEnabled = false;
        }

        WLPublishingStatus publishingStatus = null;
        WLDiscoveryStatus discoveryStatus = null;

        // publish status
        final FilePrefixSource publishedEntrySource = getPrefixSourceFor( mavenRepository );
        if ( !publishedEntrySource.exists() )
        {
            final String message;
            if ( isMavenRepositorySupported( mavenRepository ) )
            {
                if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenGroupRepository.class ) )
                {
                    final MavenGroupRepository mavenGroupRepository =
                        mavenRepository.adaptToFacet( MavenGroupRepository.class );
                    final List<String> membersWithoutWhitelists = new ArrayList<String>();
                    for ( Repository member : mavenGroupRepository.getMemberRepositories() )
                    {
                        final MavenRepository memberMavenRepository = member.adaptToFacet( MavenRepository.class );
                        if ( null != memberMavenRepository )
                        {
                            final PrefixSource ps = getPrefixSourceFor( memberMavenRepository );
                            if ( !ps.exists() )
                            {
                                membersWithoutWhitelists.add( memberMavenRepository.getName() );
                            }
                        }
                    }
                    message =
                        "Publishing not possible, as following members have not published it: "
                            + Joiner.on( ", " ).join( membersWithoutWhitelists );
                }
                else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
                {
                    if ( remoteDiscoveryEnabled )
                    {
                        message = "Discovery in progress or unable to discover remote content (see discovery status).";
                    }
                    else
                    {
                        message = "Remote discovery not enabled.";
                    }
                }
                else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) )
                {
                    message = "Check Nexus logs for more details."; // hosted reposes must be discovered always
                }
                else if ( mavenRepository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
                {
                    message = "Unsupported repository type (only hosted, proxy and groups are supported).";
                }
                else
                {
                    message = "Check Nexus logs for more details.";
                }
            }
            else
            {
                message = "Unsupported repository format (only Maven2 format is supported).";
            }
            publishingStatus = new WLPublishingStatus( PStatus.NOT_PUBLISHED, message, -1, null );
        }
        else
        {
            publishingStatus =
                new WLPublishingStatus( PStatus.PUBLISHED, "Whitelist published successfully.",
                    publishedEntrySource.getLostModifiedTimestamp(), publishedEntrySource.getFilePath() );
        }

        if ( mavenProxyRepository == null )
        {
            discoveryStatus = new WLDiscoveryStatus( DStatus.NOT_A_PROXY );
        }
        else
        {
            if ( !remoteDiscoveryEnabled )
            {
                discoveryStatus = new WLDiscoveryStatus( DStatus.DISABLED );
            }
            else if ( constrainedExecutor.hasRunningWithKey( mavenProxyRepository.getId() ) )
            {
                // still running or never run yet
                discoveryStatus = new WLDiscoveryStatus( DStatus.ENABLED );
            }
            else
            {
                final PropfileDiscoveryStatusSource discoveryStatusSource =
                    new PropfileDiscoveryStatusSource( mavenProxyRepository );
                if ( !discoveryStatusSource.exists() )
                {
                    // still running or never run yet
                    discoveryStatus = new WLDiscoveryStatus( DStatus.ENABLED );
                }
                else
                {
                    try
                    {
                        discoveryStatus = discoveryStatusSource.read();
                    }
                    catch ( IOException e )
                    {
                        Throwables.propagate( e );
                    }
                }
            }
        }
        return new WLStatus( publishingStatus, discoveryStatus );
    }

    @Override
    public WLDiscoveryConfig getRemoteDiscoveryConfig( final MavenProxyRepository mavenProxyRepository )
    {
        final AbstractMavenRepositoryConfiguration configuration =
            (AbstractMavenRepositoryConfiguration) mavenProxyRepository.getCurrentCoreConfiguration().getExternalConfiguration().getConfiguration(
                false );

        return new WLDiscoveryConfig( config.isFeatureActive() && configuration.isWLDiscoveryEnabled(),
            configuration.getWLDiscoveryInterval() );
    }

    @Override
    public void setRemoteDiscoveryConfig( final MavenProxyRepository mavenProxyRepository,
                                          final WLDiscoveryConfig config )
        throws IOException
    {
        final AbstractMavenRepositoryConfiguration configuration =
            (AbstractMavenRepositoryConfiguration) mavenProxyRepository.getCurrentCoreConfiguration().getExternalConfiguration().getConfiguration(
                false );

        final boolean enabledChanged = configuration.isWLDiscoveryEnabled() != config.isEnabled();
        configuration.setWLDiscoveryEnabled( config.isEnabled() );
        configuration.setWLDiscoveryInterval( config.getDiscoveryInterval() );
        applicationConfiguration.saveConfiguration();

        if ( enabledChanged )
        {
            updateWhitelist( mavenProxyRepository );
        }
    }

    @Override
    public FilePrefixSource getPrefixSourceFor( final MavenRepository mavenRepository )
    {
        return new FilePrefixSource( mavenRepository, config.getLocalPrefixFilePath(), config );
    }

    // ==

    @Override
    public boolean offerWLEntry( final MavenHostedRepository mavenHostedRepository, final String entry )
        throws IOException
    {
        if ( constrainedExecutor.hasRunningWithKey( mavenHostedRepository.getId() ) )
        {
            forceUpdateWhitelist( mavenHostedRepository );
            return true;
        }
        final FilePrefixSource prefixSource = getPrefixSourceFor( mavenHostedRepository );
        final RepositoryItemUidLock lock = prefixSource.getRepositoryItemUid().getLock();
        lock.lock( Action.read );
        try
        {
            if ( !prefixSource.exists() )
            {
                return false;
            }
            final WritablePrefixSourceModifier wesm =
                new WritablePrefixSourceModifier( prefixSource, config.getLocalScrapeDepth() );
            wesm.offerEntry( entry );
            if ( wesm.hasChanges() )
            {
                boolean changed = false;
                lock.lock( Action.update );
                try
                {
                    wesm.reset();
                    wesm.offerEntry( entry );
                    changed = wesm.apply();
                    if ( changed )
                    {
                        publish( mavenHostedRepository, prefixSource );
                    }
                }
                finally
                {
                    lock.unlock();
                }
                return changed;
            }
        }
        finally
        {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean revokeWLEntry( final MavenHostedRepository mavenHostedRepository, final String entry )
        throws IOException
    {
        if ( constrainedExecutor.hasRunningWithKey( mavenHostedRepository.getId() ) )
        {
            forceUpdateWhitelist( mavenHostedRepository );
            return true;
        }
        final FilePrefixSource prefixSource = getPrefixSourceFor( mavenHostedRepository );
        final RepositoryItemUidLock lock = prefixSource.getRepositoryItemUid().getLock();
        lock.lock( Action.read );
        try
        {
            if ( !prefixSource.exists() )
            {
                return false;
            }
            final WritablePrefixSourceModifier wesm =
                new WritablePrefixSourceModifier( prefixSource, config.getLocalScrapeDepth() );
            wesm.revokeEntry( entry );
            if ( wesm.hasChanges() )
            {
                boolean changed = false;
                lock.lock( Action.update );
                try
                {
                    wesm.reset();
                    wesm.revokeEntry( entry );
                    changed = wesm.apply();
                    if ( changed )
                    {
                        publish( mavenHostedRepository, prefixSource );
                    }
                }
                finally
                {
                    lock.unlock();
                }
                return changed;
            }
        }
        finally
        {
            lock.unlock();
        }
        return false;
    }

    // ==

    @Override
    public void publish( final MavenRepository mavenRepository, final PrefixSource prefixSource )
        throws IOException
    {
        // publish prefix file
        final FilePrefixSource prefixesFile = getPrefixSourceFor( mavenRepository );
        try
        {
            prefixesFile.writeEntries( prefixSource );
        }
        catch ( InvalidInputException e )
        {
            unpublish( mavenRepository );
            throw e;
        }

        // unset noscrape flag
        removeNoscrapeFlag( mavenRepository );

        // event
        eventBus.post( new WLPublishedRepositoryEvent( mavenRepository, prefixesFile ) );

        // propagate
        propagateWLUpdateOf( mavenRepository );
    }

    @Override
    public void unpublish( final MavenRepository mavenRepository )
        throws IOException
    {
        // delete (if any) published files, even those that user might manually put there
        getPrefixSourceFor( mavenRepository ).delete();

        // TODO: We do this due to RemotePrefixFileStrategy, but this is now scattered (that one may write these file,
        // and here we are cleaning them)
        for ( String path : config.getRemotePrefixFilePaths() )
        {
            new FilePrefixSource( mavenRepository, path, config ).delete();
        }

        // set noscrape flag
        addNoscrapeFlag( mavenRepository );

        // event
        eventBus.post( new WLUnpublishedRepositoryEvent( mavenRepository ) );

        // propagate
        propagateWLUpdateOf( mavenRepository );
    }

    protected void propagateWLUpdateOf( final MavenRepository mavenRepository )
    {
        MavenGroupRepository containingGroupRepository = null;
        final List<GroupRepository> groups = repositoryRegistry.getGroupsOfRepository( mavenRepository );
        for ( GroupRepository groupRepository : groups )
        {
            containingGroupRepository = groupRepository.adaptToFacet( MavenGroupRepository.class );
            if ( mavenRepository != null )
            {
                try
                {
                    // this is a group, so we go with sync method as this is quick
                    updateAndPublishWhitelist( containingGroupRepository, false );
                }
                catch ( IOException e )
                {
                    getLogger().warn(
                        "Problem while cascade updating WL for repository "
                            + RepositoryStringUtils.getHumanizedNameString( containingGroupRepository )
                            + " in response to WL update in member "
                            + RepositoryStringUtils.getHumanizedNameString( mavenRepository ) + ".", e );
                }
            }
        }
    }

    // ==

    protected void addNoscrapeFlag( final MavenRepository mavenRepository )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( config.getLocalNoScrapeFlagPath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        final DefaultStorageFileItem file =
            new DefaultStorageFileItem( mavenRepository, new ResourceStoreRequest( config.getLocalNoScrapeFlagPath() ),
                true, true, new StringContentLocator( "noscrape" ) );
        try
        {
            mavenRepository.storeItem( true, file );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // eh?
        }
        catch ( IllegalOperationException e )
        {
            // eh?
        }
    }

    @SuppressWarnings( "deprecation" )
    protected void removeNoscrapeFlag( final MavenRepository mavenRepository )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( config.getLocalNoScrapeFlagPath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        try
        {
            mavenRepository.deleteItem( true, request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // eh?
        }
        catch ( IllegalOperationException e )
        {
            // eh?
        }
    }

    // ==

    @Override
    public boolean isEventAboutWLFile( RepositoryItemEvent evt )
    {
        return evt.getRepository().getRepositoryKind().isFacetAvailable( MavenRepository.class )
            && evt.getItem() instanceof StorageFileItem
            && config.getLocalPrefixFilePath().equals( evt.getItem().getPath() );
    }

    // ==

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onNexusStartedEvent( final NexusStartedEvent evt )
    {
        startup();
    }

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onNexusStoppedEvent( final NexusStoppedEvent evt )
    {
        shutdown();
    }
}
