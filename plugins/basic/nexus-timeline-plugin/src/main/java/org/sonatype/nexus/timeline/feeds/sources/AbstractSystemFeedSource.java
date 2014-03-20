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

package org.sonatype.nexus.timeline.feeds.sources;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.tasks.DeleteRepositoryFoldersTask;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.SystemEvent;
import org.sonatype.nexus.timeline.tasks.PurgeTimeline;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import org.restlet.data.MediaType;

/**
 * The system changes feed.
 *
 * @author cstamas
 */
public abstract class AbstractSystemFeedSource
    extends AbstractFeedSource
{
  protected AbstractSystemFeedSource(final ApplicationStatusSource applicationStatusSource,
                                     final FeedRecorder feedRecorder, final String feedKey,
                                     final String feedName, final String feedDescription)
  {
    super(applicationStatusSource, feedRecorder, feedKey, feedName, feedDescription);
  }

  protected abstract List<SystemEvent> getEventList(Integer from, Integer count, Map<String, String> params);

  @Override
  protected void fillInEntries(final SyndFeed feed, final Integer from, final Integer count,
                               final Map<String, String> params)
  {
    final List<SystemEvent> items = getEventList(from, count, params);
    final List<SyndEntry> entries = Lists.newArrayListWithCapacity(items.size());

    for (SystemEvent item : items) {
      String username;
      if (item.getEventContext().containsKey(AccessManager.REQUEST_USER)) {
        username = (String) item.getEventContext().get(AccessManager.REQUEST_USER);
      }
      else {
        username = null;
      }

      String ipAddress;
      if (item.getEventContext().containsKey(AccessManager.REQUEST_REMOTE_ADDRESS)) {
        ipAddress = (String) item.getEventContext().get(AccessManager.REQUEST_REMOTE_ADDRESS);
      }
      else {
        ipAddress = null;
      }
      StringBuilder msg = new StringBuilder(item.getMessage()).append(". ");
      if (username != null) {
        msg.append(" It was initiated by a request from user ").append(username).append(".");
      }
      if (ipAddress != null) {
        msg.append(" The request was originated from IP address ").append(ipAddress).append(".");
      }

      final SyndEntry entry = new SyndEntryImpl();

      if (SystemEvent.ACTION_BOOT.equals(item.getAction())) {
        entry.setTitle("Booting");
      }
      else if (SystemEvent.ACTION_CONFIG.equals(item.getAction())) {
        entry.setTitle("Configuration change");
      }
      else if (SystemEvent.ACTION_REPO_LSTATUS_CHANGES.equals(item.getAction())) {
        entry.setTitle("Repository local status change");
      }
      else if (SystemEvent.ACTION_REPO_PSTATUS_CHANGES.equals(item.getAction())) {
        entry.setTitle("Repository proxy mode change");
      }
      else if (SystemEvent.ACTION_REPO_PSTATUS_AUTO_CHANGES.equals(item.getAction())) {
        entry.setTitle("Repository proxy mode change (user intervention may be needed!)");
      }
      // manually set to not be dependent on it
      else if ("REINDEX".equals(item.getAction())) {
        entry.setTitle("Reindexing");
      }
      else if ("PUBLISHINDEX".equals(item.getAction())) {
        entry.setTitle("Publishing indexes");
      }
      else if ("DOWNLOADINDEX".equals(item.getAction())) {
        entry.setTitle("Downloading Indexes");
      }
      else if ("OPTIMIZE_INDEX".equals(item.getAction())) {
        entry.setTitle("Optimizing Indexes");
      }
      else if ("PUBLISHINDEX".equals(item.getAction())) {
        entry.setTitle("Publishing Indexes");
      }
      else if (RebuildAttributesTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Rebuilding attributes");
      }
      else if (PurgeTimeline.ACTION.equals(item.getAction())) {
        entry.setTitle("Timeline purge");
      }
      else if (ExpireCacheTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Expiring caches");
      }
      else if (EvictUnusedProxiedItemsTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Evicting unused proxied items");
      }
      else if (SnapshotRemoverTask.SYSTEM_REMOVE_SNAPSHOTS_ACTION.equals(item.getAction())) {
        entry.setTitle("Removing snapshots");
      }
      else if (DeleteRepositoryFoldersTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Removing repository folder");
      }
      else if (EmptyTrashTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Emptying Trash");
      }
      else if (SynchronizeShadowsTask.ACTION.equals(item.getAction())) {
        entry.setTitle("Synchronizing Shadow Repository");
      }
      else if (RebuildMavenMetadataTask.REBUILD_MAVEN_METADATA_ACTION.equals(item.getAction())) {
        entry.setTitle("Rebuilding maven metadata files");
      }
      else {
        entry.setTitle(item.getAction());
      }
      final SyndContent content = new SyndContentImpl();
      content.setType(MediaType.TEXT_PLAIN.toString());
      content.setValue(msg.toString());
      entry.setPublishedDate(item.getEventDate());
      entry.setAuthor(feed.getAuthor());
      entry.setLink("/");
      entry.setDescription(content);
      entries.add(entry);
    }
    feed.setEntries(entries);
  }

}
