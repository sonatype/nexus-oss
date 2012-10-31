/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.timeline.internal;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.timeline.TimelineConfiguration;

public abstract class AbstractLockingStartable
    extends AbstractStartable
{
    private final ReentrantReadWriteLock timelineLock;

    protected AbstractLockingStartable()
    {
        this.timelineLock = new ReentrantReadWriteLock();
    }

    public void start( TimelineConfiguration config )
        throws IOException
    {
        getTimelineLock().writeLock().lock();
        try
        {
            super.start( config );
        }
        finally
        {
            getTimelineLock().writeLock().unlock();
        }
    }

    public void stop()
        throws IOException
    {
        getTimelineLock().writeLock().lock();
        try
        {
            super.stop();
        }
        finally
        {
            getTimelineLock().writeLock().unlock();
        }
    }

    protected ReentrantReadWriteLock getTimelineLock()
    {
        return timelineLock;
    }
}
