package org.sonatype.sisu.locks;

import java.util.Collection;

public interface Locks
{
    interface SharedLock
    {
        void lockShared();

        void lockExclusive();

        void unlockExclusive();

        void unlockShared();

        int sharedLockCount( Thread thread );

        int exclusiveLockCount( Thread thread );

        Collection<Thread> owners();
    }

    SharedLock getSharedLock( String name );
}
