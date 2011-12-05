/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.email.NexusPostOffice;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.plexus.appevents.Event;

/**
 * {@link EventInspector} that will send alert email (if necessary) in case of a failing {@link NexusTask}.
 * 
 * @author Alin Dreghiciu
 */
@Component( role = EventInspector.class, hint = "nexusTaskFailureAlertEmailSender" )
public class NexusTaskFailureAlertEmailSender
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{

    @Requirement
    private NexusPostOffice m_postOffice;

    /**
     * Accepts events of type {@link NexusTaskFailureEvent}. {@inheritDoc}
     */
    public boolean accepts( final Event<?> evt )
    {
        return evt != null && evt instanceof NexusTaskEventStoppedFailed<?>;
    }

    /**
     * Sends alert emails if necessary. {@inheritDoc}
     */
    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }
        final NexusTaskEventStoppedFailed<?> failureEvent = (NexusTaskEventStoppedFailed<?>) evt;
        final NexusTask<?> failedTask = failureEvent.getNexusTask();
        if ( failedTask == null || !failedTask.shouldSendAlertEmail() )
        {
            return;
        }
        m_postOffice.sendNexusTaskFailure( failedTask.getAlertEmail(), failedTask.getId(), failedTask.getName(),
            failureEvent.getFailureCause() );
    }

}
