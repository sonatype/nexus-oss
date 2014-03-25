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

package org.sonatype.nexus.timeline.feeds.subscribers;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * Subscriber listening for events recorded under {@link FeedRecorder#FAMILY_TASK} event type.
 */
@Named
@Singleton
public class TaskSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber
{
  @Inject
  public TaskSubscriber(final FeedRecorder feedRecorder) {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEventStarted evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskType", evt.getNexusTask().getClass().getName());
    putIfNotNull(data, "taskId", evt.getNexusTask().getId());
    putIfNotNull(data, "taskName", evt.getNexusTask().getName());
    putIfNotNull(data, "taskAction", getActionFromTask(evt.getNexusTask()));
    putIfNotNull(data, "taskMessage", getMessageFromTask(evt.getNexusTask()));
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_TASK,
        FeedRecorder.TASK_STARTED,
        evt.getEventDate(),
        null, // "system" is running it
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEventStoppedDone evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskStarted", String.valueOf(evt.getStartedEvent().getEventDate().getTime()));
    putIfNotNull(data, "taskType", evt.getNexusTask().getClass().getName());
    putIfNotNull(data, "taskId", evt.getNexusTask().getId());
    putIfNotNull(data, "taskName", evt.getNexusTask().getName());
    putIfNotNull(data, "taskAction", getActionFromTask(evt.getNexusTask()));
    putIfNotNull(data, "taskMessage", getMessageFromTask(evt.getNexusTask()));
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_TASK,
        FeedRecorder.TASK_FINISHED,
        evt.getEventDate(),
        null, // "system" is running it
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEventStoppedCanceled evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskStarted", String.valueOf(evt.getStartedEvent().getEventDate().getTime()));
    putIfNotNull(data, "taskType", evt.getNexusTask().getClass().getName());
    putIfNotNull(data, "taskId", evt.getNexusTask().getId());
    putIfNotNull(data, "taskName", evt.getNexusTask().getName());
    putIfNotNull(data, "taskAction", getActionFromTask(evt.getNexusTask()));
    putIfNotNull(data, "taskMessage", getMessageFromTask(evt.getNexusTask()));
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_TASK,
        FeedRecorder.TASK_CANCELED,
        evt.getEventDate(),
        null, // "system" is running it
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEventStoppedFailed evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskStarted", String.valueOf(evt.getStartedEvent().getEventDate().getTime()));
    putIfNotNull(data, "taskFailure", String.valueOf(evt.getFailureCause()));
    putIfNotNull(data, "taskType", evt.getNexusTask().getClass().getName());
    putIfNotNull(data, "taskId", evt.getNexusTask().getId());
    putIfNotNull(data, "taskName", evt.getNexusTask().getName());
    putIfNotNull(data, "taskAction", getActionFromTask(evt.getNexusTask()));
    putIfNotNull(data, "taskMessage", getMessageFromTask(evt.getNexusTask()));
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_TASK,
        FeedRecorder.TASK_FAILED,
        evt.getEventDate(),
        null, // "system" is running it
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  // ==

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
