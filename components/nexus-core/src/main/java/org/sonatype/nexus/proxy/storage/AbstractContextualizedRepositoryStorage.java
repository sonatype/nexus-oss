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

package org.sonatype.nexus.proxy.storage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Common class for repository storage implementations, that share common logic about managing context in
 * thread safe manner.
 *
 * @since 2.7.0
 */
public abstract class AbstractContextualizedRepositoryStorage
    extends ComponentSupport
{
  /**
   * Key used to mark a repository context as "initialized". This flag and the generation together controls how the
   * context is about to be updated. See NEXUS-5145.
   */
  private static final String CONTEXT_UPDATED_KEY =
      AbstractContextualizedRepositoryStorage.class.getName() + ".updated";

  /**
   * Per repository locks that synchronizes context access for same repository.
   */
  private final ConcurrentMap<String, ReentrantLock> repositoryLocks;

  /**
   * Context generations, to track changes to contexts.
   */
  private final Map<String, Integer> repositoryContextGenerations;

  protected AbstractContextualizedRepositoryStorage() {
    repositoryLocks = Maps.newConcurrentMap();
    repositoryContextGenerations = Maps.newConcurrentMap();
  }

  /**
   * @deprecated To support existing storage implementations, but use protected {@link #log} member directly instead.
   */
  @Deprecated
  protected Logger getLogger() {
    return log;
  }

  protected static enum ContextOperation
  {
    INITIALIZE, UPDATE;
  }

  /**
   * Method that returns StorageContext (to be used for reads only!) with a guarantee that it's properly
   * updated if needed. See {@link #updateStorageContext(Repository, StorageContext, ContextOperation)} method, meant
   * to be implemented by storage classes to perform storage specific context WRITE operations.
   */
  protected <C extends StorageContext> C getStorageContext(final Repository repository, final C context)
      throws IOException
  {
    checkNotNull(repository);
    if (context != null) {
      ReentrantLock lock = repositoryLocks.get(repository.getId());
      if (lock == null) {
        final ReentrantLock newlock = new ReentrantLock();
        lock = repositoryLocks.putIfAbsent(repository.getId(), newlock);
        if (lock == null) {
          lock = newlock;
        }
      }
      lock.lock();
      try {
        if (!repositoryContextGenerations.containsKey(repository.getId()) ||
            !context.hasContextObject(CONTEXT_UPDATED_KEY)
            || context.getGeneration() > repositoryContextGenerations.get(repository.getId())) {
          final ContextOperation operation;
          if (!repositoryContextGenerations.containsKey(repository.getId())) {
            operation = ContextOperation.INITIALIZE;
            log.trace("Storage {} is about to initialize context {}", getClass().getSimpleName(), context);
          }
          else {
            operation = ContextOperation.UPDATE;
            log.trace("Storage {} is about to update context {}", getClass().getSimpleName(), context);
          }
          updateStorageContext(repository, context, operation);
          context.putContextObject(CONTEXT_UPDATED_KEY, Boolean.TRUE);
          repositoryContextGenerations.put(repository.getId(), context.getGeneration());
        }
      }
      finally {
        lock.unlock();
      }
    }
    return context;
  }

  /**
   * Method to be implemented by storage implementations, to perform WRITE operations on context in "safe" way: it
   * is guaranteed that this method is invoked only once for given repository at the same time (while concurrent
   * invocation of this method ARE possible with different repositories, hence the implementation should be
   * thread safe).
   *
   * @param repository       to update context for
   * @param context          the context to update
   * @param contextOperation the operation being performed
   * @throws RemoteStorageException If context could not be updated
   */
  protected abstract void updateStorageContext(Repository repository, StorageContext context,
                                               ContextOperation contextOperation)
      throws IOException;

}
