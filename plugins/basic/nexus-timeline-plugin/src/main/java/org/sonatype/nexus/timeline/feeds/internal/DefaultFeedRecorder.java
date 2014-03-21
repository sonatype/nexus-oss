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

package org.sonatype.nexus.timeline.feeds.internal;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.NexusTimeline;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.internal.EntryRecord;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class DefaultFeedRecorder
    extends ComponentSupport
    implements FeedRecorder
{
  private final NexusTimeline nexusTimeline;

  private final NexusItemAuthorizer nexusItemAuthorizer;

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public DefaultFeedRecorder(final NexusTimeline nexusTimeline,
                             final NexusItemAuthorizer nexusItemAuthorizer,
                             final RepositoryRegistry repositoryRegistry)
  {
    this.nexusTimeline = checkNotNull(nexusTimeline);
    this.nexusItemAuthorizer = checkNotNull(nexusItemAuthorizer);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  // filtering

  private boolean filterRecord(final Entry record) {
    final String repoId = record.getData().get("repo.id");
    final String itemPath = record.getData().get("item.path");
    if (repoId == null || itemPath == null) {
      return true;
    }
    try {
      final Repository repo = repositoryRegistry.getRepository(repoId);
      final ResourceStoreRequest req = new ResourceStoreRequest(itemPath);
      return nexusItemAuthorizer.authorizePath(repo, req, Action.read);
    }
    catch (NoSuchRepositoryException e) {
      // Can't get repository for artifact, therefore we can't authorize access, therefore you don't see it
      log.debug("Feed entry contained invalid repository ID={}", repoId, e);
      return false;
    }
  }

  @Override
  public void addEvent(final FeedEvent evt) {
    final Map<String, String> data = Maps.newHashMap();
    data.putAll(evt.getData());
    data.put("_type", evt.getEventType());
    data.put("_subType", evt.getEventSubType());
    data.put("_class", evt.getEventClass());
    if (evt.getLink() != null) {
      data.put("_link", evt.getLink());
    }
    if (evt.getAuthor() != null) {
      data.put("_author", evt.getAuthor());
    }
    final EntryRecord record = new EntryRecord(
        evt.getPublished().getTime(),
        evt.getEventType(),
        evt.getEventSubType(),
        data);
    nexusTimeline.add(record);
  }

  @Override
  public List<FeedEvent> getEvents(final Set<String> types, final Set<String> subTypes, final int from, final int count,
                                   final Predicate<Entry> filter)
  {
    final List<FeedEvent> result = Lists.newArrayList();
    final TimelineCallback callback = new TimelineCallback()
    {
      @Override
      public boolean processNext(final Entry rec) throws IOException {
        if (filterRecord(rec)) {
          final Map<String, String> data = Maps.newHashMap(rec.getData());
          data.remove("_type");
          data.remove("_subType");
          data.remove("_class");
          data.remove("_link");
          data.remove("_author");
          final FeedEvent evt = new FeedEvent(
              rec.getType(),
              rec.getSubType(),
              rec.getData().get("_class"),
              new Date(rec.getTimestamp()),
              rec.getData().get("_link"), // nullable
              rec.getData().get("_author"), // nullable
              data
          );
          result.add(evt);
        }
        return true;
      }
    };
    nexusTimeline.retrieve(from, count, types, subTypes, filter, callback);
    return result;
  }
}
