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

package org.sonatype.nexus.timeline.feeds.subscribers;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.events.NexusTaskEvent;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.TaskEvent;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * Event inspector that creates feeds about tasks. Note: this EventInspector is
 * <em>intentionally synchronous EventInspector</em>! Reasoning is mainly to avoid unordered event arrivals (stopped
 * before started), but also, one can easily see by inspecting where these events are fired (see
 * {@link AbstractNexusTask}), that those are fired from already pooled thread (task executing thread), and not the
 * main HTTP request processing ones, hence, this event inspector being synchronous will not steal much CPU cycles from
 * it.
 */
@Named
@Singleton
public class NexusTaskFeedSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber
{
  @Inject
  public NexusTaskFeedSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEvent evt) {
    if (evt instanceof NexusTaskEventStarted<?>) {
      final String action = getActionFromTask(((NexusTaskEventStarted<?>) evt).getNexusTask());
      final String message = getMessageFromTask(((NexusTaskEventStarted<?>) evt).getNexusTask());
      final TaskEvent prc = getFeedRecorder().taskStarted(action, message);
      putTaskEventFromEventContext((NexusTaskEventStarted<?>) evt, prc);
    }
    else if (evt instanceof NexusTaskEventStoppedDone<?>) {
      final TaskEvent prc =
          getTaskEventFromEventContext(((NexusTaskEventStoppedDone<?>) evt).getStartedEvent());
      final String message = getMessageFromTask(((NexusTaskEventStoppedDone<?>) evt).getNexusTask());
      getFeedRecorder().taskFinished(prc, message);
    }
    else if (evt instanceof NexusTaskEventStoppedCanceled<?>) {
      final TaskEvent prc =
          getTaskEventFromEventContext(((NexusTaskEventStoppedCanceled<?>) evt).getStartedEvent());
      final String message = getMessageFromTask(((NexusTaskEventStoppedCanceled<?>) evt).getNexusTask());
      getFeedRecorder().taskCanceled(prc, message);
    }
    else if (evt instanceof NexusTaskEventStoppedFailed<?>) {
      final TaskEvent prc =
          getTaskEventFromEventContext(((NexusTaskEventStoppedFailed<?>) evt).getStartedEvent());
      getFeedRecorder().taskBroken(prc, ((NexusTaskEventStoppedFailed<?>) evt).getFailureCause());
    }
  }

  // ==

  protected void putTaskEventFromEventContext(final NexusTaskEventStarted<?> evt, final TaskEvent prc) {
    evt.getEventContext().put(TaskEvent.class.getName(), prc);
  }

  protected TaskEvent getTaskEventFromEventContext(final NexusTaskEventStarted<?> evt) {
    return (TaskEvent) evt.getEventContext().get(TaskEvent.class.getName());
  }

  protected String getActionFromTask(final NexusTask<?> task) {
    // TODO: change of NexusTask in Quartz is anyway expected, so expose this
    if (task instanceof AbstractNexusTask<?>) {
      try {
        final Method getActionMethod = AbstractNexusTask.class.getDeclaredMethod("getAction");
        getActionMethod.setAccessible(true);
        return (String) getActionMethod.invoke(task);
      }
      catch (Exception e) {
        // nothing
      }
    }
    return "UNKNOWN";
  }

  protected String getMessageFromTask(final NexusTask<?> task) {
    // TODO: change of NexusTask in Quartz is anyway expected, so expose this
    if (task instanceof AbstractNexusTask<?>) {
      try {
        final Method getMessageMethod = AbstractNexusTask.class.getDeclaredMethod("getMessage");
        getMessageMethod.setAccessible(true);
        return (String) getMessageMethod.invoke(task);
      }
      catch (Exception e) {
        // nothing
      }
    }
    return "UNKNOWN";
  }
}
