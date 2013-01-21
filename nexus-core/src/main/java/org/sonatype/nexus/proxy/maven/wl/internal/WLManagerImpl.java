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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryConfig;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus.DStatus;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus.PStatus;
import org.sonatype.nexus.proxy.maven.wl.WLStatus;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySource;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySourceModifier;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.events.WLPublishedRepositoryEvent;
import org.sonatype.nexus.proxy.maven.wl.events.WLUnpublishedRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.sisu.goodies.eventbus.EventBus;

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

    private final ApplicationConfiguration applicationConfiguration;

    private final RepositoryRegistry repositoryRegistry;

    private final WLConfig config;

    private final LocalContentDiscoverer localContentDiscoverer;

    private final RemoteContentDiscoverer remoteContentDiscoverer;

    private final EventDispatcher eventDispatcher;

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
     */
    @Inject
    public WLManagerImpl( final EventBus eventBus, final ApplicationStatusSource applicationStatusSource,
                          final ApplicationConfiguration applicationConfiguration,
                          final RepositoryRegistry repositoryRegistry, final WLConfig config,
                          final LocalContentDiscoverer localContentDiscoverer,
                          final RemoteContentDiscoverer remoteContentDiscoverer )
    {
        this.eventBus = checkNotNull( eventBus );
        this.applicationConfiguration = checkNotNull( applicationConfiguration );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.config = checkNotNull( config );
        this.localContentDiscoverer = checkNotNull( localContentDiscoverer );
        this.remoteContentDiscoverer = checkNotNull( remoteContentDiscoverer );
        this.eventDispatcher = new EventDispatcher( applicationStatusSource, this );
        this.eventBus.register( eventDispatcher );
    }

    @Override
    public void initializeAllWhitelists()
    {
        for ( MavenHostedRepository mavenRepository : repositoryRegistry.getRepositoriesWithFacet( MavenHostedRepository.class ) )
        {
            initializeWhitelist( mavenRepository );
        }
        for ( MavenProxyRepository mavenRepository : repositoryRegistry.getRepositoriesWithFacet( MavenProxyRepository.class ) )
        {
            initializeWhitelist( mavenRepository );
        }
    }

    @Override
    public void initializeWhitelist( final MavenRepository mavenRepository )
    {
        getLogger().debug( "Initializing WL of {}.", RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
        final EntrySource entrySource = getEntrySourceFor( mavenRepository );
        try
        {
            if ( entrySource.exists() )
            {
                // good, we assume is up to date, which should be unless user tampered with it
                // in that case, just delete it + update and should be fixed.
                publish( mavenRepository, entrySource );
                getLogger().info( "Existing WL of {} initialized.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            else
            {
                unpublish( mavenRepository );
                updateWhitelist( mavenRepository, true );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem during update " + RepositoryStringUtils.getHumanizedNameString( mavenRepository ) + " WL!", e );
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

    @Override
    public void updateWhitelist( final MavenRepository mavenRepository )
        throws IOException
    {
        updateWhitelist( mavenRepository, true );
    }

    protected void updateWhitelist( final MavenRepository mavenRepository, final boolean notify )
        throws IOException
    {
        getLogger().debug( "Updating WL of {}.", RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
        EntrySource entrySource = null;
        if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenGroupRepository.class ) )
        {
            // save merged WL into group's local storage (if all members has WL)
            boolean allMembersHaveWLPublished = true;
            final MavenGroupRepository mavenGroupRepository = mavenRepository.adaptToFacet( MavenGroupRepository.class );
            final ArrayList<EntrySource> memberEntrySources = new ArrayList<EntrySource>();
            for ( Repository member : mavenGroupRepository.getMemberRepositories() )
            {
                if ( member.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
                {
                    final EntrySource memberEntrySource =
                        getEntrySourceFor( member.adaptToFacet( MavenRepository.class ) );
                    if ( !memberEntrySource.exists() )
                    {
                        getLogger().debug( "{} group's member {} does not have WL published.",
                            RepositoryStringUtils.getHumanizedNameString( mavenRepository ),
                            RepositoryStringUtils.getHumanizedNameString( member ) );
                        allMembersHaveWLPublished = false;
                        break;
                    }
                    getLogger().debug( "{} group's member {} does have WL published, merging it in...",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ),
                        RepositoryStringUtils.getHumanizedNameString( member ) );
                    memberEntrySources.add( memberEntrySource );
                }
            }

            if ( allMembersHaveWLPublished )
            {
                entrySource = new MergingEntrySource( memberEntrySources );
            }
        }
        else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            final MavenProxyRepository mavenProxyRepository = mavenRepository.adaptToFacet( MavenProxyRepository.class );
            final WLDiscoveryConfig config = getRemoteDiscoveryConfig( mavenProxyRepository );
            if ( config.isEnabled() )
            {
                final DiscoveryResult discoveryResult =
                    remoteContentDiscoverer.discoverRemoteContent( mavenProxyRepository );
                if ( discoveryResult.isSuccessful() )
                {
                    entrySource = discoveryResult.getEntrySource();
                }
                else
                {
                    getLogger().debug( "{} remote discovery unsuccessful.",
                        RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
                }
            }
            else
            {
                getLogger().debug( "{} remote discovery disabled.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
        }
        else if ( mavenRepository.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) )
        {
            final DiscoveryResult discoveryResult = localContentDiscoverer.discoverLocalContent( mavenRepository );
            if ( discoveryResult.isSuccessful() )
            {
                entrySource = discoveryResult.getEntrySource();
            }
            else
            {
                getLogger().debug( "{} local discovery unsuccessful.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
        }

        if ( entrySource != null )
        {
            if ( notify )
            {
                getLogger().info( "Updated and published WL of {}.",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            publish( mavenRepository, entrySource );
        }
        else
        {
            if ( notify )
            {
                getLogger().info( "Unpublished WL of {} (and is marked for noscrape).",
                    RepositoryStringUtils.getHumanizedNameString( mavenRepository ) );
            }
            unpublish( mavenRepository );
        }
    }

    @Override
    public WLStatus getStatusFor( final MavenRepository mavenRepository )
    {
        WLPublishingStatus publishingStatus = null;
        WLDiscoveryStatus discoveryStatus = null;

        // publish status
        final FileEntrySource publishedEntrySource = getEntrySourceFor( mavenRepository );
        if ( !publishedEntrySource.exists() )
        {
            publishingStatus = new WLPublishingStatus( PStatus.NOT_PUBLISHED, -1, null );
        }
        else
        {
            publishingStatus =
                new WLPublishingStatus( PStatus.PUBLISHED, publishedEntrySource.getLostModifiedTimestamp(),
                    publishedEntrySource.getFilePath() );
        }

        final MavenProxyRepository mavenProxyRepository = mavenRepository.adaptToFacet( MavenProxyRepository.class );
        if ( mavenProxyRepository == null )
        {
            discoveryStatus = new WLDiscoveryStatus( DStatus.NOT_A_PROXY, null, -1 );
        }
        else
        {
            final WLDiscoveryConfig discoveryConfig = getRemoteDiscoveryConfig( mavenProxyRepository );
            if ( !discoveryConfig.isEnabled() )
            {
                discoveryStatus = new WLDiscoveryStatus( DStatus.DISABLED, null, -1 );
            }
            else
            {
                if ( !publishedEntrySource.exists() )
                {
                    discoveryStatus = new WLDiscoveryStatus( DStatus.FAILED, null, -1 );
                }
                else
                {
                    discoveryStatus =
                        new WLDiscoveryStatus( DStatus.SUCCESSFUL, "dunno-yet",
                            publishedEntrySource.getLostModifiedTimestamp() );
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

        return new WLDiscoveryConfig( configuration.isWLDiscoveryEnabled(), configuration.getWLDiscoveryInterval() );
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
        if ( config.isEnabled() )
        {
            configuration.setWLDiscoveryInterval( config.getDiscoveryInterval() );
        }
        else
        {
            configuration.setWLDiscoveryInterval( -1 );
        }
        applicationConfiguration.saveConfiguration();

        if ( enabledChanged )
        {
            updateWhitelist( mavenProxyRepository );
        }
    }

    @Override
    public FileEntrySource getEntrySourceFor( final MavenRepository mavenRepository )
    {
        return new FileEntrySource( mavenRepository, config.getLocalPrefixFilePath() );
    }

    // ==

    public boolean offerWLEntries( final MavenHostedRepository mavenHostedRepository, String... entries )
        throws IOException
    {
        final WritableEntrySource entrySource = getEntrySourceFor( mavenHostedRepository );
        final WritableEntrySourceModifier wesm =
            new WritableEntrySourceModifierImpl( entrySource, config.getLocalScrapeDepth() );
        wesm.offerEntries( entries );
        if ( wesm.apply() )
        {
            publish( mavenHostedRepository, entrySource );
            return true;
        }
        return false;
    }

    public boolean revokeWLEntries( final MavenHostedRepository mavenHostedRepository, String... entries )
        throws IOException
    {
        final WritableEntrySource entrySource = getEntrySourceFor( mavenHostedRepository );
        final WritableEntrySourceModifier wesm =
            new WritableEntrySourceModifierImpl( entrySource, config.getLocalScrapeDepth() );
        wesm.revokeEntries( entries );
        if ( wesm.apply() )
        {
            publish( mavenHostedRepository, entrySource );
            return true;
        }
        return false;
    }

    // ==

    @Override
    public void publish( final MavenRepository mavenRepository, final EntrySource entrySource )
        throws IOException
    {
        // publish prefix file
        final FileEntrySource prefixesFile = getEntrySourceFor( mavenRepository );
        prefixesFile.writeEntries( entrySource );

        // unset noscrape flag
        removeNoscrapeFlag( mavenRepository );

        // event
        eventBus.post( new WLPublishedRepositoryEvent( mavenRepository, prefixesFile ) );

        // propagate
        propagateWLUpdateOf( mavenRepository );
    }

    @Override
    public void republish( final MavenRepository mavenRepository )
        throws IOException
    {
        publish( mavenRepository, getEntrySourceFor( mavenRepository ) );
    }

    @Override
    public void unpublish( final MavenRepository mavenRepository )
        throws IOException
    {
        // delete (if any) published files, even those that user might manually put there
        getEntrySourceFor( mavenRepository ).delete();

        // TODO: We do this due to RemotePrefixFileStrategy, but this is now scattered (that one may write these file,
        // and here we are cleaning them)
        for ( String path : config.getRemotePrefixFilePaths() )
        {
            new FileEntrySource( mavenRepository, path ).delete();
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
        MavenRepository containingGroupRepository = null;
        final List<GroupRepository> groups = repositoryRegistry.getGroupsOfRepository( mavenRepository );
        for ( GroupRepository groupRepository : groups )
        {
            containingGroupRepository = groupRepository.adaptToFacet( MavenRepository.class );
            if ( mavenRepository != null )
            {
                try
                {
                    updateWhitelist( containingGroupRepository, false );
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
        final ResourceStoreRequest request = new ResourceStoreRequest( config.getNoScrapeFlagPath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        final DefaultStorageFileItem file =
            new DefaultStorageFileItem( mavenRepository, new ResourceStoreRequest( config.getNoScrapeFlagPath() ),
                true, true, new StringContentLocator( "noscrape" ) );
        try
        {
            mavenRepository.storeItemWithChecksums( true, file );
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

    protected void removeNoscrapeFlag( final MavenRepository mavenRepository )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( config.getNoScrapeFlagPath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        try
        {
            mavenRepository.deleteItemWithChecksums( true, request );
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

    private final static String MARKER_KEY = AbstractFileEntrySource.class.getName();

    @Override
    public void markRequestContext( RequestContext ctx )
    {
        ctx.put( MARKER_KEY, Boolean.TRUE );
    }

    @Override
    public boolean isRequestContextMarked( RequestContext ctx )
    {
        return ctx.containsKey( MARKER_KEY );
    }

    @Override
    public boolean isEventAboutWLFile( RepositoryItemEvent evt )
    {
        return evt.getRepository().getRepositoryKind().isFacetAvailable( MavenRepository.class )
            && evt.getItem() instanceof StorageFileItem
            && config.getLocalPrefixFilePath().equals( evt.getItem().getPath() );
    }
}
