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

import java.util.concurrent.ConcurrentMap;

import org.sonatype.guice.bean.reflect.Weak;

/**
 * Abstract {@link Locks} implementation; associates names with {@link ResourceLock}s.
 */
abstract class AbstractLocks
    implements Locks
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<String, ResourceLock> resourceLocks = Weak.concurrentValues();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Lock associated with the given resource name
     */
    protected abstract ResourceLock create( final String name );
}
