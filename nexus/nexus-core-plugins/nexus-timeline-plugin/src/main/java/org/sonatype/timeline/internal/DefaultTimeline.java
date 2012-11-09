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
package org.sonatype.timeline.internal;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineCallback;
import org.sonatype.timeline.TimelineConfiguration;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineRecord;

@Singleton
@Named( "default" )
public class DefaultTimeline
    implements Timeline
{

    private final Logger logger;

    private TimelineConfiguration configuration;

    private volatile boolean started;

    private final DefaultTimelinePersistor persistor;

    private final DefaultTimelineIndexer indexer;

    private final ReentrantReadWriteLock timelineLock;

    public DefaultTimeline()
    {
        this.logger = LoggerFactory.getLogger( getClass() );
        this.started = false;
        this.persistor = new DefaultTimelinePersistor();
        this.indexer = new DefaultTimelineIndexer();
        this.timelineLock = new ReentrantReadWriteLock();
    }

    protected Logger getLogger()
    {
        return logger;
    }

    /**
     * Visible for UT
     */
    protected DefaultTimelineIndexer getIndexer()
    {
        return indexer;
    }

    /**
     * Visible for UT
     */
    protected DefaultTimelinePersistor getPersistor()
    {
        return persistor;
    }

    // ==
    // Public API

    public void start( final TimelineConfiguration configuration )
        throws IOException
    {
        getLogger().debug( "Starting Timeline..." );
        doExclusively( new Work<Void>()
        {
            @Override
            public Void doIt()
                throws IOException
            {
                if ( !started )
                {
                    DefaultTimeline.this.configuration = configuration;
                    // if persistor fails, it's a total failure, we
                    // cannot work without persistor
                    persistor.start( configuration );

                    // indexer start, that might need repair
                    // and might end up in falied repair
                    try
                    {
                        indexer.start( configuration );
                    }
                    catch ( IOException e )
                    {
                        // we are staring, so repair must be tried at least once
                        // so we are passing the actual generation, and we are within exclusive lock
                        repairTimelineIndexer( e, indexer.getGeneration() );
                    }
                    DefaultTimeline.this.started = true;
                    getLogger().info( "Started Timeline..." );
                }
                return null;
            }
        } );
    }

    public void stop()
        throws IOException
    {
        getLogger().debug( "Stopping Timeline..." );
        doExclusively( new Work<Void>()
        {
            @Override
            public Void doIt()
                throws IOException
            {
                if ( started )
                {
                    DefaultTimeline.this.started = false;
                    indexer.stop();
                    getLogger().info( "Stopped Timeline..." );
                }
                return null;
            }
        } );
    }

    public boolean isStarted()
    {
        return started;
    }

    @Override
    public void add( final TimelineRecord... records )
    {
        if ( !started )
        {
            return;
        }
        try
        {
            persistor.persist( records );
            addToIndexer( records );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Failed to add a timeline record", e );
        }
    }

    @Override
    public int purge( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        if ( !started )
        {
            return 0;
        }
        return purgeFromIndexer( timestamp, types, subTypes, filter );
    }

    @Override
    public void retrieve( int fromItem, int count, Set<String> types, Set<String> subTypes, TimelineFilter
        filter, TimelineCallback callback )
    {
        if ( !started )
        {
            return;
        }
        retrieveFromIndexer( 0L, System.currentTimeMillis(), fromItem, count, types, subTypes, filter, callback );
    }

    @Override
    public void retrieve( long fromTime, long toTime, int from, int count, Set<String> types, Set<String> subTypes,
        TimelineFilter filter, TimelineCallback callback )
    {
        if ( !started )
        {
            return;
        }
        retrieveFromIndexer( fromTime, toTime, from, count, types, subTypes, filter, callback );
    }

    // ==

    protected void addToIndexer( final TimelineRecord... records )
    {
        doTryRepair( new Work<Void>()
        {
            @Override
            public Void doIt()
                throws IOException
            {
                indexer.addAll( records );
                return null;
            }
        } );
    }

    protected int purgeFromIndexer( final long timestamp, final Set<String> types, final Set<String> subTypes,
        final TimelineFilter filter )
    {
        return doTryRepair( new Work<Integer>()
        {
            @Override
            public Integer doIt()
                throws IOException
            {
                return indexer.purge( 0l, timestamp, types, subTypes );
            }
        } );
    }

    protected void retrieveFromIndexer( final long fromTime, final long toTime, final int from, final int count,
        final Set<String> types, final Set<String> subTypes, final TimelineFilter filter,
        final TimelineCallback callback )
    {
        doTryRepair( new Work<Void>()
        {
            @Override
            public Void doIt()
                throws IOException
            {
                indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter, callback );
                return null;
            }
        } );
    }

// ==

    protected static interface Work<E>
    {

        E doIt()
            throws IOException;

    }

    protected <E> E doTryRepair( final Work<E> work )
    {
        if ( timelineLock.readLock().tryLock() )
        {
            if ( started && !indexerIsDead )
            {
                final int indexerGeneration = indexer.getGeneration();
                try
                {
                    return work.doIt();
                }
                catch ( final IOException e )
                {
                    getLogger().debug( "Unable to operate against Timeline indexer!", e );
                    try
                    {
                        timelineLock.readLock().unlock();
                        try
                        {
                            doExclusively( new Work<Void>()
                            {
                                @Override
                                public Void doIt()
                                    throws IOException
                                {
                                    repairTimelineIndexer( e, indexerGeneration );
                                    return null;
                                }
                            } );
                        }
                        finally
                        {
                            timelineLock.readLock().lock();
                        }
                        return work.doIt();
                    }
                    catch ( final IOException ee )
                    {
                        getLogger().warn( "Unable to operate against Timeline indexer after repair!", e );
                    }
                }
                finally
                {
                    timelineLock.readLock().unlock();
                }
            }
        }
        return null;
    }

    protected <E> E doExclusively( final Work<E> work )
        throws IOException
    {
        timelineLock.writeLock().lock();
        try
        {
            return work.doIt();
        }
        finally
        {
            timelineLock.writeLock().unlock();
        }
    }

    // ==

    private volatile boolean indexerIsDead = false;

    protected void repairTimelineIndexer( final Exception e, final int generation )
        throws IOException
    {
        // check for any previous attempt or flagged dead indexer
        if ( !indexerIsDead && ( indexer.getGeneration() == generation ) )
        {
            getLogger().info( "Timeline index got corrupted, trying to repair it.", e );
            // stopping it cleanly
            indexer.stop();
            // deleting index files
            FileUtils.cleanDirectory( configuration.getIndexDirectory() );
            try
            {
                // creating new index from scratch
                indexer.start( configuration );
                // pouring over records from persisted into indexer
                final RepairBatch rb = new RepairBatch( indexer );
                persistor.readAllSinceDays( configuration.getRepairDaysCountRestored(), rb );
                rb.finish();

                getLogger().info(
                    "Timeline index is succesfully repaired, the last "
                        + configuration.getRepairDaysCountRestored() + " days were restored." );
            }
            catch ( IOException ex )
            {
                getLogger().warn( "Timeline index was corrupted and repair of it failed!", e );
                // we need to stop it and signal to not try any other thread
                indexerIsDead = true;
                indexer.stop();
                throw ex;
            }
            catch ( Exception ex )
            {
                getLogger().warn( "Timeline index was corrupted and repair of it failed!", e );
                // we need to stop it and signal to not try any other thread
                indexerIsDead = true;
                indexer.stop();
                throw new IOException( "Failed to repair indexer!", ex );
            }
        }
    }
}
