/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.maven.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalkerProcessor;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.DottedStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;

/**
 * The Class SnapshotRemoverJob. After a succesful run, the job guarantees that there will remain at least
 * minCountOfSnapshotsToKeep (but maybe more) snapshots per one snapshot collection by removing all older from
 * removeSnapshotsOlderThanDays. If should remove snaps if their release counterpart exists, the whole GAV will be
 * removed.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultSnapshotRemover
    extends AbstractLogEnabled
    implements SnapshotRemover
{
    /**
     * The registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @plexus.requirement
     */
    private Walker walker;

    private ContentClass contentClass = new Maven2ContentClass();

    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            IllegalArgumentException
    {
        SnapshotRemovalResult result = new SnapshotRemovalResult();

        if ( request.getRepositoryId() != null )
        {
            getLogger().info( "Removing old SNAPSHOT deployments from " + request.getRepositoryId() + " repository." );

            Repository repository = getRepositoryRegistry().getRepository( request.getRepositoryId() );

            if ( MavenRepository.class.isAssignableFrom( repository.getClass() )
                && repository.getRepositoryContentClass().isCompatible( contentClass ) )
            {
                result.addResult( removeSnapshotsFromMavenRepository( (MavenRepository) repository, request ) );
            }
            else
            {
                throw new IllegalArgumentException( "The repository with ID=" + repository.getId()
                    + " is not MavenRepository!" );
            }
        }
        else if ( request.getRepositoryGroupId() != null )
        {
            getLogger().info(
                "Removing old SNAPSHOT deployments from " + request.getRepositoryGroupId() + " repository group." );

            for ( Repository repository : getRepositoryRegistry().getRepositoryWithFacet(
                request.getRepositoryGroupId(),
                GroupRepository.class ).getMemberRepositories() )
            {
                // only from maven repositories, stay silent for others and simply skip
                if ( MavenRepository.class.isAssignableFrom( repository.getClass() )
                    && repository.getRepositoryContentClass().isCompatible( contentClass ) )
                {
                    result.addResult( removeSnapshotsFromMavenRepository( (MavenRepository) repository, request ) );
                }
            }
        }
        else
        {
            getLogger().info( "Removing old SNAPSHOT deployments from all repositories." );

            for ( Repository repository : getRepositoryRegistry().getRepositories() )
            {
                // only from maven repositories, stay silent for others and simply skip
                if ( MavenRepository.class.isAssignableFrom( repository.getClass() )
                    && repository.getRepositoryContentClass().isCompatible( contentClass ) )
                {
                    result.addResult( removeSnapshotsFromMavenRepository( (MavenRepository) repository, request ) );
                }
            }
        }

        return result;
    }

    /**
     * Removes the snapshots from maven repository.
     * 
     * @param repository the repository
     * @throws Exception the exception
     */
    protected SnapshotRemovalRepositoryResult removeSnapshotsFromMavenRepository( MavenRepository repository,
        SnapshotRemovalRequest request )
    {
        SnapshotRemovalRepositoryResult result = new SnapshotRemovalRepositoryResult( repository.getId(), 0, 0 );

        // if this is not snap repo, do nothing
        if ( !RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ) )
        {
            return result;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Collecting deletable snapshots on repository " + repository.getId() + " from storage directory "
                    + repository.getLocalUrl() );
        }

        // and "sandwich" it with RecreateMavenMetadataWalkerProcessor at once
        RecreateMavenMetadataWalkerProcessor recreateMavenMetadataWalker = new RecreateMavenMetadataWalkerProcessor();

        // create a walker to collect deletables and let it loose on collections only
        SnapshotRemoverWalkerProcessor snapshotRemoverWalker = new SnapshotRemoverWalkerProcessor(
            repository,
            request,
            recreateMavenMetadataWalker );

        DefaultWalkerContext ctx = new DefaultWalkerContext( repository, new DottedStoreWalkerFilter() );

        ctx.getProcessors().add( snapshotRemoverWalker );

        walker.walk( ctx );

        // and collect results
        result.setDeletedSnapshots( snapshotRemoverWalker.getDeletedSnapshots() );
        result.setDeletedFiles( snapshotRemoverWalker.getDeletedFiles() );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Collected and deleted " + snapshotRemoverWalker.getDeletedSnapshots() + " snapshots with alltogether "
                    + snapshotRemoverWalker.getDeletedFiles() + " files on repository " + repository.getId() );
        }

        return result;
    }

    private class SnapshotRemoverWalkerProcessor
        extends AbstractWalkerProcessor
    {
        private final RecreateMavenMetadataWalkerProcessor recreateMavenMetadataWalker;

        private final MavenRepository repository;

        private final SnapshotRemovalRequest request;

        private final Map<ArtifactVersion, List<StorageFileItem>> remainingSnapshotsAndFiles = new HashMap<ArtifactVersion, List<StorageFileItem>>();

        private final Map<ArtifactVersion, List<StorageFileItem>> deletableSnapshotsAndFiles = new HashMap<ArtifactVersion, List<StorageFileItem>>();

        private final long dateThreshold;

        private boolean shouldProcessCollection;

        private boolean removeWholeGAV;

        private int deletedSnapshots = 0;

        private int deletedFiles = 0;

        public SnapshotRemoverWalkerProcessor( MavenRepository repository, SnapshotRemovalRequest request,
            RecreateMavenMetadataWalkerProcessor recreateMavenMetadataWalker )
        {
            this.repository = repository;

            this.request = request;

            this.recreateMavenMetadataWalker = recreateMavenMetadataWalker;

            int days = request.getRemoveSnapshotsOlderThanDays();

            if ( days > 0 )
            {
                this.dateThreshold = System.currentTimeMillis() - ( days * 86400000L );
            }
            else
            {
                this.dateThreshold = -1;
            }
        }

        protected void addStorageFileItemToMap( Map<ArtifactVersion, List<StorageFileItem>> map, Gav gav,
            StorageFileItem item )
        {
            ArtifactVersion key = new DefaultArtifactVersion( gav.getVersion() );

            if ( !map.containsKey( key ) )
            {
                map.put( key, new ArrayList<StorageFileItem>() );
            }

            map.get( key ).add( item );
        }

        public void beforeWalk( WalkerContext context )
            throws Exception
        {
            recreateMavenMetadataWalker.beforeWalk( context );
        }

        @Override
        public void processItem( WalkerContext context, StorageItem item )
        {
            // nothing here
        }

        @Override
        public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "onCollectionExit() :: " + coll.getRepositoryItemUid().toString() );
            }

            shouldProcessCollection = coll.getPath().endsWith( "-SNAPSHOT" );

            if ( shouldProcessCollection )
            {
                deletableSnapshotsAndFiles.clear();

                remainingSnapshotsAndFiles.clear();

                removeWholeGAV = false;

                Gav gav = null;

                Collection<StorageItem> items;

                try
                {
                    items = repository.list( coll );
                }
                catch ( Exception e )
                {
                    // stop the crawling
                    context.stop( e );

                    return;
                }

                HashSet<Long> versionsToRemove = new HashSet<Long>();

                // gathering the facts
                for ( StorageItem item : items )
                {
                    if ( !item.isVirtual() && !StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                    {
                        gav = ( (MavenRepository) coll.getRepositoryItemUid().getRepository() )
                            .getGavCalculator().pathToGav( item.getPath() );

                        if ( gav != null )
                        {
                            // if we find a pom, check for delete on release
                            if ( !gav.isHash() && !gav.isSignature() && gav.getExtension().equals( "pom" ) )
                            {
                                if ( request.isRemoveIfReleaseExists()
                                    && releaseExistsForSnapshot( gav, item.getItemContext() ) )
                                {
                                    getLogger().debug( "Found POM and release exists, removing whole gav." );

                                    removeWholeGAV = true;

                                    // Will break out and junk whole gav
                                    break;
                                }
                            }

                            item.getItemContext().put( Gav.class.getName(), gav );

                            long itemTimestamp = System.currentTimeMillis();

                            getLogger().debug( "NOW is " + itemTimestamp );

                            if ( gav.getSnapshotTimeStamp() != null )
                            {
                                getLogger().debug( "Using GAV snapshot timestamp" );

                                itemTimestamp = gav.getSnapshotTimeStamp().longValue();
                            }
                            else
                            {
                                getLogger().debug( "GAV Snapshot timestamp not available, using item.getCreated()" );

                                itemTimestamp = item.getCreated();
                            }

                            // If this timestamp is already marked to be removed, junk it
                            if ( versionsToRemove.contains( new Long( itemTimestamp ) ) )
                            {
                                addStorageFileItemToMap( deletableSnapshotsAndFiles, gav, (StorageFileItem) item );
                            }
                            else
                            {
                                getLogger()
                                    .debug( "itemTimestamp=" + itemTimestamp + ", dateTreshold=" + dateThreshold );

                                // if dateTreshold is not used (zero days) OR
                                // if itemTimestamp is less then dateTreshold (NB: both are positive!)
                                // below will the retentionCount overrule if needed this
                                if ( -1 == dateThreshold || itemTimestamp < dateThreshold )
                                {
                                    versionsToRemove.add( new Long( itemTimestamp ) );
                                    addStorageFileItemToMap( deletableSnapshotsAndFiles, gav, (StorageFileItem) item );
                                }
                                else
                                {
                                    addStorageFileItemToMap( remainingSnapshotsAndFiles, gav, (StorageFileItem) item );
                                }
                            }
                        }
                    }
                }

                // and doing the work here
                if ( removeWholeGAV )
                {
                    try
                    {
                        for ( StorageItem item : items )
                        {
                            // preserve possible subdirs
                            if ( !( item instanceof StorageCollectionItem ) )
                            {
                                repository.deleteItem( item.getRepositoryItemUid(), item.getItemContext() );
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        getLogger().warn( "Could not delete whole GAV " + coll.getRepositoryItemUid().toString(), e );
                    }
                }
                else
                {
                    // and now check some things
                    if ( remainingSnapshotsAndFiles.size() < request.getMinCountOfSnapshotsToKeep() )
                    {
                        // do something
                        if ( remainingSnapshotsAndFiles.size() + deletableSnapshotsAndFiles.size() < request
                            .getMinCountOfSnapshotsToKeep() )
                        {
                            // delete nothing, since there is less snapshots in total as allowed
                            deletableSnapshotsAndFiles.clear();
                        }
                        else
                        {
                            TreeSet<ArtifactVersion> keys = new TreeSet<ArtifactVersion>( deletableSnapshotsAndFiles
                                .keySet() );

                            while ( remainingSnapshotsAndFiles.size() < request.getMinCountOfSnapshotsToKeep() )
                            {
                                remainingSnapshotsAndFiles.put( keys.last(), deletableSnapshotsAndFiles.get( keys
                                    .last() ) );

                                deletableSnapshotsAndFiles.remove( keys.last() );

                                keys.remove( keys.last() );
                            }

                        }
                    }

                    // NEXUS-814: is this GAV have remaining artifacts?
                    boolean gavHasMoreTimestampedSnapshots = remainingSnapshotsAndFiles.size() > 0;

                    for ( ArtifactVersion key : deletableSnapshotsAndFiles.keySet() )
                    {

                        List<StorageFileItem> files = deletableSnapshotsAndFiles.get( key );
                        deletedSnapshots++;

                        for ( StorageFileItem file : files )
                        {
                            try
                            {
                                // NEXUS-814: mark that we are deleting a TS snapshot, but there are still remaining
                                // ones in repository.
                                if ( gavHasMoreTimestampedSnapshots )
                                {
                                    file.getItemContext().put( MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV, Boolean.TRUE );
                                }

                                gav = (Gav) file.getItemContext().get( Gav.class.getName() );

                                repository.deleteItem( file.getRepositoryItemUid(), file.getItemContext() );

                                deletedFiles++;
                            }
                            catch ( Exception e )
                            {
                                getLogger().warn( "Could not delete file:", e );
                            }
                        }
                    }
                }
            }

            // remove empty dirs
            try
            {
                if ( repository.list( coll ).size() == 0 )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "Removing the empty directory leftover: UID=" + coll.getRepositoryItemUid().toString() );
                    }

                    repository.deleteItem( coll.getRepositoryItemUid(), coll.getItemContext() );
                }
                else
                {
                    // activate next processor
                    recreateMavenMetadataWalker.onCollectionExit( context, coll );
                }
            }
            catch ( ItemNotFoundException e )
            {
                // silent, this happens if whole GAV is removed and the dir is removed too
            }
            catch ( Throwable t )
            {
                context.stop( t );

                return;
            }
        }

        public boolean releaseExistsForSnapshot( Gav snapshotGav, Map<String, Object> context )
        {
            for ( Repository repository : repositoryRegistry.getRepositories() )
            {
                // we need to filter for:
                // repository that is MavenRepository and is hosted or proxy
                // repository that has release policy
                if ( repository.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class )
                    || repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
                {
                    // actually, we don't care is it proxy or hosted, we only need to filter out groups and other
                    // "composite" reposes like shadows
                    MavenRepository mrepository = repository.adaptToFacet( MavenRepository.class );

                    // look in release reposes only
                    if ( RepositoryPolicy.RELEASE.equals( mrepository.getRepositoryPolicy() ) )
                    {
                        try
                        {
                            String releaseVersion = snapshotGav.getBaseVersion().substring(
                                0,
                                snapshotGav.getBaseVersion().length() - "-SNAPSHOT".length() );

                            Gav releaseGav = new Gav(
                                snapshotGav.getGroupId(),
                                snapshotGav.getArtifactId(),
                                releaseVersion,
                                snapshotGav.getClassifier(),
                                snapshotGav.getExtension(),
                                null,
                                null,
                                null,
                                false,  // snapshot
                                false,
                                null,
                                false,
                                null );

                            String path = mrepository.getGavCalculator().gavToPath( releaseGav );

                            RepositoryItemUid uid = mrepository.createUid( path );

                            mrepository.retrieveItem( uid, context );

                            return true;
                        }
                        catch ( ItemNotFoundException e )
                        {
                            // nothing
                        }
                        catch ( Exception e )
                        {
                            // nothing
                        }
                    }
                }
            }

            return false;
        }

        public int getDeletedSnapshots()
        {
            return deletedSnapshots;
        }

        public int getDeletedFiles()
        {
            return deletedFiles;
        }

    }

}
