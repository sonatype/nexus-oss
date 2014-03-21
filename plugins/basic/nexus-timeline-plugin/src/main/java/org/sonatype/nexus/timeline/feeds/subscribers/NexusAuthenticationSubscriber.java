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

package org.sonatype.nexus.timeline.feeds.subscribers;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class NexusAuthenticationSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  private final NexusConfiguration nexusConfiguration;

  private volatile NexusAuthenticationEvent lastNexusAuthenticationEvent;

  @Inject
  public NexusAuthenticationSubscriber(final FeedRecorder feedRecorder,
                                       final NexusConfiguration nexusConfiguration)
  {
    super(feedRecorder);
    this.nexusConfiguration = checkNotNull(nexusConfiguration);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusAuthenticationEvent nae) {
    if (!isRecordedEvent(nae)) {
      // do nothing
      return;
    }

    lastNexusAuthenticationEvent = nae;

    final ClientInfo ai = nae.getClientInfo();

    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "success", String.valueOf(nae.isSuccessful()));
    putIfNotNull(data, "user.id", ai.getUserid());
    putIfNotNull(data, "user.ip", ai.getRemoteIP());
    putIfNotNull(data, "user.ua", ai.getUserAgent());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_AUTH,
        FeedRecorder.AUTH_AUTHC,
        nae.getClass().getName(),
        nae.getEventDate(),
        null, // "system"
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  // ==

  protected boolean isRecordedEvent(final NexusAuthenticationEvent nae) {
    // we record everything except anonymous related ones
    if (StringUtils.equals(nexusConfiguration.getAnonymousUsername(), nae.getClientInfo().getUserid())) {
      return false;
    }

    // if here, we record the event if not similar to previous one
    return !isSimilarEvent(nae);
  }

  protected boolean isSimilarEvent(final NexusAuthenticationEvent nae) {
    // event is similar (to previously processed one) if there was previously processed at all, the carried
    // AuthenticationItem equals to the one carried by previously processed one, and the events happened in range
    // less than 2 seconds
    if (lastNexusAuthenticationEvent != null
        && (lastNexusAuthenticationEvent.isSuccessful() == nae.isSuccessful())
        && lastNexusAuthenticationEvent.getClientInfo().equals(nae.getClientInfo())
        && (System.currentTimeMillis() - lastNexusAuthenticationEvent.getEventDate().getTime() < 2000L)) {
      return true;
    }

    return false;
  }
}
