/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryItemUidTest
    extends AbstractNexusTestEnvironment
{
    protected Repository repository;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        repository = createMock(Repository.class);

        makeThreadSafe( repository, true );
        
        expect(repository.getId()).andReturn( "dummy" ).anyTimes();
        
        replay(repository);
    }
    
    public void testReleaseFromMemory()
        throws Exception
    {
        RepositoryItemUidFactory factory = getRepositoryItemUidFactory();
        
        RepositoryItemUid uid = factory.createUid( repository, "/a.txt" );
        RepositoryItemUid uid2 = factory.createUid( repository, "/a.txt" );
        RepositoryItemUid uid3 = factory.createUid( repository, "/b.txt" );
        
        // Proof that create isn't putting anything in the internal maps
        assertEquals( 0, factory.getUidCount() );
        assertEquals( 0, factory.getLockCount() );
        
        factory.lock( uid );
        
        // Proof that locking a uid adds it to internal maps
        assertEquals( 1, factory.getUidCount() );
        assertEquals( 1, factory.getLockCount() );
        
        factory.lock( uid2 );
        
        // Proof that locking 2 uids w/ the same item does not increase the internal map count
        assertEquals( 1, factory.getUidCount() );
        assertEquals( 1, factory.getLockCount() );
        
        factory.lock( uid3 );
        
        // Proof that using a different uid creates a new item in internal map
        assertEquals( 2, factory.getUidCount() );
        assertEquals( 2, factory.getLockCount() );
        
        factory.unlock( uid3 );
        
        // Proof that removing an item updates internal maps
        assertEquals( 1, factory.getUidCount() );
        assertEquals( 1, factory.getLockCount() );
        
        factory.unlock( uid2 );
        
        // Proof that removing an item that was added twice, doesn't remove the whole list
        assertEquals( 1, factory.getUidCount() );
        assertEquals( 1, factory.getLockCount() );
        
        factory.unlock( uid );
        
        // Proof that removing the final item (if added more than once) removes whole list
        assertEquals( 0, factory.getUidCount() );
        assertEquals( 0, factory.getLockCount() );
    }

    public void testConcurrentLocksOfSameUid()
        throws Exception
    {   
        RepositoryItemUidFactory factory = getRepositoryItemUidFactory();
        
        RepositoryItemUid uidA = factory.createUid( repository, "/a.txt" );

        Thread thread1 = new Thread(
            new RepositoryItemUidLockProcessLauncher( factory, uidA, 100, 100 ) );
        Thread thread2 = new Thread(
            new RepositoryItemUidLockProcessLauncher( factory, uidA, 100, 100 ) );

        thread1.start();
        thread2.start();

        Thread.sleep( 50 );

        assertEquals( 1, getRepositoryItemUidFactory().getLockCount() );

        thread1.join();
        thread2.join();

        assertEquals( 0, getRepositoryItemUidFactory().getLockCount() );
    }

    private static final class RepositoryItemUidLockProcessLauncher
        implements Runnable
    {
        private RepositoryItemUidFactory repositoryItemUidFactory;

        private RepositoryItemUid uid;

        private int threadCount;

        private long timeout;

        public RepositoryItemUidLockProcessLauncher( RepositoryItemUidFactory repositoryItemUidFactory,
            RepositoryItemUid uid, int threadCount, long timeout )
        {
            this.uid = uid;
            this.threadCount = threadCount;
            this.timeout = timeout;
            this.repositoryItemUidFactory = repositoryItemUidFactory;
        }

        public void run()
        {
            ArrayList<Thread> threads = new ArrayList<Thread>();

            for ( int i = 0; i < threadCount; i++ )
            {
                threads.add( new Thread( new RepositoryItemUidLockProcess( repositoryItemUidFactory, uid, timeout ) ) );
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
        private RepositoryItemUidFactory repositoryItemUidFactory;

        private RepositoryItemUid uid;

        private long timeout;

        public RepositoryItemUidLockProcess( RepositoryItemUidFactory repositoryItemUidFactory, RepositoryItemUid uid,
            long timeout )
        {
            this.repositoryItemUidFactory = repositoryItemUidFactory;
            this.uid = uid;
            this.timeout = timeout;
        }

        public void run()
        {
            repositoryItemUidFactory.lock( uid );

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
                repositoryItemUidFactory.unlock( uid );
            }
        }
    }
}
