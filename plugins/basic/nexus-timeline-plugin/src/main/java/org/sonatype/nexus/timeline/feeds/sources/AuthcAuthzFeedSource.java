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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.ImmutableSet;

/**
 * Authentication and authorization related feed source.
 *
 * @since 3.0
 */
@Named(AuthcAuthzFeedSource.CHANNEL_KEY)
@Singleton
public class AuthcAuthzFeedSource
    extends AbstractFeedSource
{
  public static final String CHANNEL_KEY = "authcAuthz";

  private static final Set<String> TYPE_SET = ImmutableSet.of(FeedRecorder.FAMILY_AUTH);

  @Inject
  public AuthcAuthzFeedSource(final FeedRecorder feedRecorder) {
    super(
        feedRecorder,
        CHANNEL_KEY,
        "Authentication and Authorization",
        "Login and Access activities");
  }

  @Override
  protected List<FeedEvent> getEntries(final int from,
                                       final int count,
                                       final Map<String, Object> params)
      throws IOException
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

  /**
   * Formats entry title for event.
   */
  @Override
  protected String title(FeedEvent evt) {
    if (FeedRecorder.AUTH_AUTHC.equals(evt.getEventSubType())) {
      return "Authentication";
    }
    else if (FeedRecorder.AUTH_AUTHZ.equals(evt.getEventSubType())) {
      return "Authorization";
    }
    return super.title(evt);
  }
}
