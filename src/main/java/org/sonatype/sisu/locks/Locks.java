package org.sonatype.sisu.locks;

public interface Locks
{
    ResourceLock getResourceLock( String name );

    interface ResourceLock
    {
        void lockShared();

        void lockExclusive();

        void unlockExclusive();

        void unlockShared();

        boolean isExclusive();

        int globalOwners();

        Thread[] localOwners();

        int sharedCount( Thread thread );

        int exclusiveCount( Thread thread );
    }
}
