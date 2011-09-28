package org.sonatype.sisu.locks;

import java.util.concurrent.ConcurrentMap;

import org.sonatype.guice.bean.reflect.Weak;

abstract class AbstractLocks
    implements Locks
{
    private final ConcurrentMap<String, ResourceLock> resourceLocks = Weak.concurrentValues();

    public final ResourceLock getResourceLock( final String name )
    {
        ResourceLock lock = resourceLocks.get( name );
        if ( null == lock )
        {
            final ResourceLock oldLock = resourceLocks.putIfAbsent( name, lock = create( name ) );
            if ( null != oldLock )
            {
                return oldLock;
            }
        }
        return lock;
    }

    protected abstract ResourceLock create( final String name );
}
