package org.sonatype.nexus.proxy.item;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.access.Action;

public class DefaultRepositoryItemUidLock
    implements RepositoryItemUidLock
{
    private final RepositoryItemUidFactory factory;

    private final RepositoryItemUid uid;

    private final LockResource contentLock;

    private final AtomicInteger released;

    protected DefaultRepositoryItemUidLock( final RepositoryItemUidFactory factory, final RepositoryItemUid uid,
                                            final LockResource contentLock )
    {
        super();

        this.factory = factory;

        this.uid = uid;

        this.contentLock = contentLock;

        this.released = new AtomicInteger( 0 );
    }

    @Override
    public RepositoryItemUid getRepositoryItemUid()
    {
        return uid;
    }

    @Override
    public void lock( final Action action )
        throws IllegalStateException
    {
        if ( released.get() != 0 )
        {
            throw new IllegalStateException(
                "This instance of DefaultRepositoryItemUidLock has been released, it is not usable for locking anymore!" );
        }

        if ( action.isReadAction() )
        {
            contentLock.lockShared();
        }
        else
        {
            contentLock.lockExclusively();
        }
    }

    public void unlock()
        throws IllegalStateException
    {
        if ( released.get() != 0 )
        {
            throw new IllegalStateException(
                "This instance of DefaultRepositoryItemUidLock has been released, it is not usable for locking anymore!" );
        }

        contentLock.unlock();
    }

    @Override
    public void release()
    {
        // This below is problematic: this is untrue, since new INSTANCES will be asked for, but they will all use
        // _shared_ lock.
        // Hence, a boxed use will try to release it, but since it's unaware of being boxed, will not be able to.
        // TODO: think about this more...
        //
        // if ( contentLock.hasLocksHeld() )
        // {
        // throw new IllegalStateException(
        // "This instance of DefaultRepositoryItemUidLock still has locks associated with caller thread!" );
        // }

        if ( released.compareAndSet( 0, 1 ) )
        {
            factory.releaseUidLock( this );
        }
    }

    // ==

    // this below is mere for "transition period" to ease debugging or leak detection

    private static Logger logger = LoggerFactory.getLogger( DefaultRepositoryItemUidLock.class );

    public void finalize()
        throws Throwable
    {
        try
        {
            if ( released.compareAndSet( 0, 1 ) )
            {
                factory.releaseUidLock( this );

                logger.error( "Memory leak: UIDLock for UID {} not released properly, lock status is {}!",
                    getRepositoryItemUid(), contentLock.toString() );
            }
        }
        finally
        {
            super.finalize();
        }
    }

    // for Debug/tests vvv

    protected LockResource getContentLock()
    {
        return contentLock;
    }

    // for Debug/tests ^^^

}
