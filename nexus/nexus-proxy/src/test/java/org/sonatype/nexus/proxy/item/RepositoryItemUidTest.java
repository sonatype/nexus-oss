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

import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryItemUidTest
    extends AbstractNexusTestEnvironment
{
    protected Repository repository;

    public void testDummy()
    {
        
    }
    
    public void nontestConcurrentLocksOfSameUid()
        throws Exception
    {
        RepositoryItemUid uidA = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );

        Thread thread = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 100 ) );
        Thread thread2 = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 100 ) );

        thread.start();
        thread2.start();

        Thread.sleep( 50 );

        assertEquals( 1, getRepositoryItemUidFactory().getLockCount() );

        thread.join();
        thread2.join();

        getRepositoryItemUidFactory().release( uidA );

        assertEquals( 0, getRepositoryItemUidFactory().getLockCount() );
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
            this.uid.lock();

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
                this.uid.unlock();
            }
        }
    }
}
