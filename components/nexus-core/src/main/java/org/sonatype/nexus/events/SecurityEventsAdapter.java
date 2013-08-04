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

package org.sonatype.nexus.events;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.events.AuthorizationConfigurationChanged;
import org.sonatype.security.events.AuthorizationConfigurationChangedEvent;
import org.sonatype.security.events.SecurityConfigurationChanged;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;
import org.sonatype.security.events.UserPrincipalsExpired;
import org.sonatype.security.events.UserPrincipalsExpiredEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts new security system events to old ones and vice-versa.
 *
 * @since 2.3
 */
@Named
@Singleton
@EventBus.Managed
public class SecurityEventsAdapter
{

  private final EventBus eventBus;

  @Inject
  public SecurityEventsAdapter(final EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final AuthorizationConfigurationChanged event) {
    eventBus.post(new AuthorizationConfigurationChangedEvent(this));
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final SecurityConfigurationChanged event) {
    eventBus.post(new SecurityConfigurationChangedEvent(this));
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final UserPrincipalsExpired event) {
    eventBus.post(new UserPrincipalsExpiredEvent(this, event.getUserId(), event.getSource()));
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final AuthorizationConfigurationChangedEvent event) {
    if (!this.equals(event.getEventSender())) {
      eventBus.post(new AuthorizationConfigurationChanged());
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final SecurityConfigurationChangedEvent event) {
    if (!this.equals(event.getEventSender())) {
      eventBus.post(new SecurityConfigurationChanged());
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void on(final UserPrincipalsExpiredEvent event) {
    if (!this.equals(event.getEventSender())) {
      eventBus.post(new UserPrincipalsExpired(event.getUserId(), event.getSource()));
    }
  }

}
