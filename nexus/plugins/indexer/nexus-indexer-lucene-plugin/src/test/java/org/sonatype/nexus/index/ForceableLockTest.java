package org.sonatype.nexus.index;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ForceableLockTest
{
    private ForceableReentrantLock subject;

    private TestThread otherThread;

    private static class TestThread
        extends Thread
    {
        public volatile boolean interrupted = false;

        public volatile boolean locked = false;

        public final Semaphore semaphore = new Semaphore( 1 );

        private final ForceableReentrantLock lock;

        public TestThread( final ForceableReentrantLock lock )
        {
            this.lock = lock;
        }

        @Override
        public void run()
        {
            locked = lock.tryLock();
            semaphore.release();
            try
            {
                try
                {
                    Thread.sleep( 60 * 1000L );
                }
                catch ( InterruptedException e )
                {
                    interrupted = true;
                }
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    @Before
    public void setUp()
    {
        subject = new ForceableReentrantLock();
        otherThread = new TestThread( subject );
    }

    @After
    public void tearDown()
        throws InterruptedException
    {
        otherThread.interrupt();
        otherThread.join();
    }

    @Test
    public void basic()
        throws InterruptedException
    {
        otherThread.semaphore.acquire();
        otherThread.start();
        otherThread.semaphore.acquire();

        Assert.assertTrue( otherThread.locked ); // the other thread is holding the lock
        Assert.assertFalse( otherThread.interrupted ); // sanity check
        Assert.assertFalse( subject.tryLock() ); // this thread is not able to get the lock

        Assert.assertTrue( subject.tryForceLock( 5, TimeUnit.SECONDS ) ); // this thread can force the lock
        Assert.assertTrue( otherThread.interrupted );
    }

    @Test
    public void reentrant()
    {
        Assert.assertTrue( subject.tryLock() );
        Assert.assertTrue( subject.tryLock() );

        subject.unlock();
        subject.unlock();
    }

    @Test( expected = IllegalStateException.class )
    public void nonownerUnlock()
        throws InterruptedException
    {
        otherThread.semaphore.acquire();
        otherThread.start();
        otherThread.semaphore.acquire();

        Assert.assertTrue( otherThread.locked ); // the other thread is holding the lock
        Assert.assertFalse( otherThread.interrupted ); // sanity check

        subject.unlock();
    }

    @Test( expected = IllegalStateException.class )
    public void notlockedUnlock()
    {
        subject.unlock();
    }
}
