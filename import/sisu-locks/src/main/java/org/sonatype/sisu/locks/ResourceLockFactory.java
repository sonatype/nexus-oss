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
 * Factory API for managing various kinds of resource locks.
 */
public interface ResourceLockFactory
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
}
