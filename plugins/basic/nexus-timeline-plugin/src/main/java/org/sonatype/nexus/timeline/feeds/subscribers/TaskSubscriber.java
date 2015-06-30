/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.scheduling.events.TaskEventStarted;
import org.sonatype.nexus.scheduling.events.TaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.TaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.TaskEventStoppedFailed;
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
  public TaskSubscriber(final FeedRecorder feedRecorder)
  {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final TaskEventStarted evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskType", evt.getTaskInfo().getConfiguration().getTypeId());
    putIfNotNull(data, "taskId", evt.getTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getTaskInfo().getConfiguration().getTypeName());
    putIfNotNull(data, "taskMessage", evt.getTaskInfo().getConfiguration().getMessage());
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
  public void on(final TaskEventStoppedDone evt) {
    final Map<String, String> data = Maps.newHashMap();
    long taskStarted = evt.getTaskInfo().getLastRunState() != null ? evt.getTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskType", evt.getTaskInfo().getConfiguration().getTypeId());
    putIfNotNull(data, "taskId", evt.getTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getTaskInfo().getConfiguration().getTypeName());
    putIfNotNull(data, "taskMessage", evt.getTaskInfo().getConfiguration().getMessage());
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
  public void on(final TaskEventStoppedCanceled evt) {
    final Map<String, String> data = Maps.newHashMap();
    long taskStarted = evt.getTaskInfo().getLastRunState() != null ? evt.getTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskType", evt.getTaskInfo().getConfiguration().getTypeId());
    putIfNotNull(data, "taskId", evt.getTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getTaskInfo().getConfiguration().getTypeName());
    putIfNotNull(data, "taskMessage", evt.getTaskInfo().getConfiguration().getMessage());
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
  public void on(final TaskEventStoppedFailed evt) {
    final Map<String, String> data = Maps.newHashMap();
    long taskStarted = evt.getTaskInfo().getLastRunState() != null ? evt.getTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskFailure", String.valueOf(evt.getFailureCause()));
    putIfNotNull(data, "taskType", evt.getTaskInfo().getConfiguration().getTypeId());
    putIfNotNull(data, "taskId", evt.getTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getTaskInfo().getConfiguration().getTypeName());
    putIfNotNull(data, "taskMessage", evt.getTaskInfo().getConfiguration().getMessage());
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
}
