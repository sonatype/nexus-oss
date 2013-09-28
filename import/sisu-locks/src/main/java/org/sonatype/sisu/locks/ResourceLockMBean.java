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
 * JMX API for managing and monitoring resource locks.
 */
public interface ResourceLockMBean
{
    /**
     * @return Names of currently allocated resource locks
     */
    String[] listResourceNames();

    /**
     * @return Identities of threads that own the named resource lock
     */
    String[] findOwningThreads( String name );

    /**
     * @return Identities of threads waiting for the named resource lock
     */
    String[] findWaitingThreads( String name );

    /**
     * @return Names of resource locks owned by the given thread
     */
    String[] findOwnedResources( String tid );

    /**
     * @return Names of resource locks wanted by the given thread
     */
    String[] findWaitedResources( String tid );

    /**
     * Forcibly releases the named resource lock; use with caution
     */
    void releaseResource( String name );
}
