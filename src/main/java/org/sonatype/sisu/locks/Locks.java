package org.sonatype.sisu.locks;

public interface Locks
{
    SharedLock getSharedLock( String name );

    interface SharedLock
    {
        void lockShared();

        void lockExclusive();

        void unlockExclusive();

        void unlockShared();

        boolean isExclusive();

        int globalOwners();

        Thread[] localOwners();

        int sharedLockCount( Thread thread );

        int exclusiveLockCount( Thread thread );
    }
}
