package org.sonatype.nexus.proxy.item;

/**
 * Before you continue using this class, stop and read carefully it's description. This "smart" lock is NOT an improved
 * implementation of Java's ReentrantReadWriteLock in any way. It does NOT implement the unsupported atomic
 * lock-upgrade. All this class allows is following pattern:
 * 
 * <pre>
 *   ...
 *   lock.lockShared();
 *   try {
 *     ...
 *     lock.lockExclusively();
 *     try {
 *       ...
 *     } finally {
 *       lock.unlock();
 *     }
 *     ...
 *   } finally {
 *     lock.unlock();
 *   }
 * </pre>
 * 
 * @author cstamas
 */
public interface LockResource
{
    /**
     * Acquires a shared lock. Blocks until successful.
     */
    void lockShared();

    /**
     * Acquires an exclusive lock. Blocks until successful.
     */
    void lockExclusively();

    /**
     * Unlocks the last acquired lock.
     */
    void unlock();

    /**
     * Returns true if the caller thread owns locks of any kinds on this lock.
     * 
     * @return
     */
    boolean hasLocksHeld();
}
