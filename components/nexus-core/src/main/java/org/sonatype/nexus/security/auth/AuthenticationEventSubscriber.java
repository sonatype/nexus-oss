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
package org.sonatype.nexus.security.auth;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.security.events.AuthenticationEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Forwards {@link AuthenticationEvent} as {@link NexusAuthenticationEvent}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AuthenticationEventSubscriber
    implements EventSubscriber
{

  private final Provider<EventBus> eventBus;

  private final Provider<ClientInfoProvider> clientInfoProvider;

  @Inject
  public AuthenticationEventSubscriber(final Provider<EventBus> eventBus,
                                       final Provider<ClientInfoProvider> clientInfoProvider)
  {
    this.eventBus = checkNotNull(eventBus);
    this.clientInfoProvider = checkNotNull(clientInfoProvider);
  }

  @Subscribe
  public void on(final AuthenticationEvent event) {
    ClientInfo clientInfo = clientInfoProvider.get().getCurrentThreadClientInfo();
    eventBus.get().post(new NexusAuthenticationEvent(
        this,
        clientInfo == null
            ? new ClientInfo(event.getUserId(), null, null)
            : new ClientInfo(event.getUserId(), clientInfo.getRemoteIP(), clientInfo.getUserAgent()),
        event.isSuccessful()
    ));
  }

}
