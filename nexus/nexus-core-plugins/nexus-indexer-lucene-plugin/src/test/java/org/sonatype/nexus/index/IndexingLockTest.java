package org.sonatype.nexus.index;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import junit.framework.TestCase;

public class IndexingLockTest
    extends TestCase
{

    private ReadWriteLock locker;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        locker = new ReentrantReadWriteLock();
    }

    public void testIndexLocking()
    {
        beginIndex();
    }

    public void testThreadIndexLocking() throws InterruptedException
    {
        new Thread( new Runnable()
        {

            public void run()
            {
                Lock lock = locker.writeLock();
                lock.lock();

                Thread.yield();

                try
                {
                    Thread.sleep( 3000 );
                }
                catch ( InterruptedException e )
                {
                    // no problem
                }
                finally
                {
                    lock.unlock();
                }
            }
        } ).start();

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        Lock lock = locker.readLock();
        boolean hasLock = true;
        try
        {
            hasLock = lock.tryLock();
            assertFalse( hasLock );
        }
        finally
        {
            if ( hasLock )
                lock.unlock();
        }
    }

    private void beginIndex()
    {
        Lock lock = locker.writeLock();
        lock.lock();
        try
        {
            downloadRemoteIndex();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void downloadRemoteIndex()
    {
        Lock lock = locker.readLock();
        try
        {
            assertTrue( lock.tryLock() );
        }
        finally
        {
            lock.unlock();
        }
    }

}
