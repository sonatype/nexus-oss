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
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.util.CompositeException;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineCallback;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineRecord;

@Singleton
@Named( "default" )
public class DefaultTimeline
    extends AbstractLockingStartable
    implements Timeline
{
    /**
     * A safeguard, for maximum retries.
     */
    private static final int MAX_RETRIES = 1;

    private final DefaultTimelinePersistor persistor;

    private final DefaultTimelineIndexer indexer;

    public DefaultTimeline()
    {
        this.persistor = new DefaultTimelinePersistor();
        this.indexer = new DefaultTimelineIndexer();
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

    @Override
    protected void doStart()
        throws IOException
    {
        // if persistor fails, it's a total failure, we
        // cannot work without persistor
        persistor.start( getConfiguration() );

        // indexer start, that might need repair
        // and might end up in falied repair
        try
        {
            indexer.start( getConfiguration() );
        }
        catch ( IOException e )
        {
            repairTimelineIndexer( e );
        }
    }

    @Override
    protected void doStop()
        throws IOException
    {
        final ArrayList<IOException> exceptions = new ArrayList<IOException>( 2 );
        try
        {
            indexer.stop();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Failure during stop of Timeline Indexer.", e );
            exceptions.add( e );
        }
        try
        {
            persistor.stop();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Failure during stop of Timeline Persistor.", e );
            exceptions.add( e );
        }

        if ( !exceptions.isEmpty() )
        {
            throw new IOException( "Exception(s) happened during stop of Timeline", new CompositeException(
                "Multiple exceptions happened, please see prior log messages for details.", exceptions ) );
        }
    }

    @Override
    public void add( final TimelineRecord... records )
    {
        getTimelineLock().readLock().lock();
        try
        {
            if ( !isStarted() )
            {
                return;
            }
            try
            {
                persistor.persist( records );
                addToIndexer( 0, records );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Failed to add a timeline record", e );
            }
        }
        finally
        {
            getTimelineLock().readLock().unlock();
        }
    }

    @Override
    public int purge( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return purgeFromIndexer( 0, timestamp, types, subTypes, filter );
    }

    @Override
    public void retrieve( int fromItem, int count, Set<String> types, Set<String> subTypes, TimelineFilter filter,
                          TimelineCallback callback )
    {
        retrieve( 0L, System.currentTimeMillis(), fromItem, count, types, subTypes, filter, callback );
    }

    @Override
    public void retrieve( long fromTime, long toTime, int from, int count, Set<String> types, Set<String> subTypes,
                          TimelineFilter filter, TimelineCallback callback )
    {
        retrieveFromIndexer( 0, fromTime, toTime, from, count, types, subTypes, filter, callback );
    }

    // ==

    protected void addToIndexer( int retry, final TimelineRecord... records )
    {
        getTimelineLock().readLock().lock();
        try
        {
            if ( retry > MAX_RETRIES )
            {
                return;
            }
            if ( !isStarted() )
            {
                return;
            }
            try
            {
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        indexer.addAll( records );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        addToIndexer( retry + 1, records );
                    }
                }
            }
            catch ( IOException e )
            {
                getLogger().warn( "Failed to add a timeline record", e );
            }
        }
        finally
        {
            getTimelineLock().readLock().unlock();
        }
    }

    protected int purgeFromIndexer( int retry, long timestamp, Set<String> types, Set<String> subTypes,
                                    TimelineFilter filter )
    {
        getTimelineLock().readLock().lock();
        try
        {
            if ( retry > MAX_RETRIES )
            {
                return 0;
            }
            if ( !isStarted() )
            {
                return 0;
            }
            try
            {
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        return indexer.purge( 0l, timestamp, types, subTypes );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        return purgeFromIndexer( retry + 1, timestamp, types, subTypes, filter );
                    }
                }
                else
                {
                    return 0;
                }
            }
            catch ( IOException e )
            {
                getLogger().warn( "Failed to purge timeline!", e );
                return 0;
            }
        }
        finally
        {
            getTimelineLock().readLock().unlock();
        }
    }

    protected void retrieveFromIndexer( int retry, long fromTime, long toTime, int from, int count, Set<String> types,
                                        Set<String> subTypes, TimelineFilter filter, TimelineCallback callback )
    {
        getTimelineLock().readLock().lock();
        try
        {
            if ( retry > MAX_RETRIES )
            {
                return;
            }
            if ( !isStarted() )
            {
                return;
            }
            try
            {
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter, callback );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        retrieveFromIndexer( retry + 1, fromTime, toTime, from, count, types, subTypes, filter,
                            callback );
                    }
                }
                else
                {
                    return;
                }
            }
            catch ( IOException e )
            {
                getLogger().warn( "Unable to retrieve data from timeline!", e );
                return;
            }
        }
        finally
        {
            getTimelineLock().readLock().unlock();
        }
    }

    // ==

    private final ReentrantLock repairLock = new ReentrantLock();

    /**
     * Indexer is healthy if there is no ongoing repair, and indexer reports itself as started.
     */
    protected boolean isIndexerHealthy()
    {
        // we have no ongoing repair and indexer is started
        return ( !repairLock.isLocked() ) && indexer.isStarted();
    }

    /**
     * This method might, and probably will, be invoked by multiple threads doing different things, but only one should
     * "win" and do the job. The prize of the winner is actually a penalty, that the winning thread will do the heavy
     * lifting of repairing the timeline indexer, which is actually shoveling over the records from persistor into newly
     * started indexer, if it was possible to start it at all.
     * 
     * @param e
     * @throws IOException
     */
    protected void repairTimelineIndexer( final Exception e )
        throws IOException
    {
        if ( repairLock.tryLock() )
        {
            try
            {
                getLogger().info( "Timeline index got corrupted, trying to repair it.", e );
                // stopping it cleanly
                indexer.stop();
                // deleting index files
                FileUtils.cleanDirectory( getConfiguration().getIndexDirectory() );
                try
                {
                    // creating new index from scratch
                    indexer.start( getConfiguration() );
                    // pouring over records from persisted into indexer
                    final RepairBatch rb = new RepairBatch( indexer );
                    persistor.readAllSinceDays( getConfiguration().getRepairDaysCountRestored(), rb );
                    rb.finish();
                    if ( indexer.isStarted() )
                    {
                        getLogger().info(
                            "Timeline index is succesfully repaired, the last "
                                + getConfiguration().getRepairDaysCountRestored() + " days were restored." );
                    }
                    else
                    {
                        getLogger().warn( "Timeline index was corrupted and repair of it failed!" );
                    }
                }
                catch ( Exception ex )
                {
                    // we need to stop it
                    indexer.stop();
                    if ( ex instanceof IOException )
                    {
                        throw (IOException) ex;
                    }
                    else
                    {
                        throw new IOException( "Unable to repair the Timeline indexer!", ex );
                    }
                }
            }
            finally
            {
                repairLock.unlock();
            }
        }
    }
}
