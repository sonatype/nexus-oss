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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.inject.Mediator;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.threads.NexusExecutorService;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Key;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default host for {@link EventSubscriber}.
 *
 * @since 2.7.0
 */
@Named
@EagerSingleton
public class EventSubscriberHost
    extends AbstractLoggingComponent
{
  private final int HOST_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
      EventSubscriberHost.class.getName() + ".poolSize", 500);

  private final EventBus eventBus;

  private final NexusExecutorService hostThreadPool;

  private final com.google.common.eventbus.AsyncEventBus asyncBus;

  @Inject
  public EventSubscriberHost(final EventBus eventBus, final BeanLocator beanLocator) {
    this.eventBus = checkNotNull(eventBus);

    // direct hand-off used! Host pool will use caller thread to execute async inspectors when pool full!
    final ThreadPoolExecutor target =
        new ThreadPoolExecutor(0, HOST_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new NexusThreadFactory("esh", "Event Subscriber Host"), new CallerRunsPolicy());
    this.hostThreadPool = NexusExecutorService.forCurrentSubject(target);
    this.asyncBus = new com.google.common.eventbus.AsyncEventBus("esh-async", hostThreadPool);
    beanLocator.watch(Key.get(EventSubscriber.class), new EventSubscriberMediator(), this);
    beanLocator.watch(Key.get(EventInspector.class), new EventInspectorMediator(), this);
    eventBus.register(this);
  }

  public void shutdown() {
    eventBus.unregister(this);
    // we need clean shutdown, wait all background event inspectors to finish to have consistent state
    hostThreadPool.shutdown();
    try {
      hostThreadPool.awaitTermination(5L, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      getLogger().debug("Interrupted while waiting for termination", e);
    }
  }

  public void register(final Object object) {
    if (object instanceof Asynchronous) {
      asyncBus.register(object);
    }
    else {
      eventBus.register(object);
    }
    getLogger().trace("Registered {}", object);
  }

  public void unregister(final Object object) {
    if (object instanceof Asynchronous) {
      asyncBus.unregister(object);
    }
    else {
      eventBus.unregister(object);
    }
    getLogger().trace("Unregistered {}", object);
  }

  /**
   * Used by UTs and ITs only, to "wait for calm period", when all the async event inspectors finished.
   */
  @VisibleForTesting
  public boolean isCalmPeriod() {
    // "calm period" is when we have no queued nor active threads
    return ((ThreadPoolExecutor) hostThreadPool.getTargetExecutorService()).getQueue().isEmpty()
        && ((ThreadPoolExecutor) hostThreadPool.getTargetExecutorService()).getActiveCount() == 0;
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onEvent(final Object evt) {
    asyncBus.post(evt);
    if (evt instanceof NexusStoppedEvent) {
      shutdown();
    }
  }

  // == EventSubscriber support

  public static class EventSubscriberMediator
      implements Mediator<Named, EventSubscriber, EventSubscriberHost>
  {
    @Override
    public void add(final BeanEntry<Named, EventSubscriber> entry, final EventSubscriberHost watcher) throws Exception {
      try {
        watcher.register(entry.getValue());
      }
      catch (Exception e) {
        // NEXUS-4775 Guice exception trying to resolve circular dependencies too early
      }
    }

    @Override
    public void remove(final BeanEntry<Named, EventSubscriber> entry, final EventSubscriberHost watcher)
        throws Exception
    {
      try {
        watcher.unregister(entry.getValue());
      }
      catch (Exception e) {
        // NEXUS-4775 Guice exception trying to resolve circular dependencies too early
      }
    }
  }

  // == Legacy EventInspector support

  private static final ConcurrentMap<String, EventInspectorSubscriberAdapter> adapters = Maps.newConcurrentMap();

  public static class EventInspectorMediator
      implements Mediator<Named, EventInspector, EventSubscriberHost>
  {
    @Override
    public void add(final BeanEntry<Named, EventInspector> entry, final EventSubscriberHost watcher) throws Exception {
      final EventInspectorSubscriberAdapter adapter;
      if (Asynchronous.class.isAssignableFrom(entry.getImplementationClass())) {
        adapter = new AsynchronousEventInspectorSubscriberAdapter(entry);
      }
      else {
        adapter = new EventInspectorSubscriberAdapter(entry);
      }
      adapters.put(entry.getKey().value(), adapter);
      watcher.register(adapter);
    }

    @Override
    public void remove(final BeanEntry<Named, EventInspector> entry, final EventSubscriberHost watcher)
        throws Exception
    {
      final EventInspectorSubscriberAdapter adapter = adapters.get(entry.getKey().value());
      if (adapter != null) {
        watcher.unregister(adapter);
      }
    }
  }

  public static class EventInspectorSubscriberAdapter
  {
    private final BeanEntry<Named, EventInspector> eventInspectorEntry;

    public EventInspectorSubscriberAdapter(final BeanEntry<Named, EventInspector> eventInspectorEntry) {
      this.eventInspectorEntry = checkNotNull(eventInspectorEntry);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(Event<?> event) {
      try {
        final EventInspector ei = eventInspectorEntry.getValue();
        if (ei.accepts(event)) {
          ei.inspect(event);
        }
      }
      catch (Exception e) {
        // nop, guice might NPE
      }
    }

    @Override
    public String toString() {
      return "EIAdapter(" + eventInspectorEntry.getImplementationClass().getName() + ")";
    }
  }

  public static class AsynchronousEventInspectorSubscriberAdapter
      extends EventInspectorSubscriberAdapter
      implements Asynchronous
  {
    public AsynchronousEventInspectorSubscriberAdapter(final BeanEntry<Named, EventInspector> eventInspectorEntry) {
      super(eventInspectorEntry);
    }

    @Override
    public String toString() {
      return super.toString() + " (async)";
    }
  }

}
