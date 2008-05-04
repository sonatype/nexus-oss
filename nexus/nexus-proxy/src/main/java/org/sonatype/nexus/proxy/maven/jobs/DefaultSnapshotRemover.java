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
package org.sonatype.nexus.proxy.maven.jobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.StoreWalker;

/**
 * The Class SnapshotRemoverJob. It has two parameters: minCountOfSnapshotsToKeep and removeSnapshotsOlderThanDays.
 * After a succesful run, the job guarantees that there will remain at least minCountOfSnapshotsToKeep (but maybe more)
 * snapshots per one snapshot collection by removing all older from removeSnapshotsOlderThanDays.
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

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws Exception
    {
        SnapshotRemovalResult result = null;

        if ( request.getRepositoryId() != null )
        {
            getLogger().info( "Removing old SNAPSHOT deployments from " + request.getRepositoryId() + " repository." );

            Repository repository = getRepositoryRegistry().getRepository( request.getRepositoryId() );

            result = removeSnapshotsFromMavenRepository( repository, request );
        }
        else if ( request.getRepositoryGroupId() != null )
        {
            getLogger().info(
                "Removing old SNAPSHOT deployments from " + request.getRepositoryGroupId() + " repository group." );

            result = new SnapshotRemovalResult();

            for ( Repository repository : getRepositoryRegistry().getRepositoryGroup( request.getRepositoryGroupId() ) )
            {
                result.append( removeSnapshotsFromMavenRepository( repository, request ) );
            }

        }
        else
        {
            getLogger().info( "Removing old SNAPSHOT deployments from all repositories." );

            result = new SnapshotRemovalResult();

            for ( Repository repository : getRepositoryRegistry().getRepositories() )
            {
                result.append( removeSnapshotsFromMavenRepository( repository, request ) );
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
    protected SnapshotRemovalResult removeSnapshotsFromMavenRepository( Repository repository,
        SnapshotRemovalRequest request )
        throws Exception
    {
        SnapshotRemovalResult result = new SnapshotRemovalResult( repository.getId(), 0, 0 );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Collecting deletable snapshots on repository " + repository.getId() );
        }

        // create a walker to collect deletables and let it loose on collections only
        SnapshotRemoverWalker walker = new SnapshotRemoverWalker( getLogger(), repository, request );
        walker.start();

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
        private final Logger logger;

        private final Repository repository;

        private final SnapshotRemovalRequest request;

        private final Map<String, List<StorageFileItem>> remainingSnapshotsAndFiles = new HashMap<String, List<StorageFileItem>>();

        private final Map<String, List<StorageFileItem>> deletableSnapshotsAndFiles = new HashMap<String, List<StorageFileItem>>();

        private final long dateTreshold;

        private boolean shouldProcessCollection;

        private int deletedSnapshots = 0;

        private int deletedFiles = 0;

        public SnapshotRemoverWalker( Logger logger, Repository repository, SnapshotRemovalRequest request )
        {
            super();
            this.logger = logger;
            this.repository = repository;
            this.request = request;
            this.dateTreshold = System.currentTimeMillis() - ( request.getRemoveSnapshotsOlderThanDays() * 86400000 );
        }

        protected void addStorageFileItemToMap( Map<String, List<StorageFileItem>> map, String key, StorageFileItem item )
        {
            if ( !map.containsKey( key ) )
            {
                map.put( key, new ArrayList<StorageFileItem>() );
            }
            map.get( key ).add( item );
        }

        protected void onCollectionEnter( ResourceStore store, StorageCollectionItem coll, Logger logger )
        {
            deletableSnapshotsAndFiles.clear();
            remainingSnapshotsAndFiles.clear();
            shouldProcessCollection = coll.getParentPath().contains( "-SNAPSHOT" );
            if ( shouldProcessCollection )
            {
                Gav gav = null;
                Collection<StorageItem> items = coll.list();
                for ( StorageItem item : items )
                {
                    if ( !item.isVirtual() && !StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                    {
                        if ( M2ArtifactRecognizer.isSnapshot( item.getPath() )
                            && !M2ArtifactRecognizer.isMetadata( item.getPath() ) )
                        {
                            gav = M2GavCalculator.calculate( item.getPath() );

                            if ( gav != null )
                            {
                                long itemTimestamp = gav.getSnapshotTimeStamp() != null ? gav
                                    .getSnapshotTimeStamp().longValue() : item.getCreated();

                                if ( itemTimestamp < dateTreshold )
                                {
                                    addStorageFileItemToMap(
                                        deletableSnapshotsAndFiles,
                                        gav.getSnapshotBuildNumber(),
                                        (StorageFileItem) item );
                                }
                                else
                                {
                                    addStorageFileItemToMap(
                                        remainingSnapshotsAndFiles,
                                        gav.getSnapshotBuildNumber(),
                                        (StorageFileItem) item );
                                }
                            }
                        }
                    }
                }
            }
        }

        protected void processItem( ResourceStore store, StorageItem item, Logger logger )
        {
            // nothing here
        }

        protected void onCollectionExit( ResourceStore store, StorageCollectionItem coll, Logger logger )
        {
            if ( shouldProcessCollection )
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
                        TreeSet<String> keys = new TreeSet<String>( deletableSnapshotsAndFiles.keySet() );
                        while ( remainingSnapshotsAndFiles.size() < request.getMinCountOfSnapshotsToKeep() )
                        {
                            remainingSnapshotsAndFiles.put( keys.last(), deletableSnapshotsAndFiles.get( keys.last() ) );
                            deletableSnapshotsAndFiles.remove( keys.last() );
                            keys.remove( keys.last() );
                        }

                    }
                }
                for ( String key : deletableSnapshotsAndFiles.keySet() )
                {
                    List<StorageFileItem> files = deletableSnapshotsAndFiles.get( key );
                    deletedSnapshots++;
                    for ( StorageFileItem file : files )
                    {
                        try
                        {
                            repository.deleteItem( file.getRepositoryItemUid() );
                            deletedFiles++;
                        }
                        catch ( Exception e )
                        {
                            logger.warn( "Could not delete file:", e );
                        }
                    }
                }
            }
        }

        public void start()
        {
            shouldProcessCollection = false;

            walk( repository, logger, true, true );
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
