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

package org.sonatype.nexus.timeline.feeds;

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.NexusTimeline;

import com.google.common.base.Predicate;

/**
 * A recorder for events for later retrieval. The Actions are "generic" Nexus event related. This component
 * provides a slight abstraction over {@link NexusTimeline}, that is used by it.
 *
 * @since 3.0
 */
public interface FeedRecorder
{
  // creating

  void addNexusArtifactEvent(NexusArtifactEvent evt);

  void addSystemEvent(SystemEvent evt);

  void addAuthcAuthzEvent(AuthcAuthzEvent evt);

  // tasks

  TaskEvent taskStarted(String action, String message);

  void taskFinished(TaskEvent prc, String finishMessage);

  void taskCanceled(TaskEvent prc, String cancelMessage);

  void taskBroken(TaskEvent prc, Throwable e);

  // reading

  List<NexusArtifactEvent> getNexusArtifectEvents(Set<String> subtypes, Integer from, Integer count,
                                                  Predicate<Entry> filter);

  List<SystemEvent> getSystemEvents(Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter);

  List<AuthcAuthzEvent> getAuthcAuthzEvents(Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter);
}
