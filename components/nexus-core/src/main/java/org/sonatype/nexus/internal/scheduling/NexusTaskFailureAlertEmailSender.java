/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.internal.scheduling;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.nexus.email.NexusEmailer;
import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.events.TaskEventStoppedFailed;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link EventSubscriber} that will send alert email (if necessary) in case of a failing {@link Task}.
 */
@Singleton
@Named
public class NexusTaskFailureAlertEmailSender
    extends ComponentSupport
    implements EventSubscriber, Asynchronous
{
  private final NexusEmailer nexusEmailer;

  @Inject
  public NexusTaskFailureAlertEmailSender(final NexusEmailer nexusEmailer) {
    this.nexusEmailer = checkNotNull(nexusEmailer);
  }

  /**
   * Sends alert emails if necessary. {@inheritDoc}
   */
  @Subscribe
  @AllowConcurrentEvents
  public void inspect(final TaskEventStoppedFailed failureEvent) {
    final TaskInfo failedTask = failureEvent.getTaskInfo();
    if (failedTask == null || failedTask.getConfiguration().getAlertEmail() == null) {
      return;
    }
    sendNexusTaskFailure(
        failedTask.getConfiguration().getAlertEmail(),
        failedTask.getId(),
        failedTask.getName(),
        failureEvent.getFailureCause()
    );
  }

  private void sendNexusTaskFailure(final String email,
                                    final String taskId,
                                    final String taskName,
                                    final Throwable cause)
  {
    final StringBuilder body = new StringBuilder();

    if (taskId != null) {
      body.append(String.format("Task ID: %s", taskId)).append("\n");
    }

    if (taskName != null) {
      body.append(String.format("Task Name: %s", taskName)).append("\n");
    }

    if (cause != null) {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      cause.printStackTrace(pw);
      body.append("Stack trace: ").append("\n").append(sw.toString());
    }

    MailRequest request = nexusEmailer.getDefaultMailRequest("Nexus: Task execution failure", body.toString());

    request.getToAddresses().add(new Address(email));

    nexusEmailer.sendMail(request);
  }
}
