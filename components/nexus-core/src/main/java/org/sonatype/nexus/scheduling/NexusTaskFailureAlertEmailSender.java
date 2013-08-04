/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.scheduling;

import org.sonatype.nexus.email.NexusPostOffice;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * {@link EventInspector} that will send alert email (if necessary) in case of a failing {@link NexusTask}.
 *
 * @author Alin Dreghiciu
 */
@Component(role = EventInspector.class, hint = "nexusTaskFailureAlertEmailSender")
public class NexusTaskFailureAlertEmailSender
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{

  @Requirement
  private NexusPostOffice m_postOffice;

  /**
   * Accepts events of type {@link NexusTaskFailureEvent}. {@inheritDoc}
   */
  public boolean accepts(final Event<?> evt) {
    return evt != null && evt instanceof NexusTaskEventStoppedFailed<?>;
  }

  /**
   * Sends alert emails if necessary. {@inheritDoc}
   */
  public void inspect(final Event<?> evt) {
    if (!accepts(evt)) {
      return;
    }
    final NexusTaskEventStoppedFailed<?> failureEvent = (NexusTaskEventStoppedFailed<?>) evt;
    final NexusTask<?> failedTask = failureEvent.getNexusTask();
    if (failedTask == null || !failedTask.shouldSendAlertEmail()) {
      return;
    }
    m_postOffice.sendNexusTaskFailure(failedTask.getAlertEmail(), failedTask.getId(), failedTask.getName(),
        failureEvent.getFailureCause());
  }

}
