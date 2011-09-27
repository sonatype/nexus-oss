package org.sonatype.sisu.locks;

import java.util.concurrent.ConcurrentMap;

import org.sonatype.guice.bean.reflect.Weak;

abstract class AbstractLocks
    implements Locks
{
    private final ConcurrentMap<String, SharedLock> sharedLocks = Weak.concurrentValues();

    public final SharedLock getSharedLock( final String name )
    {
        SharedLock lock = sharedLocks.get( name );
        if ( null == lock )
        {
            final SharedLock oldLock = sharedLocks.putIfAbsent( name, lock = create( name ) );
            if ( null != oldLock )
            {
                return oldLock;
            }
        }
        return lock;
    }

    protected abstract SharedLock create( final String name );
}
