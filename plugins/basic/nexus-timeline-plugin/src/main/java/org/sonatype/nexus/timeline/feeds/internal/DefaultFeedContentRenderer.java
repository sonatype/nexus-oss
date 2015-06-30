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
package org.sonatype.nexus.timeline.feeds.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.rest.FeedContentRenderer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default {@link FeedContentRenderer} honors overrides, and if needed, dispatches to some rendered (for now
 * "plaintext" only).
 * 
 * @since 3.0
 */
@Singleton
@Named
public class DefaultFeedContentRenderer
    extends ComponentSupport
    implements FeedContentRenderer
{
  private final PlaintextRenderer plainTextRenderer;

  @Inject
  public DefaultFeedContentRenderer(final PlaintextRenderer plainTextRenderer) {
    this.plainTextRenderer = checkNotNull(plainTextRenderer, "plainTextRenderer");
  }

  @Override
  public String getContentType(final FeedEvent evt) {
    return "text/plain";
  }

  @Override
  public String getTitle(final FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    final String title = evt.getTitle();
    if (title != null) {
      return title;
    }
    return plainTextRenderer.getTitle(evt);
  }

  @Override
  public String getContent(final FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    final String content = evt.getContent();
    if (content != null) {
      return content;
    }
    return plainTextRenderer.getContent(evt);
  }
}
