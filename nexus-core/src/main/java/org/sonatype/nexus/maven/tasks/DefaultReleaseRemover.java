package org.sonatype.nexus.maven.tasks;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.maven.tasks.descriptors.ReleaseRemovalTaskDescriptor.ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.maven.tasks.descriptors.ReleaseRemovalTaskDescriptor;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.version.GenericVersionParser;
import org.sonatype.nexus.proxy.maven.version.InvalidVersionSpecificationException;
import org.sonatype.nexus.proxy.maven.version.Version;
import org.sonatype.nexus.proxy.maven.version.VersionParser;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.DottedStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.wastebasket.DeleteOperation;
import org.sonatype.scheduling.TaskUtil;
import sun.plugin.dom.exception.InvalidStateException;

/**
 * @since 2.5
 */
@Named
@Singleton
public class DefaultReleaseRemover
    extends AbstractLoggingComponent
    implements ReleaseRemover
{

    private RepositoryRegistry repositoryRegistry;

    private Walker walker;

    private ContentClass maven2ContentClass;

    private VersionParser versionScheme = new GenericVersionParser();

    @Inject
    public DefaultReleaseRemover( final RepositoryRegistry repositoryRegistry, final Walker walker,
                                  final @Named( "maven2" ) ContentClass maven2ContentClass )
    {
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.walker = checkNotNull( walker );
        this.maven2ContentClass = checkNotNull( maven2ContentClass );
    }

    @Override
    public ReleaseRemovalResult removeReleases( final ReleaseRemovalRequest request )
        throws NoSuchRepositoryException
    {
        getLogger().debug( "Removing releases from repository: {}", request.getRepositoryId() );
        ReleaseRemovalResult result = new ReleaseRemovalResult( request.getRepositoryId() );

        Repository repository = repositoryRegistry.getRepository( request.getRepositoryId() );
        if ( !process( request, result, repository ) )
        {
            throw new IllegalArgumentException( "The repository with ID=" + repository.getId() + " is not valid for "
                                                    + ID );
        }
        return result;
    }

    private boolean process( final ReleaseRemovalRequest request, final ReleaseRemovalResult result,
                             final Repository repository )
    {
        if ( !repository.getRepositoryContentClass().isCompatible( maven2ContentClass ) )
        {
            getLogger().debug( "Skipping '{}' is not a maven 2 repository", repository.getId() );
            return false;
        }

        if ( !repository.getLocalStatus().shouldServiceRequest() )
        {
            getLogger().debug( "Skipping '{}' because the repository is out of service", repository.getId() );
            return false;
        }

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            getLogger().debug( "Skipping '{}' because it is a proxy repository", repository.getId() );
            return false;
        }

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            getLogger().debug( "Skipping '{}' because it is a group repository", repository.getId() );
            return false;
        }

        MavenRepository mavenRepository = repository.adaptToFacet( MavenRepository.class );

        if ( RepositoryPolicy.SNAPSHOT.equals( mavenRepository.getRepositoryPolicy() ) )
        {
            getLogger().debug( "Skipping '{}' because it is a snapshot repository", repository.getId() );
            return false;
        }

        result.setResult( removeReleasesFromMavenRepository( mavenRepository, request ) );
        return true;
    }

    private ReleaseRemovalResult removeReleasesFromMavenRepository( final MavenRepository repository,
                                                                    final ReleaseRemovalRequest request )
    {
        TaskUtil.checkInterruption();
        ReleaseRemovalResult result = new ReleaseRemovalResult( repository.getId() );

        if ( !repository.getLocalStatus().shouldServiceRequest() )
        {
            return result;
        }

        getLogger().debug(
            "Collecting deletable releases on repository " + repository.getId() + " from storage directory "
                + repository.getLocalUrl() );

        DefaultWalkerContext ctxMain =
            new DefaultWalkerContext( repository, new ResourceStoreRequest( "/" ), new DottedStoreWalkerFilter() );

        ctxMain.getContext().put( DeleteOperation.DELETE_OPERATION_CTX_KEY, DeleteOperation.MOVE_TO_TRASH );

        ctxMain.getProcessors().add( new ReleaseRemovalWalkerProcessor( repository, request ) );

        walker.walk( ctxMain );

        if ( ctxMain.getStopCause() != null )
        {
            result.setSuccessful( false );
        }
        return result;
    }

    private class ReleaseRemovalWalkerProcessor
        extends AbstractWalkerProcessor
    {

        private static final String POSSIBLY_EMPTY_COLLECTIONS = "possiblyEmptyCollections";

        private final MavenRepository repository;

        private final ReleaseRemovalRequest request;

        private final Map<Version, List<StorageFileItem>> deletableVersionsAndFiles =
            new HashMap<Version, List<StorageFileItem>>();

        private final Map<Gav, Map<Version, List<StorageFileItem>>> gas =
            new HashMap<Gav, Map<Version, List<StorageFileItem>>>();

        private int deletedFiles = 0;

        private ReleaseRemovalWalkerProcessor( final MavenRepository repository,
                                               final ReleaseRemovalRequest request )
        {
            this.repository = repository;
            this.request = request;
        }

        @Override
        public void processItem( final WalkerContext context, final StorageItem item )
            throws Exception
        {
        }

        @Override
        public void onCollectionExit( final WalkerContext context, final StorageCollectionItem coll )
            throws Exception
        {
            try
            {
                doOnCollectionExit( context, coll );
            }
            catch ( Exception e )
            {
                // we always simply log the exception and continue
                getLogger().warn( "{} failed to process path: '{}'.", ID, coll.getPath(), e );
            }
        }

        private void doOnCollectionExit( final WalkerContext context, final StorageCollectionItem coll )
            throws ItemNotFoundException, StorageException, IllegalOperationException,
            InvalidVersionSpecificationException
        {
            deletableVersionsAndFiles.clear();

            Collection<StorageItem> items = repository.list( false, coll );
            Gav gav = null;
            for ( StorageItem item : items )
            {
                if ( !item.isVirtual() && !StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    gav =
                        ( (MavenRepository) coll.getRepositoryItemUid().getRepository() ).getGavCalculator().pathToGav(
                            item.getPath() );
                    if ( gav != null )
                    {
                        addCollectionToContext( context, coll );
                        addStorageFileItemToMap( deletableVersionsAndFiles, gav, (StorageFileItem) item );
                    }
                }
            }
            if ( null != gav )
            {
                getLogger().debug( "Adding these files to consider for deletion: {}",
                                   deletableVersionsAndFiles.toString() );
                addVersionsToGas( gas, deletableVersionsAndFiles, gav );
            }
        }

        /**
         * Store visited collections so we can later determine if we need to delete them.
         */
        private void addCollectionToContext( final WalkerContext context, final StorageCollectionItem coll )
        {
            if ( !context.getContext().containsKey( POSSIBLY_EMPTY_COLLECTIONS ) )
            {
                context.getContext().put( POSSIBLY_EMPTY_COLLECTIONS, Lists.<StorageCollectionItem>newArrayList() );
            }
            ( (List<StorageCollectionItem>) context.getContext().get( POSSIBLY_EMPTY_COLLECTIONS ) ).add( coll );
        }

        /**
         * Map Group + Artifact to each version with those GA coordinates
         */
        private void addVersionsToGas( final Map<Gav, Map<Version, List<StorageFileItem>>> gas,
                                       final Map<Version, List<StorageFileItem>> versionsAndFiles,
                                       final Gav gav )
        {
            //ga only coordinates
            Gav ga = new Gav( gav.getGroupId(), gav.getArtifactId(), "" );
            if ( !gas.containsKey( ga ) )
            {
                gas.put( ga, Maps.newHashMap( versionsAndFiles ) );
            }
            gas.get( ga ).putAll( versionsAndFiles );
        }

        protected void addStorageFileItemToMap( Map<Version, List<StorageFileItem>> map, Gav gav, StorageFileItem item )
        {
            Version key = null;
            try
            {
                key = versionScheme.parseVersion( gav.getVersion() );
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new InvalidStateException( "Unable to determine version for " + gav.getVersion() +
                                                     ", cannot proceed with deletion of releases unless"
                                                     + "all version information can be parsed into major.minor.incremental version." );
            }

            if ( !map.containsKey( key ) )
            {
                map.put( key, new ArrayList<StorageFileItem>() );
            }

            map.get( key ).add( item );
        }

        @Override
        public void afterWalk( final WalkerContext context )
            throws Exception
        {
            getLogger().debug( "Processing and possibly deleting release versions" );
            for ( Map.Entry<Gav, Map<Version, List<StorageFileItem>>> gavListEntry : gas.entrySet() )
            {
                Map<Version, List<StorageFileItem>> versions = gavListEntry.getValue();
                if ( versions.size() > request.getNumberOfVersionsToKeep() )
                {
                    getLogger().debug( "{} will delete {} versions of artifact with g={} a ={}",
                                       ReleaseRemovalTaskDescriptor.ID,
                                       versions.size() - request.getNumberOfVersionsToKeep(),
                                       gavListEntry.getKey().getGroupId(), gavListEntry.getKey().getArtifactId() );

                    List<Version> sortedVersions = Lists.newArrayList( versions.keySet() );
                    Collections.sort( sortedVersions );
                    List<Version> toDelete =
                        sortedVersions.subList( 0, versions.size() - request.getNumberOfVersionsToKeep() );
                    getLogger().debug( "Will delete these versions: {}", toDelete );
                    for ( Version version : toDelete )
                    {
                        for ( StorageFileItem storageFileItem : versions.get( version ) )
                        {
                            repository.deleteItem( createResourceStoreRequest( storageFileItem, context ) );
                            deletedFiles++;
                        }
                    }
                    if ( context.getContext().containsKey( POSSIBLY_EMPTY_COLLECTIONS ) )
                    {
                        for ( StorageCollectionItem coll : (List<StorageCollectionItem>) context.getContext().get(
                            POSSIBLY_EMPTY_COLLECTIONS ) )
                        {
                            removeDirectoryIfEmpty( coll );
                        }
                    }
                }
            }
        }

        //TODO - KR reuse with DefaultSnapshotRemover
        private ResourceStoreRequest createResourceStoreRequest( final StorageItem item, final WalkerContext ctx )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );

            if ( ctx.getContext().containsKey( DeleteOperation.DELETE_OPERATION_CTX_KEY ) )
            {
                request.getRequestContext().put( DeleteOperation.DELETE_OPERATION_CTX_KEY,
                                                 ctx.getContext().get( DeleteOperation.DELETE_OPERATION_CTX_KEY ) );
            }

            return request;
        }

        //TODO - KR reuse with DefaultSnapshotRemover
        private ResourceStoreRequest createResourceStoreRequest( final StorageCollectionItem item,
                                                                 final DeleteOperation operation )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );
            request.getRequestContext().put( DeleteOperation.DELETE_OPERATION_CTX_KEY, operation );
            return request;
        }

        //TODO - KR reuse with DefaultSnapshotRemover, need to handle repository variable
        private void removeDirectoryIfEmpty( StorageCollectionItem coll )
            throws StorageException, IllegalOperationException, UnsupportedStorageOperationException
        {
            try
            {
                if ( repository.list( false, coll ).size() > 0 )
                {
                    return;
                }

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Removing the empty directory leftover: UID=" + coll.getRepositoryItemUid().toString() );
                }

                // directory is empty, never move to trash
                repository.deleteItem( false, createResourceStoreRequest( coll, DeleteOperation.DELETE_PERMANENTLY ) );
            }
            catch ( ItemNotFoundException e )
            {
                // silent, this happens if whole GAV is removed and the dir is removed too
            }
        }
    }
}
