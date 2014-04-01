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
package org.sonatype.nexus.proxy.repository.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusExecutorService;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Named
public class DefaultThreadPoolManager
    implements ThreadPoolManager
{
  private static final int REPOSITORY_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
      "nexus.repositoryThreadPoolSize", 50);
  
  private final EventBus eventBus;

  private final NexusExecutorService repositoryThreadPool;

  @Inject
  public DefaultThreadPoolManager(final EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
    // direct hand-off used! Proxy pool will use caller thread to execute the task when full!
    final ThreadPoolExecutor target =
        new ThreadPoolExecutor(0, REPOSITORY_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new NexusThreadFactory("repo", "Repository TPool"),
            new CallerRunsPolicy());

    this.repositoryThreadPool = NexusExecutorService.forCurrentSubject(target);
    eventBus.register(this);
  }

  @Override
  public ExecutorService getRepositoryThreadPool(Repository repository) {
    return repositoryThreadPool;
  }

  @Subscribe
  public void on(final NexusStoppedEvent e) {
    eventBus.unregister(this);
    terminatePool(repositoryThreadPool);
  }

  // ==

  private void terminatePool(final ExecutorService executorService) {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }
}
