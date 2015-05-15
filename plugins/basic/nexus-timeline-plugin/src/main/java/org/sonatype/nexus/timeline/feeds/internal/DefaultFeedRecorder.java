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
package org.sonatype.nexus.timeline.feeds.internal;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.internal.EntryRecord;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link FeedRecorder}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultFeedRecorder
    extends ComponentSupport
    implements FeedRecorder
{
  private final Timeline timeline;

  @Inject
  public DefaultFeedRecorder(final Timeline timeline)
  {
    this.timeline = checkNotNull(timeline);
  }

  @Override
  public void addEvent(final FeedEvent evt) {
    final Map<String, String> data = Maps.newHashMap();
    data.putAll(evt.getData());
    data.put("_type", evt.getEventType());
    data.put("_subType", evt.getEventSubType());
    if (evt.getLink() != null) {
      data.put("_link", evt.getLink());
    }
    if (evt.getAuthor() != null) {
      data.put("_author", evt.getAuthor());
    }
    // we use TL internal class
    final EntryRecord record = new EntryRecord(
        evt.getPublished().getTime(),
        evt.getEventType(),
        evt.getEventSubType(),
        data);
    timeline.add(record);
  }

  @Override
  public List<FeedEvent> getEvents(final Set<String> types, final Set<String> subTypes, final int from, final int count,
                                   final Predicate<Entry> filter)
  {
    final List<FeedEvent> result = Lists.newArrayList();
    // TODO: filter by perms
    final TimelineCallback callback = new TimelineCallback()
    {
      @Override
      public boolean processNext(final Entry rec) throws IOException {
        final Map<String, String> data = Maps.newHashMap(rec.getData());
        data.remove("_type");
        data.remove("_subType");
        data.remove("_link");
        data.remove("_author");
        final FeedEvent evt = new FeedEvent(
            rec.getType(),
            rec.getSubType(),
            new Date(rec.getTimestamp()),
            rec.getData().get("_author"), // nullable
            rec.getData().get("_link"), // nullable
            data
        );
        result.add(evt);
        return true;
      }
    };
    timeline.retrieve(from, count, types, subTypes, filter, callback);
    return result;
  }
}
