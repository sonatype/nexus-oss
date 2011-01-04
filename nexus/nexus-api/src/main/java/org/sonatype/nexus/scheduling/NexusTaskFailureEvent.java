/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.scheduling;

import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * An event that signals that execution of a {@link NexusTask} has failed.
 *
 * @author Alin Dreghiciu
 */
public class NexusTaskFailureEvent<T>
    extends AbstractEvent<NexusTask<T>>
{

    /**
     * Failure cause.
     */
    private final Throwable m_cause;

    /**
     * Constructor.
     *
     * @param task  failing nexus task
     * @param cause failure cause
     */
    public NexusTaskFailureEvent( final NexusTask<T> task,
                                  final Throwable cause )
    {
        super( task );
        m_cause = cause;
    }

    /**
     * Returns the newxus task that failed.
     *
     * @return failing nexus task
     */
    public NexusTask<T> getNexusTask()
    {
        return getEventSender();
    }

    /**
     * Returns the failure cause.
     *
     * @return cause
     */
    public Throwable getCause()
    {
        return m_cause;
    }

}
