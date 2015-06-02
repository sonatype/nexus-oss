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
package org.sonatype.nexus.timeline.feeds.sources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.ImmutableSet;

@Named(TaskFeedSource.CHANNEL_KEY)
@Singleton
public class TaskFeedSource
    extends AbstractFeedSource
{
  public static final String CHANNEL_KEY = "taskChanges";

  private static final Set<String> TYPE_SET = ImmutableSet.of(FeedRecorder.FAMILY_TASK);

  @Inject
  public TaskFeedSource(final FeedRecorder feedRecorder) {
    super(feedRecorder,
        CHANNEL_KEY,
        "Scheduled Tasks",
        "Scheduled Tasks activities");
  }

  @Override
  protected List<FeedEvent> getEntries(final int from,
                                       final int count,
                                       final Map<String, Object> params)
  {
    return getFeedRecorder().getEvents(
        TYPE_SET,
        null,
        toWhere(params),
        params,
        from,
        count,
        null
    );
  }

  @Override
  protected String title(final FeedEvent evt) {
    if (FeedRecorder.TASK_STARTED.equals(evt.getEventSubType())) {
      return "Started: " + evt.getData().get("taskName");
    }
    else if (FeedRecorder.TASK_FINISHED.equals(evt.getEventSubType())) {
      return "Finished: " + evt.getData().get("taskName");
    }
    else if (FeedRecorder.TASK_CANCELED.equals(evt.getEventSubType())) {
      return "Canceled: " + evt.getData().get("taskName");
    }
    else if (FeedRecorder.TASK_FAILED.equals(evt.getEventSubType())) {
      return "Failed: " + evt.getData().get("taskName");
    }
    return super.title(evt);
  }
}
