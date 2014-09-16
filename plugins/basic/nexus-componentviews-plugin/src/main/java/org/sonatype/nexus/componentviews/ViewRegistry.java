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
package org.sonatype.nexus.componentviews;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An in-memory container for all of the views currently available for handling requests.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ViewRegistry
{
  private final ConcurrentHashMap<String, View> activeViews = new ConcurrentHashMap<>();

  private final EventBus eventBus;

  @Inject
  public ViewRegistry(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void registerView(View view) {
    checkNotNull(view);

    final View alreadyBoundView = activeViews.putIfAbsent(view.getName(), view);

    checkState(alreadyBoundView == null, "A view is already bound to name %s", view.getName());

    eventBus.post(new ViewRegisteredEvent(this, view));
  }

  public View findViewByName(String viewName) {
    return activeViews.get(viewName);
  }

  /**
   * Unregistraters a view and fires a {@link ViewUnregisteredEvent}.
   */
  public void unregisterView(View view) {
    checkNotNull(view);

    final View remove = activeViews.remove(view.getName());

    eventBus.post(new ViewUnregisteredEvent(this, view));
  }

  @Nullable
  public View getView(String viewName) {
    return activeViews.get(viewName);
  }
}
