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
import java.util.LinkedHashSet;
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

import com.google.common.annotations.VisibleForTesting;
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
        // init WLs of repositories on boot. In first "flight" we do not do any update
        // only ack existing WLs or in case of non-existent (upgrade), we just mark those
        // reposes as noscrape. First the hosted+proxy reposes are processed, and they
        // are gathered into a list that we know they need-update (have no WL at all)
        // 2nd pass is for groups, but they are NOT collected for updates.
        // Finally, those collected for update will get update bg jobs spawned.

        // All this is important for 1st boot only, as on subsequent boot WLs will be already
        // present and just event will be published.
        // hosted + proxies get inited first, collect those needing update
        // those will be all on upgrade, and none on subsequent boots
        final ArrayList<MavenRepository> needUpdateRepositories = new ArrayList<MavenRepository>();
        {
            final ArrayList<MavenRepository> initableRepositories = new ArrayList<MavenRepository>();
            initableRepositories.addAll( repositoryRegistry.getRepositoriesWithFacet( MavenHostedRepository.class ) );
            initableRepositories.addAll( repositoryRegistry.getRepositoriesWithFacet( MavenProxyRepository.class ) );
            for ( MavenRepository mavenRepository : initableRepositories )
            {
                if ( isMavenRepositorySupported( mavenRepository )
                    && mavenRepository.getLocalStatus().shouldServiceRequest() )
                {
                    if ( doInitializeWhitelistOnStartup( mavenRepository ) )
                    {
                        // collect those marked as need-update
                        needUpdateRepositories.add( mavenRepository );
                    }
                }
            }
        }
        // groups get inited next, this mostly means they will be marked as noscrape on upgraded instances,
        // and just a published event will be fired on consequent boots
        {
            final ArrayList<MavenRepository> initableGroupRepositories = new ArrayList<MavenRepository>();
            initableGroupRepositories.addAll( repositoryRegistry.getRepositoriesWithFacet( MavenGroupRepository.class ) );
            for ( MavenRepository mavenRepository : initableGroupRepositories )
            {
                if ( isMavenRepositorySupported( mavenRepository )
                    && mavenRepository.getLocalStatus().shouldServiceRequest() )
                {
                    // groups will not be collected to needs-update list
                    doInitializeWhitelistOnStartup( mavenRepository );
                }
            }
        }
        // spawn all the needed updates as bg jobs
        // these will maintaing groups too as needed
        for ( MavenRepository mavenRepository : needUpdateRepositories )
        {
            updateWhitelist( mavenRepository );
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
            if ( !executor.awaitTermination( 15L, TimeUnit.SECONDS ) )
            {
                executor.shutdownNow();
            }
        }
        catch ( InterruptedException e )
        {
            getLogger().debug( "Could not cleanly shut down", e );
        }
    }

    @Override
    public void initializeWhitelist( final MavenRepository mavenRepository )
    {
        getLogger().debug( "Initializing white-list of newly added {}", mavenRepository );
        try
        {
            // mark it for noscrape if not marked yet
            unpublish( mavenRepository );
            // spawn update, this will do whatever is needed (and handle cases like blocked, out of service etc),
            // and publish
            updateWhitelist( mavenRepository );
            getLogger().info( "Initializing non-existing white-list of newly added {}",
                RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
        }
        catch ( Exception e )
        {
            getLogger().warn( "Problem during white-list initialisation of newly added {}",
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
     * Initializes maven repository WL on startup. Signals with returning {@code true} if the repository needs update of
     * WL.
     * 
     * @param mavenRepository
     * @return {@code true} if repository needs update.
     */
    protected boolean doInitializeWhitelistOnStartup( final MavenRepository mavenRepository )
    {
        getLogger().debug( "Initializing white-list of {}", mavenRepository );
        final PrefixSource prefixSource = getPrefixSourceFor( mavenRepository );
        try
        {
            if ( prefixSource.exists() )
            {
                // good, we assume is up to date, which should be unless user tampered with it
                // in that case, just delete it + update and should be fixed.
                publish( mavenRepository, prefixSource, false );
                getLogger().info( "Existing white-list of {} initialized",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            else
            {
                // mark it for noscrape if not marked yet
                // this is mainly important on 1st boot or newly added reposes
                unpublish( mavenRepository, false );
                getLogger().info( "Initializing non-existing white-list of {}",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                return true;
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Problem during white-list initialisation of {}",
                RepositoryStringUtils.getHumanizedNameString( mavenRepository ), e );
            try
            {
                unpublish( mavenRepository, false );
            }
            catch ( IOException ioe )
            {
                // silently
            }
        }
        return false;
    }

    /**
     * Method meant to be invoked on regular periods (like hourly, as we defined "resolution" of WL update period in
     * hours too), and will perform WL update only on those proxy repositories that needs it.
     */
    protected void mayUpdateAllProxyWhitelists()
    {
        getLogger().trace( "mayUpdateAllProxyWhitelists started" );
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
                getLogger().trace( "Proxy repository {} is not in state to be updated", mavenProxyRepository );
            }
            catch ( Exception e )
            {
                // just neglect it and continue, but do log it
                getLogger().warn( "Problem during white-list update of proxy repository {}",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), e );
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
     */
    protected boolean mayUpdateProxyWhitelist( final MavenProxyRepository mavenProxyRepository )
    {
        final WLDiscoveryStatus discoveryStatus = getStatusFor( mavenProxyRepository ).getDiscoveryStatus();
        if ( discoveryStatus.getStatus().isEnabled() )
        {
            // only update if any of these below are true:
            // status is ERROR or ENABLED_NOT_POSSIBLE (hit an error during last discovery)
            // status is anything else and WL update period is here
            final WLDiscoveryConfig config = getRemoteDiscoveryConfig( mavenProxyRepository );
            if ( discoveryStatus.getStatus() == DStatus.ERROR
                || discoveryStatus.getStatus() == DStatus.ENABLED_NOT_POSSIBLE
                || ( ( System.currentTimeMillis() - discoveryStatus.getLastDiscoveryTimestamp() ) > config.getDiscoveryInterval() ) )
            {
                if ( discoveryStatus.getStatus() == DStatus.ENABLED_IN_PROGRESS )
                {
                    getLogger().debug( "Proxy {} has never been discovered before", mavenProxyRepository );
                }
                else if ( discoveryStatus.getStatus() == DStatus.ENABLED_NOT_POSSIBLE )
                {
                    getLogger().debug( "Proxy {} discovery was not possible before", mavenProxyRepository );
                }
                else if ( discoveryStatus.getStatus() == DStatus.ERROR )
                {
                    getLogger().debug( "Proxy {} previous discovery hit an error", mavenProxyRepository );
                }
                else
                {
                    getLogger().debug( "Proxy {} needs periodic remote discovery update", mavenProxyRepository );
                }
                final boolean updateSpawned = doUpdateWhitelistAsync( false, mavenProxyRepository );
                if ( !updateSpawned )
                {
                    // this means that either remote discovery takes too long or user might pressed Force discovery
                    // on UI for moments before this call kicked in. Anyway, warn the user in logs
                    getLogger().info(
                        "Proxy {} periodic remote discovery skipped as there is an ongoing job updating it, consider raising the update interval for this repository",
                        RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                }
                return updateSpawned;
            }
            else
            {
                getLogger().debug( "Proxy {} white-list is up to date", mavenProxyRepository );
            }
        }
        else
        {
            getLogger().debug( "Proxy {} white-list update requested, but it's remote discovery is disabled",
                mavenProxyRepository );
        }
        return false;
    }

    @Override
    public boolean updateWhitelist( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        checkUpdateConditions( mavenRepository );
        return doUpdateWhitelistAsync( false, mavenRepository );
    }

    @Override
    public boolean forceUpdateWhitelist( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        checkUpdateConditions( mavenRepository );
        return doUpdateWhitelistAsync( true, mavenRepository );
    }

    @Override
    public void forceProxyQuickUpdateWhitelist( final MavenProxyRepository mavenProxyRepository )
        throws IllegalStateException
    {
        checkUpdateConditions( mavenProxyRepository );
        try
        {
            getLogger().debug( "Quick updating white-list of {}", mavenProxyRepository );
            constrainedExecutor.cancelRunningWithKey( mavenProxyRepository.getId() );
            final PrefixSource prefixSource =
                updateProxyWhitelist( mavenProxyRepository, Collections.singletonList( quickRemoteStrategy ) );
            if ( prefixSource != null )
            {
                getLogger().info( "Updated and published white-list of {}",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
                publish( mavenProxyRepository, prefixSource );
            }
            else
            {
                getLogger().info( "Unpublished white-list of {} (and is marked for noscrape)",
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

    /**
     * Checks conditions for repository, is it updateable. If not for any reason, {@link IllegalStateException} is
     * thrown.
     * 
     * @param mavenRepository
     * @throws IllegalStateException when passed in repository cannot be updated for some reason. Reason is message of
     *             the exception being thrown.
     */
    protected void checkUpdateConditions( final MavenRepository mavenRepository )
        throws IllegalStateException
    {
        if ( !isMavenRepositorySupported( mavenRepository ) )
        {
            // we should really not see this, it would mean some execution path is buggy as it gets here
            // with unsupported repo
            throw new IllegalStateException(
                "Repository not supported by white-list feature (only Maven2 hosted, proxy and group repositories are supported)" );
        }
        final LocalStatus localStatus = mavenRepository.getLocalStatus();
        if ( !localStatus.shouldServiceRequest() )
        {
            throw new IllegalStateException( "Repository out of service" );
        }
    }

    /**
     * Performs "background" async update. If {@code forced} is {@code true}, it will always schedule an update job
     * (even at cost of cancelling any currently running one). If {@code forced} is {@code false}, job will be spawned
     * only if another job for same repository is not running.
     * 
     * @param forced if {@code true} will always schedule update job, and might cancel any existing job, if running.
     * @param mavenRepository
     * @return if {@code forced=true}, return value of {@code true} means this invocation did cancel previous job. If
     *         {@code forced=false}, return value {@code true} means this invocation did schedule a job, otherwise it
     *         did not, as another job for same repository was already running.
     */
    protected boolean doUpdateWhitelistAsync( final boolean forced, final MavenRepository mavenRepository )
    {
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
                getLogger().debug( "Forced white-list update on {} canceled currently running discovery job",
                    mavenRepository );
            }
            return canceledPreviousJob;
        }
        else
        {
            return constrainedExecutor.mayExecute( mavenRepository.getId(), updateRepositoryJob );
        }
    }

    /**
     * Is visible to expose over the nexus-it-helper-plugin only, and UTs are using this. Should not be used for other
     * means.
     * 
     * @return {@code true} if there are white-list jobs running.
     */
    @VisibleForTesting
    public boolean isUpdateWhitelistJobRunning()
    {
        final Statistics statistics = constrainedExecutor.getStatistics();
        getLogger().debug( "Running update jobs for {}", statistics.getCurrentlyRunningJobKeys() );
        return !statistics.getCurrentlyRunningJobKeys().isEmpty();
    }

    protected void updateAndPublishWhitelist( final MavenRepository mavenRepository, final boolean notify )
        throws IOException
    {
        getLogger().debug( "Updating white-list of {}", mavenRepository );
        try
        {
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
                getLogger().info( "Repository {} unsupported by white-list feature",
                    RepositoryStringUtils.getFullHumanizedNameString( mavenRepository ) );
                return;
            }
            if ( prefixSource != null )
            {
                if ( notify )
                {
                    getLogger().info( "Updated and published white-list of {}",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                }
                publish( mavenRepository, prefixSource );
            }
            else
            {
                if ( notify )
                {
                    getLogger().info( "Unpublished white-list of {} (and is marked for noscrape)",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                }
                unpublish( mavenRepository );
            }
        }
        catch ( IllegalStateException e )
        {
            // just ack it, log it and return peacefully
            getLogger().info( e.getMessage() );
            return;
        }
    }

    protected PrefixSource updateProxyWhitelist( final MavenProxyRepository mavenProxyRepository,
                                                 final List<RemoteStrategy> remoteStrategies )
        throws IllegalStateException, IOException
    {
        checkUpdateConditions( mavenProxyRepository );

        final PropfileDiscoveryStatusSource discoveryStatusSource =
            new PropfileDiscoveryStatusSource( mavenProxyRepository );

        final ProxyMode proxyMode = mavenProxyRepository.getProxyMode();
        if ( !proxyMode.shouldProxy() )
        {
            final WLDiscoveryStatus discoveryStatus =
                new WLDiscoveryStatus( DStatus.ENABLED_NOT_POSSIBLE, "none", "Proxy repository is blocked.",
                    System.currentTimeMillis() );
            discoveryStatusSource.write( discoveryStatus );
            throw new IllegalStateException( "Maven repository "
                + RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository )
                + " not in state to be updated (is blocked)." );
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

            getLogger().debug( "Results of {} remote discovery: {}", mavenProxyRepository,
                discoveryResult.getAllResults() );

            if ( discoveryResult.isSuccessful() )
            {
                prefixSource = discoveryResult.getPrefixSource();
            }
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
            getLogger().info( "{} remote discovery disabled",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ) );
        }
        return prefixSource;
    }

    protected PrefixSource updateHostedWhitelist( final MavenHostedRepository mavenHostedRepository )
        throws IllegalStateException, IOException
    {
        checkUpdateConditions( mavenHostedRepository );
        PrefixSource prefixSource = null;
        final DiscoveryResult<MavenHostedRepository> discoveryResult =
            localContentDiscoverer.discoverLocalContent( mavenHostedRepository );
        if ( discoveryResult.isSuccessful() )
        {
            prefixSource = discoveryResult.getPrefixSource();
        }
        else
        {
            getLogger().debug( "{} local discovery unsuccessful", mavenHostedRepository );
        }
        return prefixSource;
    }

    protected PrefixSource updateGroupWhitelist( final MavenGroupRepository mavenGroupRepository )
        throws IllegalStateException, IOException
    {
        checkUpdateConditions( mavenGroupRepository );
        PrefixSource prefixSource = null;
        // save merged WL into group's local storage (if all members has WL)
        boolean allMembersHaveWLPublished = true;
        final LinkedHashSet<String> entries = new LinkedHashSet<String>();
        for ( Repository member : mavenGroupRepository.getMemberRepositories() )
        {
            if ( member.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                // neglect completely out of service members
                if ( member.getLocalStatus().shouldServiceRequest() )
                {
                    final FilePrefixSource memberEntrySource =
                        getPrefixSourceFor( member.adaptToFacet( MavenRepository.class ) );
                    // lock to prevent file being deleted between exists check and reading it up
                    final RepositoryItemUidLock lock = memberEntrySource.getRepositoryItemUid().getLock();
                    lock.lock( Action.read );
                    try
                    {
                        if ( !memberEntrySource.exists() )
                        {
                            allMembersHaveWLPublished = false;
                            break;
                        }
                        entries.addAll( memberEntrySource.readEntries() );
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
        }
        if ( allMembersHaveWLPublished )
        {
            prefixSource = new ArrayListPrefixSource( new ArrayList<String>( entries ) );
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
                        "Publishing not possible, following members have no published whitelist: "
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
                discoveryStatus = new WLDiscoveryStatus( DStatus.ENABLED_IN_PROGRESS );
            }
            else
            {
                final PropfileDiscoveryStatusSource discoveryStatusSource =
                    new PropfileDiscoveryStatusSource( mavenProxyRepository );
                if ( !discoveryStatusSource.exists() )
                {
                    if ( !mavenProxyRepository.getLocalStatus().shouldServiceRequest() )
                    {
                        // should run but not yet scheduled, or never run yet
                        // out of service prevents us to persist ending states, so this
                        // is the only place where we actually "calculate" it
                        discoveryStatus =
                            new WLDiscoveryStatus( DStatus.ENABLED_NOT_POSSIBLE, "none",
                                "Repository is out of service.", System.currentTimeMillis() );
                    }
                    else
                    {
                        // should run but not yet scheduled, or never run yet
                        discoveryStatus = new WLDiscoveryStatus( DStatus.ENABLED_IN_PROGRESS );
                    }
                }
                else
                {
                    // all the other "ending" states are persisted
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
        publish( mavenRepository, prefixSource, true );
    }

    protected void publish( final MavenRepository mavenRepository, final PrefixSource prefixSource,
                            final boolean propagate )
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

        if ( propagate )
        {
            // propagate
            propagateWLUpdateOf( mavenRepository );
        }
    }

    @Override
    public void unpublish( final MavenRepository mavenRepository )
        throws IOException
    {
        unpublish( mavenRepository, true );
    }

    protected void unpublish( final MavenRepository mavenRepository, final boolean propagate )
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

        if ( propagate )
        {
            // propagate
            propagateWLUpdateOf( mavenRepository );
        }
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
                        "Problem while cascading white-list update to group repository {} from it's member {}",
                        RepositoryStringUtils.getHumanizedNameString( containingGroupRepository ),
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ), e );
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
