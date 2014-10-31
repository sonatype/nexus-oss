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
package org.sonatype.nexus.component.source.internal;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.ComponentSource;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.ComponentSourceRegistry;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

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
  private final ConcurrentHashMap<ComponentSourceId, ComponentSource> sources = new ConcurrentHashMap<>();

  public void register(final ComponentSource source) {
    checkNotNull(source);

    final ComponentSource sleeve = applySleeve(source);

    final ComponentSource alreadyBound = sources.putIfAbsent(sleeve.getId(), sleeve);

    checkState(alreadyBound == null, "A source is already bound to name %s", sleeve.getId());

    log.info("Registering component source {}", sleeve);
  }

  public boolean unregister(ComponentSource source) {
    checkNotNull(source);

    final ComponentSource removed = sources.remove(source.getId());

    if (removed instanceof ComponentSourceSleeve) {
      ((ComponentSourceSleeve) removed).markExpired();
    }

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
  public <T extends ComponentSource> T getSource(String name) {
    return getSource(getIdByName(name));
  }

  @Nullable
  @Override
  public <T extends ComponentSource> T getSource(final ComponentSourceId sourceId) {
    return (T) this.sources.get(sourceId);
  }

  @Override
  public <T extends ComponentSource> Provider<T> getSourceProvider(final ComponentSourceId sourceId) {
    checkState(InMemorySourceRegistry.this.getSource(sourceId) != null,
        "Cannot obtain source Provider - source %s not found.", sourceId);
    return new Provider<T>()
    {
      @Override
      public T get() {
        final T source = InMemorySourceRegistry.this.getSource(sourceId);
        checkState(source != null, "Attempt to access missing component source %s.", sourceId);
        return source;
      }
    };
  }

  @Override
  public <T extends ComponentSource> Provider<T> getSourceProvider(final String sourceName) {
    final ComponentSourceId id = getIdByName(checkNotNull(sourceName));
    checkNotNull(id, "No source found for name %s.", sourceName);
    return getSourceProvider(id);
  }

  private ComponentSourceId getIdByName(String sourceName) {
    for (ComponentSourceId id : sources.keySet()) {
      if (id.getName().equals(sourceName)) {
        return id;
      }
    }
    return null;
  }

  private ComponentSource applySleeve(ComponentSource source) {
    if (source instanceof PullComponentSource) {
      return new ComponentSourceSleeve((PullComponentSource) source);
    }
    return source;
  }
}
