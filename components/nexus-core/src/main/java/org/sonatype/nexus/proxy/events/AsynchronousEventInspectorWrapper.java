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

package org.sonatype.nexus.proxy.events;

import org.sonatype.plexus.appevents.Event;

import com.google.common.base.Preconditions;

/**
 * A simple async event inspector wrapper, that wraps other event inspector and makes it async. Usable in tests, but
 * not
 * only in tests ... albeit, EventInspectors are Components, applying this on them is not quite trivial.
 *
 * @author cstamas
 * @since 2.0
 */
public class AsynchronousEventInspectorWrapper
    implements EventInspector, AsynchronousEventInspector
{
  private final EventInspector eventInspector;

  public AsynchronousEventInspectorWrapper(final EventInspector eventInspector) {
    this.eventInspector = Preconditions.checkNotNull(eventInspector);
  }

  @Override
  public boolean accepts(Event<?> evt) {
    return eventInspector.accepts(evt);
  }

  @Override
  public void inspect(Event<?> evt) {
    eventInspector.inspect(evt);
  }
}
