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

        try
        {
            uidA.lock();
            assert RepositoryItemUid.getLockCount() == 1;

            uidB.lock();
            assert RepositoryItemUid.getLockCount() == 2;

            uidC.lock();
            assert RepositoryItemUid.getLockCount() == 3;

            uidD.lock();
            assert RepositoryItemUid.getLockCount() == 4;

            uidE.lock();
            assert RepositoryItemUid.getLockCount() == 5;

            uidA.unlock();
            assert RepositoryItemUid.getLockCount() == 4;

            uidB.unlock();
            assert RepositoryItemUid.getLockCount() == 3;

            uidC.unlock();
            assert RepositoryItemUid.getLockCount() == 2;

            uidD.unlock();
            assert RepositoryItemUid.getLockCount() == 1;

            uidE.unlock();
            assert RepositoryItemUid.getLockCount() == 0;
        }
        finally
        {
            // just in case
            uidA.unlock();
            uidB.unlock();
            uidC.unlock();
            uidD.unlock();
            uidE.unlock();
        }
    }
    
    public void testConcurrentLocksOfSameUid()
        throws Exception
    {
        RepositoryItemUid uidA = new RepositoryItemUid( repository, "/a.txt" );
        
        Thread thread = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 20) );
        Thread thread2 = new Thread( new RepositoryItemUidLockProcessLauncher( uidA, 100, 20) );
        
        thread.start();
        thread2.start();
        
        Thread.sleep( 10 );
        
        assertEquals( 1, RepositoryItemUid.getLockCount() );
        
        thread.join();
        thread2.join();
        
        assertEquals( 0, RepositoryItemUid.getLockCount() );
    }
    
    private static final class RepositoryItemUidLockProcessLauncher
        implements
            Runnable
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
            
            for ( int i = 0 ; i < threadCount ; i++ )
            {
                threads.add( new Thread( new RepositoryItemUidLockProcess( this.uid, timeout ) ) );
            }
            
            for ( Iterator<Thread> iter = threads.iterator() ; iter.hasNext() ; )
            {
                iter.next().start();
            }
            
            try
            {
                Thread.sleep( 5 );
                
                for ( Iterator<Thread> iter = threads.iterator() ; iter.hasNext() ; )
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
        implements
            Runnable
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
            try
            {
                this.uid.lock();
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
