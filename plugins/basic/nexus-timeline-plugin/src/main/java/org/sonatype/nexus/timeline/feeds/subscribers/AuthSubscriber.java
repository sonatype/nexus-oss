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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.event.Asynchronous;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.security.ClientInfo;
import org.sonatype.nexus.security.anonymous.AnonymousManager;
import org.sonatype.nexus.security.authc.NexusAuthenticationEvent;
import org.sonatype.nexus.security.authz.NexusAuthorizationEvent;
import org.sonatype.nexus.security.authz.ResourceInfo;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subscriber listening for events recorded under {@link FeedRecorder#FAMILY_AUTH} event type.
 */
@Named
@Singleton
public class AuthSubscriber
    extends AbstractFeedEventSubscriber
    implements EventSubscriber, Asynchronous
{
  private final AnonymousManager anonymousManager;

  private volatile NexusAuthenticationEvent lastNexusAuthenticationEvent;

  private volatile NexusAuthorizationEvent lastNexusAuthorizationEvent;

  @Inject
  public AuthSubscriber(final AnonymousManager anonymousManager,
                        final FeedRecorder feedRecorder)
  {
    super(feedRecorder);
    this.anonymousManager = checkNotNull(anonymousManager);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final NexusAuthenticationEvent nae) {
    if (!isRecordedAuthcEvent(nae)) {
      // do nothing
      return;
    }

    lastNexusAuthenticationEvent = nae;

    final ClientInfo ai = nae.getClientInfo();

    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "success", String.valueOf(nae.isSuccessful()));
    putIfNotNull(data, "userId", ai.getUserid());
    putIfNotNull(data, "userIp", ai.getRemoteIP());
    putIfNotNull(data, "userUa", ai.getUserAgent());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_AUTH,
        FeedRecorder.AUTH_AUTHC,
        nae.getEventDate(),
        null, // "system"
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(NexusAuthorizationEvent nae) {
    if (!isRecordedAuthzEvent(nae)) {
      // do nothing
      return;
    }
    lastNexusAuthorizationEvent = nae;

    final ClientInfo ai = nae.getClientInfo();
    final ResourceInfo ri = nae.getResourceInfo();

    final Map<String, String> data = Maps.newHashMap();
    putIfNotNull(data, "success", String.valueOf(nae.isSuccessful()));
    putIfNotNull(data, "userId", ai.getUserid());
    putIfNotNull(data, "userIp", ai.getRemoteIP());
    putIfNotNull(data, "userUa", ai.getUserAgent());
    putIfNotNull(data, "resProto", ri.getAccessProtocol());
    putIfNotNull(data, "resMethod", ri.getAccessMethod());
    putIfNotNull(data, "resAction", ri.getAction());
    putIfNotNull(data, "resUri", ri.getAccessedUri());
    final FeedEvent fe = new FeedEvent(
        FeedRecorder.FAMILY_AUTH,
        FeedRecorder.AUTH_AUTHZ,
        nae.getEventDate(),
        null, // "system"
        "/", // link to UI
        data
    );
    getFeedRecorder().addEvent(fe);
  }

  // ==

  protected boolean isRecordedAuthcEvent(final NexusAuthenticationEvent nae) {
    // we record everything except anonymous related ones
    if (StringUtils.equals(anonymousManager.getConfiguration().getUserId(), nae.getClientInfo().getUserid())) {
      return false;
    }

    // if here, we record the event if not similar to previous one
    return !isSimilarAuthcEvent(nae);
  }

  protected boolean isSimilarAuthcEvent(final NexusAuthenticationEvent nae) {
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

  protected boolean isRecordedAuthzEvent(final NexusAuthorizationEvent nae) {
    // we record only authz failures
    if (nae.isSuccessful()) {
      return false;
    }
    // we record everything except anonymous related ones
    if (StringUtils.equals(anonymousManager.getConfiguration().getUserId(), nae.getClientInfo().getUserid())) {
      return false;
    }

    // if here, we record the event if not similar to previous one
    return !isSimilarAuthzEvent(nae);
  }

  protected boolean isSimilarAuthzEvent(final NexusAuthorizationEvent nae) {
    // event is similar (to previously processed one) if there was previously processed at all, the carried
    // AuthenticationItem equals to the one carried by previously processed one, and the events happened in range
    // less than 2 seconds
    if (lastNexusAuthorizationEvent != null
        && lastNexusAuthorizationEvent.getClientInfo().equals(nae.getClientInfo())
        && lastNexusAuthorizationEvent.getResourceInfo().equals(nae.getResourceInfo())
        && (System.currentTimeMillis() - lastNexusAuthorizationEvent.getEventDate().getTime() < 2000L)) {
      return true;
    }

    return false;
  }
}
