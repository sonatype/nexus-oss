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

package org.sonatype.nexus.feeds.record;

import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component(role = EventInspector.class, hint = "NexusAuthenticationEventInspector")
public class NexusAuthenticationEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
  @Requirement
  private NexusConfiguration nexusConfiguration;

  private volatile NexusAuthenticationEvent lastNexusAuthenticationEvent;

  @Override
  public boolean accepts(Event<?> evt) {
    if (evt instanceof NexusAuthenticationEvent) {
      // We accept only some NexusAuthenticationEvent, see #isRecordedEvent()
      return isRecordedEvent((NexusAuthenticationEvent) evt);
    }
    else {
      return false;
    }
  }

  @Override
  public void inspect(Event<?> evt) {
    final NexusAuthenticationEvent nae = (NexusAuthenticationEvent) evt;

    if (!isRecordedEvent(nae)) {
      // do nothing
      return;
    }

    lastNexusAuthenticationEvent = nae;

    final ClientInfo ai = nae.getClientInfo();

    final String msg =
        String.format("%s user [%s] from IP address %s", (nae.isSuccessful() ? "Successfully authenticated"
            : "Unable to authenticate"), ai.getUserid(), StringUtils.defaultString(ai.getRemoteIP(), "[unknown]"));

    if (getLogger().isDebugEnabled()) {
      getLogger().debug(msg);
    }

    AuthcAuthzEvent aae = new AuthcAuthzEvent(nae.getEventDate(), FeedRecorder.SYSTEM_AUTHC, msg);

    final String ip = ai.getRemoteIP();

    if (ip != null) {
      evt.getEventContext().put(AccessManager.REQUEST_REMOTE_ADDRESS, ip);
    }

    getFeedRecorder().addAuthcAuthzEvent(aae);
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
