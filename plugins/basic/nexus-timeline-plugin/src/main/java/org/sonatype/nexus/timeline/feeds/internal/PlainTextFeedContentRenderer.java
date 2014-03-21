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
package org.sonatype.nexus.timeline.feeds.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.rest.FeedContentRenderer;

/**
 * A {@link FeedContentRenderer} that renders feed content as {@link "text/plain"}.
 *
 * @since 3.0
 */
@Singleton
@Named("text/plain")
public class PlainTextFeedContentRenderer
    implements FeedContentRenderer
{
  @Override
  public String getContentType(final FeedEvent evt) {
    return "text/plain";
  }

  @Override
  public String getTitle(final FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    if (evt.getData().containsKey("_title")) {
      return evt.getData().get("_title");
    }
    // TODO: implement this
    return evt.getEventClass();
  }

  @Override
  public String getContent(FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    if (evt.getData().containsKey("_content")) {
      return evt.getData().get("_content");
    }
    // TODO: implement this
    // get the template by
    evt.getEventClass();
    // render template with
    return evt.getData().toString();
  }
}
