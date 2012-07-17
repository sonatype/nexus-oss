package org.sonatype.nexus.auth;

/**
 * Manages and provides {@link ClientInfo} instances.
 * 
 * @author cstamas
 * @since 2.1
 */
public interface ClientInfoProvider
{
    /**
     * Returns the {@link ClientInfo} for current thread. It will be non-null if this thread is a REST (or better HTTP)
     * Request processing thread, and {@code null} if this is a non REST Request processing thread (like a scheduled
     * task threads are).
     * 
     * @return the current thread's {@link ClientInfo} or {@code null} if none available.
     */
    ClientInfo getCurrentThreadClientInfo();
}
