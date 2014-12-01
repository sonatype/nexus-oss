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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
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
  public TaskSubscriber(final FeedRecorder feedRecorder)
  {
    super(feedRecorder);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusTaskEventStarted evt) {
    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "taskType", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskId", evt.getNexusTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getNexusTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskMessage", evt.getNexusTaskInfo().getConfiguration().getMessage());
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
    long taskStarted = evt.getNexusTaskInfo().getLastRunState() != null ? evt.getNexusTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskType", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskId", evt.getNexusTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getNexusTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskMessage", evt.getNexusTaskInfo().getConfiguration().getMessage());
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
    long taskStarted = evt.getNexusTaskInfo().getLastRunState() != null ? evt.getNexusTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskType", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskId", evt.getNexusTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getNexusTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskMessage", evt.getNexusTaskInfo().getConfiguration().getMessage());
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
    long taskStarted = evt.getNexusTaskInfo().getLastRunState() != null ? evt.getNexusTaskInfo().getLastRunState().getRunStarted().getTime() : -1;
    putIfNotNull(data, "taskStarted", String.valueOf(taskStarted));
    putIfNotNull(data, "taskFailure", String.valueOf(evt.getFailureCause()));
    putIfNotNull(data, "taskType", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskId", evt.getNexusTaskInfo().getId());
    putIfNotNull(data, "taskName", evt.getNexusTaskInfo().getName());
    putIfNotNull(data, "taskAction", evt.getNexusTaskInfo().getConfiguration().getType());
    putIfNotNull(data, "taskMessage", evt.getNexusTaskInfo().getConfiguration().getMessage());
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
