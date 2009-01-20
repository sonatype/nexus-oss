/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.events;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryItemEvent" )
public class RepositoryItemEventInspector
    extends AbstractFeedRecorderEventInspector
{
    @Requirement
    private IndexerManager indexerManager;

    @Requirement
    private ArtifactContextProducer artifactContextProducer;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryItemEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        inspectForNexus( evt );

        inspectForIndexerManager( evt );
    }

    private void inspectForNexus( AbstractEvent evt )
    {
        RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

        if ( ievt instanceof RepositoryItemEventRetrieve )
        {
            // RETRIEVE event creates a lot of noise in events,
            // so we are not processing those
            return;
        }

        if ( ievt.getItemUid().getPath().endsWith( ".pom" ) || ievt.getItemUid().getPath().endsWith( ".jar" ) )
        {
            // filter out links and dirs/collections
            if ( StorageFileItem.class.isAssignableFrom( ievt.getItem().getClass() ) )
            {
                StorageFileItem pomItem = (StorageFileItem) ievt.getItem();

                NexusArtifactEvent nae = new NexusArtifactEvent();
                NexusItemInfo ai = new NexusItemInfo();
                ai.setRepositoryId( pomItem.getRepositoryId() );
                ai.setPath( pomItem.getPath() );
                ai.setRemoteUrl( pomItem.getRemoteUrl() );
                nae.setNexusItemInfo( ai );
                nae.setEventDate( ievt.getEventDate() );
                nae.setEventContext( ievt.getContext() );

                if ( ievt instanceof RepositoryItemEventCache )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_CACHED );
                }
                else if ( ievt instanceof RepositoryItemEventStore )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_DEPLOYED );
                }
                else if ( ievt instanceof RepositoryItemEventDelete )
                {
                    nae.setAction( NexusArtifactEvent.ACTION_DELETED );
                }
                else
                {
                    return;
                }

                getFeedRecorder().addNexusArtifactEvent( nae );
            }

        }
    }

    private void inspectForIndexerManager( AbstractEvent evt )
    {
        try
        {
            RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

            // sadly, the nexus-indexer is maven2 only, hence we check is the repo
            // from where we get the event is a maven2 repo
            if ( !MavenRepository.class.isAssignableFrom( ievt.getRepository().getClass() ) )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "This is not a MavenRepository instance, will not process it." );
                }

                return;
            }

            // should we sync at all
            if ( ievt.getRepository().isIndexable()
                && ( RepositoryItemEventStore.class.isAssignableFrom( ievt.getClass() )
                    || RepositoryItemEventCache.class.isAssignableFrom( ievt.getClass() ) || RepositoryItemEventDelete.class
                    .isAssignableFrom( ievt.getClass() ) ) )
            {
                IndexingContext context = getIndexerManager().getRepositoryLocalIndexContext(
                    ievt.getRepository().getId() );

                // by calculating GAV we check wether the request is against a repo artifact at all
                Gav gav = ( (MavenRepository) ievt.getRepository() ).getGavCalculator().pathToGav(
                    ievt.getItemUid().getPath() );

                // signatures and hashes are not considered for processing
                // reason (NEXUS-814 related): the actual artifact and it's POM will (or already did)
                // emitted events about modifying them
                if ( context != null && gav != null && !gav.isSignature() && !gav.isHash() )
                {
                    // if we have a valid indexing context and have access to a File
                    if ( DefaultFSLocalRepositoryStorage.class.isAssignableFrom( ievt
                        .getItemUid().getRepository().getLocalStorage().getClass() ) )
                    {
                        File file = ( (DefaultFSLocalRepositoryStorage) ievt.getRepository().getLocalStorage() )
                            .getFileFromBase( ievt.getRepository(), ievt.getItem().getItemContext(), ievt
                                .getItemUid().getPath() );

                        if ( file.exists() )
                        {
                            ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

                            if ( ac != null )
                            {
                                if ( getLogger().isDebugEnabled() )
                                {
                                    getLogger().debug( "The ArtifactContext created from file is fine, continuing." );
                                }

                                ArtifactInfo ai = ac.getArtifactInfo();

                                if ( ievt instanceof RepositoryItemEventCache )
                                {
                                    // add file to index
                                    if ( getLogger().isDebugEnabled() )
                                    {
                                        getLogger().debug(
                                            "Adding artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                                + " to index (CACHE)." );
                                    }

                                    getIndexerManager().getNexusIndexer().addArtifactToIndex( ac, context );
                                }
                                else if ( ievt instanceof RepositoryItemEventStore )
                                {
                                    // add file to index
                                    if ( getLogger().isDebugEnabled() )
                                    {
                                        getLogger().debug(
                                            "Adding artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                                + " to index (STORE)." );
                                    }

                                    getIndexerManager().getNexusIndexer().addArtifactToIndex( ac, context );
                                }
                                else if ( ievt instanceof RepositoryItemEventDelete )
                                {
                                    // NEXUS-814: we should not delete always
                                    if ( !ievt.getItem().getItemContext().containsKey(
                                        SnapshotRemover.MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV ) )
                                    {
                                        // remove file from index
                                        if ( getLogger().isDebugEnabled() )
                                        {
                                            getLogger().debug(
                                                "Deleting artifact " + ai.groupId + ":" + ai.artifactId + ":"
                                                    + ai.version + " from index (DELETE)." );
                                        }

                                        getIndexerManager().getNexusIndexer().deleteArtifactFromIndex( ac, context );
                                    }
                                    else
                                    {
                                        // do NOT remove file from index
                                        if ( getLogger().isDebugEnabled() )
                                        {
                                            getLogger()
                                                .debug(
                                                    "NOT deleting artifact "
                                                        + ai.groupId
                                                        + ":"
                                                        + ai.artifactId
                                                        + ":"
                                                        + ai.version
                                                        + " from index (DELETE), since it is a timestamped snapshot and more builds exists." );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        if ( ievt instanceof RepositoryItemEventDelete )
                        {
                            // NEXUS-814: we should not delete always
                            if ( !ievt.getItem().getItemContext().containsKey(
                                SnapshotRemover.MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV ) )
                            {
                                ArtifactInfo ai = new ArtifactInfo();

                                ai.groupId = gav.getGroupId();

                                ai.artifactId = gav.getArtifactId();

                                ai.version = gav.getVersion();

                                ai.classifier = gav.getClassifier();

                                ArtifactContext ac = new ArtifactContext( null, null, null, ai, gav );

                                // remove file from index
                                if ( getLogger().isDebugEnabled() )
                                {
                                    getLogger().debug(
                                        "Deleting artifact " + ai.groupId + ":" + ai.artifactId + ":" + ai.version
                                            + " from index (DELETE)." );
                                }

                                getIndexerManager().getNexusIndexer().deleteArtifactFromIndex( ac, context );
                            }
                            else
                            {
                                // do NOT remove file from index
                                if ( getLogger().isDebugEnabled() )
                                {
                                    getLogger()
                                        .debug(
                                            "NOT deleting artifact "
                                                + gav.getGroupId()
                                                + ":"
                                                + gav.getArtifactId()
                                                + ":"
                                                + gav.getVersion()
                                                + " from index (DELETE), since it is a timestamped snapshot and more builds exists." );
                                }
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e ) // TODO be more specific
        {
            getLogger().error( "Could not maintain index!", e );
        }

    }

}
