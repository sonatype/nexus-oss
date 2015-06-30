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

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.base.Predicate;

public class DummyFeedRecorder
    implements FeedRecorder
{
  int receivedEventCount = 0;

  public int getReceivedEventCount() {
    return receivedEventCount;
  }

  @Override
  public void addEvent(final FeedEvent entry) {
    receivedEventCount++;
  }

  @Override
  public List<FeedEvent> getEvents(final Set<String> types, final Set<String> subtypes, final int from, final int count,
                                   final Predicate<Entry> filter)
  {
    return null;
  }
}
