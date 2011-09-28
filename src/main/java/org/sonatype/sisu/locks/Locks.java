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

    // ----------------------------------------------------------------------
    // Lock types
    // ----------------------------------------------------------------------

    /**
     * Reentrant lock for resources that support shared and/or exclusive access.
     */
    interface ResourceLock
    {
        /**
         * Takes a shared lock for the associated resource.
         */
        void lockShared();

        /**
         * Takes an exclusive lock for the associated resource.
         */
        void lockExclusive();

        /**
         * Drops an exclusive lock for the associated resource.
         */
        void unlockExclusive();

        /**
         * Drops a shared lock for the associated resource.
         */
        void unlockShared();

        /**
         * @return {@code true} if the resource is exclusively locked; otherwise {@code false}
         */
        boolean isExclusive();

        /**
         * @return Number of threads (local and remote) accessing the associated resource
         */
        int globalOwners();

        /**
         * @return Details of local threads currently accessing the associated resource
         */
        Thread[] localOwners();

        /**
         * @return Number of shared locks taken by the given thread
         */
        int sharedCount( Thread thread );

        /**
         * @return Number of exclusive locks taken by the given thread
         */
        int exclusiveCount( Thread thread );
    }
}
