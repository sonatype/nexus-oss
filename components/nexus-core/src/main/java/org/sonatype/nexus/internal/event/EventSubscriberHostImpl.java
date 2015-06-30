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
package org.sonatype.nexus.internal.event;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.event.Asynchronous;
import org.sonatype.nexus.common.event.EventSubscriber;
import org.sonatype.nexus.common.event.EventSubscriberHost;
import org.sonatype.nexus.common.property.SystemPropertiesHelper;
import org.sonatype.nexus.thread.NexusExecutorService;
import org.sonatype.nexus.thread.NexusThreadFactory;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link EventSubscriberHost}.
 */
@Named
@Singleton
public class EventSubscriberHostImpl
    extends LifecycleSupport
    implements EventSubscriberHost
{
  private final int HOST_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
      EventSubscriberHostImpl.class.getName() + ".poolSize", 500);

  private final EventBus eventBus;

  private final List<Provider<EventSubscriber>> eventSubscriberProviders;

  private final NexusExecutorService hostThreadPool;

  private final com.google.common.eventbus.AsyncEventBus asyncBus;

  @Inject
  public EventSubscriberHostImpl(final EventBus eventBus,
                                 final List<Provider<EventSubscriber>> eventSubscriberProviders)
  {
    this.eventBus = checkNotNull(eventBus);
    this.eventSubscriberProviders = checkNotNull(eventSubscriberProviders);

    // direct hand-off used! Host pool will use caller thread to execute async inspectors when pool full!
    final ThreadPoolExecutor target =
        new ThreadPoolExecutor(0, HOST_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new NexusThreadFactory("esh", "Event Subscriber Host"), new CallerRunsPolicy());
    this.hostThreadPool = NexusExecutorService.forCurrentSubject(target);
    this.asyncBus = new com.google.common.eventbus.AsyncEventBus("esh-async", hostThreadPool);

    eventBus.register(this);
    log.info("Initialized");
  }

  @Override
  protected void doStart() throws Exception {
    for (Provider<EventSubscriber> eventSubscriberProvider : eventSubscriberProviders) {
      EventSubscriber es = null;
      try {
        es = eventSubscriberProvider.get();
        register(es);
      }
      catch (Exception e) {
        log.warn("Could not register: {}", es, e);
      }
    }
  }

  @Override
  protected void doStop() throws Exception {
    eventBus.unregister(this);

    for (Provider<EventSubscriber> eventSubscriberProvider : eventSubscriberProviders) {
      EventSubscriber es = null;
      try {
        es = eventSubscriberProvider.get();
        unregister(es);
      }
      catch (Exception e) {
        log.warn("Could not unregister: {}", es, e);
      }
    }

    // we need clean shutdown, wait all background event inspectors to finish to have consistent state
    hostThreadPool.shutdown();
    try {
      hostThreadPool.awaitTermination(5L, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      log.debug("Interrupted while waiting for termination", e);
    }
  }

  private void register(final Object object) {
    if (object instanceof Asynchronous) {
      asyncBus.register(object);
    }
    else {
      eventBus.register(object);
    }
    log.trace("Registered {}", object);
  }

  private void unregister(final Object object) {
    if (object instanceof Asynchronous) {
      asyncBus.unregister(object);
    }
    else {
      eventBus.unregister(object);
    }
    log.trace("Unregistered {}", object);
  }

  /**
   * Used by UTs and ITs only, to "wait for calm period", when all the async event inspectors finished.
   */
  @Override
  @VisibleForTesting
  public boolean isCalmPeriod() {
    // "calm period" is when we have no queued nor active threads
    return ((ThreadPoolExecutor) hostThreadPool.getTargetExecutorService()).getQueue().isEmpty()
        && ((ThreadPoolExecutor) hostThreadPool.getTargetExecutorService()).getActiveCount() == 0;
  }

  // FIXME: Sort out what actually uses this?

  @Subscribe
  @AllowConcurrentEvents
  public void onEvent(final Object evt) {
    asyncBus.post(evt);
  }
}
