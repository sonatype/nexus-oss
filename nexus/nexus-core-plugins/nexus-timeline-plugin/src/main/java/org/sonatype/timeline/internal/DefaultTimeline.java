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
        getLogger().info( "Starting Timeline..." );
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
        getLogger().info( "Stopping Timeline..." );
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
                addToIndexer( records );
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
        return purgeFromIndexer( timestamp, types, subTypes, filter );
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
        retrieveFromIndexer( fromTime, toTime, from, count, types, subTypes, filter, callback );
    }

    // ==

    protected void addToIndexer( final TimelineRecord... records )
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
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        indexer.addAll( records );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        if ( isIndexerHealthy() )
                        {
                            indexer.addAll( records );
                        }
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

    protected int purgeFromIndexer( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        getTimelineLock().readLock().lock();
        try
        {
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
                        if ( isIndexerHealthy() )
                        {
                            return indexer.purge( 0l, timestamp, types, subTypes );
                        }
                        else
                        {
                            return 0;
                        }
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

    protected void retrieveFromIndexer( long fromTime, long toTime, int from, int count, Set<String> types,
        Set<String> subTypes, TimelineFilter filter, TimelineCallback callback )
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
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter, callback );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        if ( isIndexerHealthy() )
                        {
                            indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter, callback );
                        }
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

    private volatile boolean repairTriedAndFailed = false;

    /**
     * Indexer is healthy if there is no ongoing repair, and indexer reports itself as started.
     */
    protected synchronized boolean isIndexerHealthy()
    {
        // we have no ongoing repair and indexer is started
        final boolean locked = repairLock.tryLock();
        try
        {
            return locked && indexer.isStarted();
        }
        finally
        {
            if ( locked )
            {
                repairLock.unlock();
            }
        }
    }

    /**
     * This method will be invoked by only one thread, the one that detects an indexer problem (catches an IOException
     * during indexer operation most likely). If the thread detecting the problem fails to repair the indexer, it will
     * be flagged as "dead beef" (see {@link DefaultTimeline#repairTriedAndFailed}), so no subsequent thread catching
     * IOException will try to fix it anymore. For concurrent invocations protection (ruling out parallel retries),
     * a lock is used for foolproof protection, but also, {@link #isIndexerHealthy()} method will protect too. Repair
     * will be tried only once, and never after. Timeline repair in that case will happen on next instance reboot, but
     * this was the case even before (indexer was stopped, and {@link #isIndexerHealthy()} method started returning
     * false, hence, noone touched indexer, nor catched IOException anymore, and no repair was tried).
     *
     * @param e
     * @throws IOException
     */
    protected void repairTimelineIndexer( final Exception e )
        throws IOException
    {
        repairLock.lock();
        try
        {
            // check for any previous attempt
            if ( repairTriedAndFailed )
            {
                // return silently, as we leave indexer in broken state to not bork whole instance
                return;
            }
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

                // TODO: the else branch has no sense, as on unsuccesful start, exception will be thrown anyway
                if ( indexer.isStarted() )
                {
                    getLogger().info(
                        "Timeline index is succesfully repaired, the last "
                            + getConfiguration().getRepairDaysCountRestored() + " days were restored." );
                }
                else
                {
                    // this branch will most likely never execute
                    throw new IOException( "Unable to repair the Timeline indexer!" );
                }
            }
            catch ( IOException ex )
            {
                getLogger().warn( "Timeline index was corrupted and repair of it failed!", e );
                // we need to stop it and signal to not try any other thread
                repairTriedAndFailed = true;
                indexer.stop();
                throw ex;
            }
        }
        finally
        {
            repairLock.unlock();
        }
    }
}
