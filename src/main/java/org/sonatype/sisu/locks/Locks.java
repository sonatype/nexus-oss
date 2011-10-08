/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

/**
 * Factory API for creating various kinds of locks.
 */
public interface Locks
{
    /**
     * Returns the {@link ResourceLock} associated with the given resource name.
     * 
     * @param name The lock name
     * @return Named resource lock
     */
    ResourceLock getResourceLock( String name );

    /**
     * Returns all resource names associated with active {@link ResourceLock}s.
     * 
     * @return Resource names
     */
    String[] getResourceNames();

    /**
     * Shuts down the lock factory and cleans up any allocated resources/threads.
     */
    void shutdown();

    // ----------------------------------------------------------------------
    // Lock types
    // ----------------------------------------------------------------------

    /**
     * Reentrant lock for resources that support shared and/or exclusive access.
     */
    interface ResourceLock
    {
        /**
         * Takes a shared resource lock for the given thread.
         */
        void lockShared( Thread thread );

        /**
         * Takes an exclusive resource lock for the given thread.
         */
        void lockExclusive( Thread thread );

        /**
         * Drops an exclusive resource lock for the given thread.
         */
        void unlockExclusive( Thread thread );

        /**
         * Drops a shared resource lock for the given thread.
         */
        void unlockShared( Thread thread );

        /**
         * @return Threads that currently hold locks on this resource
         */
        Thread[] getOwners();

        /**
         * @return Threads that are waiting for locks on this resource
         */
        Thread[] getWaiters();

        /**
         * @return Number of shared locks taken by the given thread
         */
        int getSharedCount( Thread thread );

        /**
         * @return Number of exclusive locks taken by the given thread
         */
        int getExclusiveCount( Thread thread );
    }
}
