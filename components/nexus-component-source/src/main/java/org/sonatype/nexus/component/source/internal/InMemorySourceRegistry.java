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
package org.sonatype.nexus.component.source.internal;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.ComponentSource;
import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.component.source.ComponentSourceRegistry;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A registry of the currently available component sources.
 *
 * @since 3.0
 */
@Named
@Singleton
public class InMemorySourceRegistry
    extends ComponentSupport
    implements ComponentSourceRegistry
{
  private final ConcurrentHashMap<String, ComponentSource> sources = new ConcurrentHashMap<>();

  public void register(ComponentSource source) {
    checkNotNull(source);

    final ComponentSource alreadyBound = sources.putIfAbsent(source.getId().getName(), source);

    checkState(alreadyBound == null, "A source is already bound to name %s", source.getId());

    log.info("Registering component source {}", source);
  }

  public boolean unregister(ComponentSource source) {
    checkNotNull(source);

    final ComponentSource removed = sources.remove(source.getId());

    if (removed != null) {
      log.info("Unregistering source {}", source);
    }

    return removed != null;
  }

  public void update(ComponentSource source) {
    unregister(source);
    register(source);
  }

  @Override
  public ComponentSource getSource(String name) {
    final ComponentSource source = sources.get(name);
    checkArgument(source != null, "No source found with name %s", name);
    return source;
  }

  @Override
  public ComponentSource getSource(final ComponentSourceId sourceId) {
    for (ComponentSource source : sources.values()) {
      if (source.getId().equals(sourceId)) {
        return source;
      }
    }
    throw new IllegalArgumentException(String.format("No source found for id %s", sourceId));
  }
}
