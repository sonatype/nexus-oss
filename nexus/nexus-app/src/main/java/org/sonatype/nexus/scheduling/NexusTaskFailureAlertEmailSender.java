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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.email.NexusPostOffice;
import org.sonatype.plexus.appevents.Event;

/**
 * {@link EventInspector} that will send alert email (if necessary) in case of a failing {@link NexusTask}.
 *
 * @author Alin Dreghiciu
 */
@Component( role = EventInspector.class, hint = "nexusTaskFailureAlertEmailSender" )
public class NexusTaskFailureAlertEmailSender
    implements EventInspector
{

    @Requirement
    private NexusPostOffice m_postOffice;

    /**
     * Accepts events of type {@link NexusTaskFailureEvent}.
     *
     * {@inheritDoc}
     */
    public boolean accepts( final Event<?> evt )
    {
        return evt != null && evt instanceof NexusTaskFailureEvent;
    }

    /**
     * Sends alert emails if necessary.
     * {@inheritDoc}
     */
    public void inspect( final Event<?> evt )
    {
        if( !accepts( evt ) )
        {
            return;
        }
        final NexusTaskFailureEvent<?> failureEvent = (NexusTaskFailureEvent<?>) evt;
        final NexusTask<?> failedTask = failureEvent.getNexusTask();
        if( failedTask == null || !failedTask.shouldSendAlertEmail() )
        {
            return;
        }
        // TODO how to get id and name
        m_postOffice.sendNexusTaskFailure( failedTask.getAlertEmail(), null, null, failureEvent.getCause() );
    }
    
}
