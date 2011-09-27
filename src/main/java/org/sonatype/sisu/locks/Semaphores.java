package org.sonatype.sisu.locks;

public interface Semaphores
{
    interface Sem
    {
        void acquireShared();

        void acquireExclusive();

        void releaseExclusive();

        void releaseShared();
        
        int[] getHoldCounts();
    }

    Sem get( String name );
}
