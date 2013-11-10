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

package org.sonatype.nexus.proxy.walker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskUtil;

public class DefaultWalkerContext
    implements WalkerContext
{
  private final Repository resourceStore;

  private final WalkerFilter walkerFilter;

  private final ResourceStoreRequest request;

  private final WalkerThrottleController throttleController;

  private Map<String, Object> context;

  private List<WalkerProcessor> processors;

  private Throwable stopCause;

  private Comparator<StorageItem> itemComparator;

  private volatile boolean running;

  public DefaultWalkerContext(Repository store, ResourceStoreRequest request) {
    this(store, request, null);
  }

  public DefaultWalkerContext(Repository store, ResourceStoreRequest request, WalkerFilter filter) {
    this(store, request, filter, true);
  }

  public DefaultWalkerContext(Repository store, ResourceStoreRequest request, WalkerFilter filter,
                              boolean localOnly)
  {
    super();

    this.resourceStore = store;

    this.request = request;

    this.walkerFilter = filter;

    this.running = true;

    if (request.getRequestContext().containsKey(WalkerThrottleController.CONTEXT_KEY, false)) {
      this.throttleController =
          (WalkerThrottleController) request.getRequestContext().get(WalkerThrottleController.CONTEXT_KEY, false);
    }
    else {
      this.throttleController = WalkerThrottleController.NO_THROTTLING;
    }
  }

  @Override
  public boolean isLocalOnly() {
    return request.isRequestLocalOnly();
  }

  @Override
  public Map<String, Object> getContext() {
    if (context == null) {
      context = new HashMap<String, Object>();
    }
    return context;
  }

  @Override
  public List<WalkerProcessor> getProcessors() {
    if (processors == null) {
      processors = new ArrayList<WalkerProcessor>();
    }

    return processors;
  }

  @Override
  public WalkerFilter getFilter() {
    return walkerFilter;
  }

  @Override
  public Repository getRepository() {
    return resourceStore;
  }

  @Override
  public ResourceStoreRequest getResourceStoreRequest() {
    return request;
  }

  @Override
  public boolean isStopped() {
    try {
      TaskUtil.checkInterruption();
    }
    catch (TaskInterruptedException e) {
      if (stopCause == null) {
        stopCause = e;
      }

      running = false;
    }

    return !running;
  }

  @Override
  public Throwable getStopCause() {
    return stopCause;
  }

  @Override
  public void stop(Throwable cause) {
    running = false;

    stopCause = cause;
  }

  @Override
  public WalkerThrottleController getThrottleController() {
    return this.throttleController;
  }

  public Comparator<StorageItem> getItemComparator() {
    return itemComparator;
  }

  public void setItemComparator(final Comparator<StorageItem> itemComparator) {
    this.itemComparator = itemComparator;
  }

}
