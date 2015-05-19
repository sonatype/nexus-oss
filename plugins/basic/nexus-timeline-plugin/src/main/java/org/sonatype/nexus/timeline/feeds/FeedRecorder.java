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
package org.sonatype.nexus.timeline.feeds;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.timeline.Timeline;

import com.google.common.base.Function;

/**
 * A recorder for events and their retrieval. The Actions are "generic" Nexus event related. This component
 * provides a slight abstraction over {@link Timeline}, that is used by it. Still, these "families" and
 * "subtypes" below are not by all means the only one possible to be recorded. You can still extend this set
 * by your types and subtypes. There are only the events recoded out of the box by this plugin.
 *
 * @since 3.0
 */
public interface FeedRecorder
{
  // ==

  String FAMILY_SYSTEM = "system";

  String SYSTEM_BOOT = "boot";

  String SYSTEM_CONFIG = "config";

  // ==

  String FAMILY_AUTH = "auth";

  String AUTH_AUTHC = "authc";

  String AUTH_AUTHZ = "authz";

  // ==

  String FAMILY_REPO = "repo";

  String REPO_CREATED = "created";

  String REPO_DROPPED = "dropped";

  String REPO_UPDATED = "updated";

  String REPO_LSTATUS = "local";

  String REPO_PSTATUS = "proxy";

  // ==

  String FAMILY_ASSET = "asset";

  String ASSET_CACHED = "cached";

  String ASSET_CACHED_UPDATE = "cachedU";

  String ASSET_DEPLOYED = "deployed";

  String ASSET_DEPLOYED_UPDATE = "deployedU";

  String ASSET_DELETED = "deleted";

  // ==

  String FAMILY_COMPONENT = "component";

  String COMPONENT_CACHED = "cached";

  String COMPONENT_CACHED_UPDATE = "cachedU";

  String COMPONENT_DEPLOYED = "deployed";

  String COMPONENT_DEPLOYED_UPDATE = "deployedU";

  String COMPONENT_DELETED = "deleted";

  // ==

  String FAMILY_TASK = "task";

  String TASK_STARTED = "started";

  String TASK_FINISHED = "finished";

  String TASK_CANCELED = "canceled";

  String TASK_FAILED = "failed";

  // creating

  /**
   * Adds feed event to timeline.
   */
  void addEvent(FeedEvent entry);

  /**
   * Retrieves feed events from timeline.
   *
   * @param types    to filter for types or {@code null}.
   * @param subtypes to filter for subtypes or {@code null}.
   * @param from     paging, from
   * @param count    paging, to
   * @param function custom transormation or {@code null}. Useful to fill in parts of data NOT stored in timeline (or
   *                 composed from data present), like generated content, event title etc.
   */
  List<FeedEvent> getEvents(@Nullable Set<String> types,
                            @Nullable Set<String> subtypes,
                            int from,
                            int count,
                            @Nullable Function<FeedEvent, FeedEvent> function);
}
