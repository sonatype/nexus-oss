/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
