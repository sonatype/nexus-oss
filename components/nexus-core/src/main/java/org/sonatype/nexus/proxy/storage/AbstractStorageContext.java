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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.nexus.logging.AbstractLoggingComponent;

/**
 * The abstract storage context.
 *
 * @author cstamas
 */
public abstract class AbstractStorageContext
    extends AbstractLoggingComponent
    implements StorageContext
{
  private final HashMap<String, Object> context;

  private final StorageContext parent;

  private AtomicInteger generation;

  @Deprecated
  private long changeTimestamp;

  protected AbstractStorageContext(StorageContext parent) {
    this.context = new HashMap<String, Object>();
    this.parent = parent;
    this.generation = new AtomicInteger(0);
    this.changeTimestamp = System.currentTimeMillis();
  }

  public synchronized int getGeneration() {
    if (parent != null) {
      return parent.getGeneration() + generation.get();
    }
    return generation.get();
  }

  @Deprecated
  public synchronized long getLastChanged() {
    if (parent != null) {
      final long parentChangeTimestamp = parent.getLastChanged();

      if (parentChangeTimestamp > changeTimestamp) {
        return parentChangeTimestamp;
      }
    }

    return changeTimestamp;
  }

  protected synchronized void incrementGeneration() {
    generation.incrementAndGet();
    changeTimestamp = System.currentTimeMillis();
  }

  public StorageContext getParentStorageContext() {
    return parent;
  }

  public void setParentStorageContext(StorageContext parent) {
    // noop
    getLogger().warn(
        "Class {} uses illegal method invocation, org.sonatype.nexus.proxy.storage.AbstractStorageContext.setParentStorageContext( StorageContext ), please update the code!",
        getClass().getName());
  }

  public Object getContextObject(String key) {
    return getContextObject(key, true);
  }

  public synchronized Object getContextObject(final String key, final boolean fallbackToParent) {
    if (context.containsKey(key)) {
      return context.get(key);
    }
    else if (fallbackToParent && parent != null) {
      return parent.getContextObject(key);
    }
    else {
      return null;
    }
  }

  public synchronized void putContextObject(String key, Object value) {
    final Object previous = context.put(key, value);

    incrementGeneration();
    getLogger().debug("Context entry \"{}\" updated: {} -> {}", new Object[]{key, previous, value});
  }

  public synchronized void removeContextObject(String key) {
    final Object removed = context.remove(key);

    incrementGeneration();
    getLogger().debug("Context entry \"{}\" removed. Existent value: ", key, removed);
  }

  public boolean hasContextObject(String key) {
    return context.containsKey(key);
  }

  @Override
  public String toString() {
    return getClass().getName() + "{generation=" + generation + ", parent=" + parent + "}";
  }
}
