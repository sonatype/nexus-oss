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

package org.sonatype.nexus.compat;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 2.3
 */
@Named
@Singleton
public class EventBusApplicationEventMulticaster
    implements ApplicationEventMulticaster
{

  private final EventBus eventBus;

  private Map<EventListener, Adapter> adapters;

  @Inject
  public EventBusApplicationEventMulticaster(final EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
    this.adapters = Maps.newIdentityHashMap();
  }

  @Override
  public void addEventListener(final EventListener listener) {
    if (listener != null) {
      final Adapter adapter = new Adapter(listener);
      adapters.put(listener, adapter);
      eventBus.register(adapter);
    }
  }

  @Override
  public void removeEventListener(final EventListener listener) {
    if (listener != null) {
      final Adapter adapter = adapters.remove(listener);
      if (adapter != null) {
        eventBus.unregister(adapter);
      }
    }
  }

  @Override
  public void notifyEventListeners(final Event<?> evt) {
    eventBus.post(evt);
  }

  public static class Adapter
  {

    private final EventListener listener;

    private Adapter(final EventListener listener) {
      this.listener = listener;
    }

    @AllowConcurrentEvents
    @Subscribe
    public void forward(final Event<?> evt) {
      listener.onEvent(evt);
    }

    @Override
    public String toString() {
      return "Adapter for: " + listener;
    }

  }

}
