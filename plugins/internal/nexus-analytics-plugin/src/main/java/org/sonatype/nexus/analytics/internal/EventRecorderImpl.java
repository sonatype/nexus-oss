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
package org.sonatype.nexus.analytics.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.analytics.EventData;
import org.sonatype.nexus.analytics.EventRecorder;
import org.sonatype.nexus.analytics.EventStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link EventRecorder}.
 *
 * @since 2.8
 */
@Named
@Singleton
public class EventRecorderImpl
  extends ComponentSupport
  implements EventRecorder
{
  private final EventStore store;

  private volatile boolean enabled = false;

  @Inject
  public EventRecorderImpl(final EventStore store) {
    this.store = checkNotNull(store);
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
    log.debug("Enabled: {}", enabled);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void record(final EventData data) {
    checkNotNull(data);

    if (!enabled) {
      // bitch as this may be impacting performance, callers should guard before building event data
      log.warn("Attempting to record analytics data when collection is disabled; ignoring");
      return;
    }

    log.debug("Record: {}", data);
    try {
      store.add(data);
    }
    catch (Exception e) {
      log.warn("Failed to record event data", e);
    }
  }
}
