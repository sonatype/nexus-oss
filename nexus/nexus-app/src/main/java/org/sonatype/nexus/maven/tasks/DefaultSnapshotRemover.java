/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.StoreWalker;

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

    private ContentClass contentClass = new Maven2ContentClass();

    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
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

            for ( Repository repository : getRepositoryRegistry().getRepositoryGroup( request.getRepositoryGroupId() ) )
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

        // create a walker to collect deletables and let it loose on collections only
        SnapshotRemoverWalker walker = new SnapshotRemoverWalker( getLogger(), repository, request );

        // start the dance
        walker.start();

        // and collect results
        result.setDeletedSnapshots( walker.getDeletedSnapshots() );
        result.setDeletedFiles( walker.getDeletedFiles() );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Collected and deleted " + walker.getDeletedSnapshots() + " snapshots with alltogether "
                    + walker.getDeletedFiles() + " files on repository " + repository.getId() );
        }

        return result;
    }

    private class SnapshotRemoverWalker
        extends StoreWalker
    {
        private final MavenRepository repository;

        private final SnapshotRemovalRequest request;

        private final Map<ArtifactVersion, List<StorageFileItem>> remainingSnapshotsAndFiles = new HashMap<ArtifactVersion, List<StorageFileItem>>();

        private final Map<ArtifactVersion, List<StorageFileItem>> deletableSnapshotsAndFiles = new HashMap<ArtifactVersion, List<StorageFileItem>>();

        private final long dateThreshold;

        private boolean shouldProcessCollection;

        private boolean removeWholeGAV;

        private Gav gavToRemove;

        private int deletedSnapshots = 0;

        private int deletedFiles = 0;

        public SnapshotRemoverWalker( Logger logger, MavenRepository repository, SnapshotRemovalRequest request )
        {
            super( repository, logger );

            this.repository = repository;

            this.request = request;

            int days = request.getRemoveSnapshotsOlderThanDays();

            if ( days > 0 )
            {
                this.dateThreshold = System.currentTimeMillis() - ( (long) days * 86400000L );
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

        protected void processItem( StorageItem item )
        {
            // nothing here
        }

        protected void onCollectionExit( StorageCollectionItem coll )
        {
            shouldProcessCollection = coll.getPath().endsWith( "-SNAPSHOT" );

            if ( shouldProcessCollection )
            {
                deletableSnapshotsAndFiles.clear();

                remainingSnapshotsAndFiles.clear();

                removeWholeGAV = false;

                gavToRemove = null;

                Gav gav = null;

                Collection<StorageItem> items;

                try
                {
                    items = repository.list( coll );
                }
                catch ( Exception e )
                {
                    // stop the crawling
                    stop( e );

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
                                if ( request.isRemoveIfReleaseExists() && releaseExistsForSnapshot( gav ) )
                                {
                                    getLogger().debug( "Found POM and release exists, removing whole gav." );

                                    removeWholeGAV = true;

                                    gavToRemove = gav;

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
                        ArtifactStoreRequest req = new ArtifactStoreRequest( gavToRemove.getGroupId(), gavToRemove
                            .getArtifactId(), gavToRemove.getVersion() );

                        // remove the whole GAV
                        repository.deleteArtifactPom( req, true, true, true );
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
                            return;
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

                    for ( ArtifactVersion key : deletableSnapshotsAndFiles.keySet() )
                    {

                        List<StorageFileItem> files = deletableSnapshotsAndFiles.get( key );
                        deletedSnapshots++;

                        for ( StorageFileItem file : files )
                        {
                            try
                            {
                                gav = (Gav) file.getItemContext().get( Gav.class.getName() );

                                // If hash or signature, just junk it
                                if ( gav.isHash() || gav.isSignature() )
                                {
                                    repository.deleteItem( file.getRepositoryItemUid(), file.getItemContext() );
                                }
                                // Otherwise, go through proper channels to remove.
                                else
                                {
                                    ArtifactStoreRequest req = new ArtifactStoreRequest( gav.getGroupId(), gav
                                        .getArtifactId(), gav.getVersion(), gav.getExtension(), gav.getClassifier() );

                                    if ( "pom".equals( gav.getExtension() ) )
                                    {
                                        repository.deleteArtifactPom( req, false, false, false );
                                    }
                                    else
                                    {
                                        repository.deleteArtifact( req, false, false, false );
                                    }
                                }

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
                    repository.deleteItem( coll.getRepositoryItemUid(), coll.getItemContext() );
                }
            }
            catch ( ItemNotFoundException e )
            {
                // silent, this happens if whole GAV is removed and the dir is removed too
            }
            catch ( Throwable t )
            {
                stop( t );

                return;
            }
        }

        public boolean releaseExistsForSnapshot( Gav snapshotGav )
        {
            // get a GAV request
            ArtifactStoreRequest req = new ArtifactStoreRequest(
                snapshotGav.getGroupId(),
                snapshotGav.getArtifactId(),
                snapshotGav
                    .getBaseVersion().substring( 0, snapshotGav.getBaseVersion().length() - "-SNAPSHOT".length() ) );

            // do not proxy, look for local content only
            req.setRequestLocalOnly( true );

            for ( Repository repository : repositoryRegistry.getRepositories() )
            {
                if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
                {
                    MavenRepository mrepository = (MavenRepository) repository;

                    // look in release reposes only
                    if ( RepositoryPolicy.RELEASE.equals( mrepository.getRepositoryPolicy() ) )
                    {
                        try
                        {
                            mrepository.retrieveArtifactPom( req );

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

        public void start()
        {
            shouldProcessCollection = false;

            walk( true, true );
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
