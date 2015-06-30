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

import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractFeedEventSubscriber
    extends ComponentSupport
{
  private final FeedRecorder feedRecorder;

  protected AbstractFeedEventSubscriber(final FeedRecorder feedRecorder) {
    this.feedRecorder = checkNotNull(feedRecorder);
  }

  protected FeedRecorder getFeedRecorder() {
    return feedRecorder;
  }

  protected void putIfNotNull(final Map<String, String> map, final String key, final String value) {
    checkNotNull(map);
    checkNotNull(key);
    if (value != null && !value.trim().isEmpty()) {
      map.put(key, value);
    } else {
      map.put(key, "n/a");
    }
  }
}
