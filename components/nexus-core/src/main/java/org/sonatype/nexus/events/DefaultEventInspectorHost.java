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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.plexus.appevents.Event;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it. For ones implementing {@link AsynchronousEventInspector} a cached
 * thread pool is used to execute them in a separate thread. In case of pool saturation, the caller thread will execute
 * the async inspector (as if it would be non-async one). Host cannot assume and does not know which inspector is
 * "less important" (could be dropped without having data loss in case of excessive load for example), hence it applies
 * same rules to all inspectors.
 *
 * @author cstamas
 * @deprecated In favor of {@link EventSubscriberHost}.
 */
@Named
@EagerSingleton
@Deprecated
public class DefaultEventInspectorHost
    extends AbstractLoggingComponent
    implements EventInspectorHost
{
  private final EventSubscriberHost eventSubscriberHost;

  @Inject
  public DefaultEventInspectorHost(final EventSubscriberHost eventSubscriberHost) {
    this.eventSubscriberHost = checkNotNull(eventSubscriberHost);
  }

  // == EventInspectorHost iface

  @Override
  public void shutdown() {
    // nothing
  }

  /**
   * Used by UTs and ITs only, to "wait for calm period", when all the async event inspectors finished.
   */
  @VisibleForTesting
  @Override
  public boolean isCalmPeriod() {
    return eventSubscriberHost.isCalmPeriod();
  }

  @VisibleForTesting
  public void onEvent(final Event<?> evt) {
    eventSubscriberHost.onEvent(evt);
  }
}
