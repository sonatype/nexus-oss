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
package org.sonatype.nexus.proxy.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.repository.DummyRepository;

public class RepositoryItemUidTest
    extends AbstractNexusTestCase
{
    protected DummyRepository repository = new DummyRepository( "dummy" );

    public void testLocks()
        throws Exception
    {
        RepositoryItemUid uidA = new RepositoryItemUid( repository, "/a.txt" );
        RepositoryItemUid uidB = new RepositoryItemUid( repository, "/b.txt" );
        RepositoryItemUid uidC = new RepositoryItemUid( repository, "/c.txt" );
        RepositoryItemUid uidD = new RepositoryItemUid( repository, "/d.txt" );
        RepositoryItemUid uidE = new RepositoryItemUid( repository, "/e.txt" );

        ReentrantLock lockA = null;
        ReentrantLock lockB = null;
        ReentrantLock lockC = null;
        ReentrantLock lockD = null;
        ReentrantLock lockE = null;

        lockA = uidA.lock();
        assert RepositoryItemUid.getLockCount() == 1;

        lockB = uidB.lock();
        assert RepositoryItemUid.getLockCount() == 2;

        lockC = uidC.lock();
        assert RepositoryItemUid.getLockCount() == 3;

        lockD = uidD.lock();
        assert RepositoryItemUid.getLockCount() == 4;

        lockE = uidE.lock();
        assert RepositoryItemUid.getLockCount() == 5;

        uidA.unlock( lockA );
        assert RepositoryItemUid.getLockCount() == 4;

        uidB.unlock( lockB );
        assert RepositoryItemUid.getLockCount() == 3;

        uidC.unlock( lockC );
        assert RepositoryItemUid.getLockCount() == 2;

        uidD.unlock( lockD );
        assert RepositoryItemUid.getLockCount() == 1;

        uidE.unlock( lockE );
        assert RepositoryItemUid.getLockCount() == 0;
    }

    public void testConcurrentLocksOfSameUid()
        throws Exception
    {
        RepositoryItemUid uidA = new RepositoryItemUid( repository, "/a.txt" );

        Thread thread = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 100 ) );
        Thread thread2 = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 100 ) );

        thread.start();
        thread2.start();

        Thread.sleep( 50 );

        assertEquals( 1, RepositoryItemUid.getLockCount() );

        thread.join();
        thread2.join();

        assertEquals( 0, RepositoryItemUid.getLockCount() );
    }

    private static final class RepositoryItemUidLockProcessLauncher
        implements Runnable
    {
        private RepositoryItemUid uid;

        private int threadCount;

        private long timeout;

        public RepositoryItemUidLockProcessLauncher( RepositoryItemUid uid, int threadCount, long timeout )
        {
            this.uid = uid;
            this.threadCount = threadCount;
            this.timeout = timeout;
        }

        public void run()
        {
            ArrayList<Thread> threads = new ArrayList<Thread>();

            for ( int i = 0; i < threadCount; i++ )
            {
                threads.add( new Thread( new RepositoryItemUidLockProcess( this.uid, timeout ) ) );
            }

            for ( Iterator<Thread> iter = threads.iterator(); iter.hasNext(); )
            {
                iter.next().start();
            }

            try
            {
                Thread.sleep( 5 );

                for ( Iterator<Thread> iter = threads.iterator(); iter.hasNext(); )
                {
                    iter.next().join();
                }
            }
            catch ( InterruptedException e )
            {
            }
        }
    }

    private static final class RepositoryItemUidLockProcess
        implements Runnable
    {
        private RepositoryItemUid uid;

        private long timeout;

        public RepositoryItemUidLockProcess( RepositoryItemUid uid, long timeout )
        {
            this.uid = uid;
            this.timeout = timeout;
        }

        public void run()
        {
            ReentrantLock lock = this.uid.lock();

            try
            {
                Thread.sleep( timeout );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
            finally
            {
                this.uid.unlock( lock );
            }
        }
    }
}
