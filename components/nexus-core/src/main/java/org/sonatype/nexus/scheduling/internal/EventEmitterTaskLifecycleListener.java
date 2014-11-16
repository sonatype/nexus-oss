/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.scheduling.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskLifecycleListenerSupport;
import org.sonatype.nexus.scheduling.events.NexusTaskEventCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener that emits task events.
 */
@Singleton
@Named
public class EventEmitterTaskLifecycleListener
    extends TaskLifecycleListenerSupport
{
  private final EventBus eventBus;

  @Inject
  public EventEmitterTaskLifecycleListener(final EventBus eventBus)
  {
    this.eventBus = checkNotNull(eventBus);
  }

  @Override
  public void onTaskStarted(TaskInfo<?> task) {
    eventBus.post(new NexusTaskEventStarted<>(task));
  }

  @Override
  public void onTaskCanceled(TaskInfo<?> task) {
    eventBus.post(new NexusTaskEventCanceled<>(task));
  }

  @Override
  public void onTaskStoppedStopped(TaskInfo<?> task) {
    eventBus.post(new NexusTaskEventStoppedDone<>(task));
  }

  @Override
  public void onTaskStoppedStoppedCanceled(TaskInfo<?> task) {
    eventBus.post(new NexusTaskEventStoppedCanceled<>(task));
  }

  @Override
  public void onTaskStoppedStoppedFailed(TaskInfo<?> task, Throwable reason) {
    eventBus.post(new NexusTaskEventStoppedFailed<>(task, reason));
  }
}
