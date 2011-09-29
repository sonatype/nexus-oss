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

import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Local JDK {@link Locks}.
 */
@Named
@Singleton
public final class DefaultLocks
    extends AbstractLocks
{
    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected ResourceLock create( final String name )
    {
        return new ResourceLockImpl();
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ResourceLock} implemented on top of a JDK {@link Semaphore}.
     */
    public static final class ResourceLockImpl
        extends AbstractSemaphoreResourceLock
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Semaphore sem = new Semaphore( Integer.MAX_VALUE, true );

        // ----------------------------------------------------------------------
        // Semaphore methods
        // ----------------------------------------------------------------------

        @Override
        protected void acquire( final int permits )
        {
            sem.acquireUninterruptibly( permits );
        }

        @Override
        protected void release( final int permits )
        {
            sem.release( permits );
        }

        @Override
        protected int availablePermits()
        {
            return sem.availablePermits();
        }
    }
}
