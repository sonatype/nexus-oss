/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.simple.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.realms.tools.DynamicSecurityResource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages dynamic {@code repository-instance} permissions for {@code simple} format.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DynamicSecurityResourceImpl
    implements DynamicSecurityResource
{
  private final SecurityModelConfiguration model = new Configuration();

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private volatile boolean dirty = true;

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public SecurityModelConfiguration getConfiguration() {
    Lock lock = readWriteLock.readLock();
    lock.lock();
    try {
      dirty = false;
      return model;
    }
    finally {
      lock.unlock();
    }
  }

  public static interface Customizer
  {
    void apply(SecurityModelConfiguration model);
  }

  public void apply(final Customizer customizer) {
    checkNotNull(customizer);
    Lock lock = readWriteLock.writeLock();
    lock.lock();
    try {
      customizer.apply(model);
      dirty = true;
    }
    finally {
      lock.unlock();
    }
  }
}
