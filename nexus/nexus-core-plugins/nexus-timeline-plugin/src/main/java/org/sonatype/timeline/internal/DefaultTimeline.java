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
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineCallback;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineRecord;

@Singleton
@Named( "default" )
public class DefaultTimeline
    extends AbstractStartable
    implements Timeline
{
    private final DefaultTimelinePersistor persistor;

    private final DefaultTimelineIndexer indexer;

    private final ReentrantReadWriteLock timelineLock = new ReentrantReadWriteLock();

    private final ReentrantLock repairLock = new ReentrantLock();

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
        try
        {
            timelineLock.writeLock().lock();
            persistor.start( getConfiguration() );
            try
            {
                indexer.start( getConfiguration() );
            }
            catch ( IOException e )
            {
                repairTimelineIndexer( e );
            }
        }
        finally
        {
            timelineLock.writeLock().unlock();
        }
    }

    @Override
    protected void doStop()
        throws IOException
    {
        try
        {
            timelineLock.writeLock().lock();
            indexer.stop();
        }
        finally
        {
            timelineLock.writeLock().unlock();
        }
    }

    @Override
    public void add( final TimelineRecord... records )
    {
        try
        {
            timelineLock.readLock().lock();
            if ( !isStarted() )
            {
                return;
            }
            try
            {
                persistor.persist( records );
                if ( isIndexerHealthy() )
                {
                    try
                    {
                        indexer.addAll( records );
                    }
                    catch ( IOException e )
                    {
                        repairTimelineIndexer( e );
                        // now try add again
                        indexer.addAll( records );
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
            timelineLock.readLock().unlock();
        }
    }

    @Override
    public int purge( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        try
        {
            timelineLock.readLock().lock();
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
                        // now try purge again
                        return indexer.purge( 0l, timestamp, types, subTypes );
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
            timelineLock.readLock().unlock();
        }
    }

    @Override
    public void retrieve( int fromItem, int count, Set<String> types, Set<String> subTypes, TimelineFilter filter,
                          TimelineCallback callback )
    {
        retrieve( 0L, System.currentTimeMillis(), types, subTypes, fromItem, count, filter, callback );
    }

    // ==

    protected boolean isIndexerHealthy()
    {
        // we have no ongoing repair and indexer is started
        return ( !repairLock.isLocked() ) && indexer.isStarted();
    }

    protected void repairTimelineIndexer( final Exception e )
        throws IOException
    {
        if ( repairLock.tryLock() )
        {
            try
            {
                getLogger().info( "Timeline index got corrupted, trying to repair it.", e );
                indexer.stop();
                FileUtils.cleanDirectory( getConfiguration().getIndexDirectory() );
                try
                {
                    indexer.start( getConfiguration() );
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

    protected void retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes, int from, int count,
                             TimelineFilter filter, TimelineCallback callback )
    {
        try
        {
            timelineLock.readLock().lock();
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

                        // now try retrieve again
                        indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter, callback );
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
            timelineLock.readLock().unlock();
        }
    }
}
