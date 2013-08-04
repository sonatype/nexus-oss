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

package org.sonatype.nexus.proxy.repository.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusExecutorService;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

@Component(role = ThreadPoolManager.class)
public class DefaultThreadPoolManager
    implements ThreadPoolManager, Disposable
{
  private static final int GROUP_REPOSITORY_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
      "nexus.groupRepositoryThreadPoolSize", 200);

  private static final int PROXY_REPOSITORY_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
      "nexus.proxyRepositoryThreadPoolSize", 50);

  private final NexusExecutorService groupRepositoryThreadPool;

  private final NexusExecutorService proxyRepositoryThreadPool;

  public DefaultThreadPoolManager() {
    // direct hand-off used! Group pool will use caller thread to execute the task when full!
    final ThreadPoolExecutor gTarget =
        new ThreadPoolExecutor(0, GROUP_REPOSITORY_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new NexusThreadFactory("group", "Group TPool"),
            new CallerRunsPolicy());

    // direct hand-off used! Proxy pool will use caller thread to execute the task when full!
    final ThreadPoolExecutor pTarget =
        new ThreadPoolExecutor(0, PROXY_REPOSITORY_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new NexusThreadFactory("proxy", "Proxy TPool"),
            new CallerRunsPolicy());

    this.groupRepositoryThreadPool = NexusExecutorService.forCurrentSubject(gTarget);
    this.proxyRepositoryThreadPool = NexusExecutorService.forCurrentSubject(pTarget);
  }

  @Override
  public ExecutorService getRepositoryThreadPool(Repository repository) {
    if (repository.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
      return groupRepositoryThreadPool;
    }
    else if (repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
      return proxyRepositoryThreadPool;
    }
    else {
      return null;
    }
  }

  @Override
  public synchronized void createPool(Repository repository) {
    // nop for now
  }

  @Override
  public synchronized void removePool(Repository repository) {
    // nop for now
  }

  public synchronized void shutdown() {
    terminatePool(groupRepositoryThreadPool);
    terminatePool(proxyRepositoryThreadPool);
  }

  @Override
  public void dispose() {
    shutdown();
  }

  // ==

  protected void terminatePool(final ExecutorService executorService) {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }
}
