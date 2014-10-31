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

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.ComponentSource;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.ComponentSourceRegistry;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigId;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigStore;
import org.sonatype.nexus.component.source.api.config.ComponentSourceFactory;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * A {@link ComponentSourceConfigStore} wrapper that keeps the {@link ComponentSourceRegistry} up to date with the
 * latest changes in configuration.
 *
 * @since 3.0
 */
@Named("registering")
@Singleton
public class RegisteringComponentSourceStore
    implements ComponentSourceConfigStore
{
  private final InMemorySourceRegistry registry;

  private final Map<String, ComponentSourceFactory> factories;

  private final ComponentSourceConfigStore inner;

  @Inject
  public RegisteringComponentSourceStore(final InMemorySourceRegistry registry,
                                         final Map<String, ComponentSourceFactory> factories,
                                         @Named("orient") final ComponentSourceConfigStore inner)
  {
    this.registry = registry;
    this.factories = factories;
    this.inner = inner;
  }

  @Override
  public ComponentSourceId createId(final String name) {
    return inner.createId(name);
  }

  @Override
  public ComponentSourceConfigId add(final ComponentSourceConfig config) throws IOException {
    final ComponentSourceConfigId id = inner.add(config);
    final ComponentSource source = create(config);
    registry.register(source);
    return id;
  }

  @Override
  public void update(final ComponentSourceConfigId id, final ComponentSourceConfig config) throws IOException {
    inner.update(id, config);
    registry.update(create(config));
  }

  @Override
  public void remove(final ComponentSourceConfigId id) throws IOException {
    checkNotNull(id);
    final ComponentSourceConfig configToRemove = findById(id);
    inner.remove(id);
    registry.unregister(registry.getSource(configToRemove.getSourceId()));
  }

  @Override
  public Map<ComponentSourceConfigId, ComponentSourceConfig> getAll() throws IOException {
    return inner.getAll();
  }

  @Nullable
  @Override
  public ComponentSourceConfig get(final String sourceName) throws IOException {
    return inner.get(sourceName);
  }

  @Override
  public void start() throws Exception {
    inner.start();
  }

  @Override
  public void stop() throws Exception {
    inner.stop();
  }

  @Override
  public Lifecycle getLifecycle() {
    return inner.getLifecycle();
  }

  @Nullable
  private ComponentSourceConfig findById(ComponentSourceConfigId id) throws IOException {
    final Map<ComponentSourceConfigId, ComponentSourceConfig> all = getAll();
    for (ComponentSourceConfigId eachId : all.keySet()) {
      if (eachId.equals(id)) {
        return all.get(id);
      }
    }
    return null;
  }

  private ComponentSource create(final ComponentSourceConfig config) {
    final ComponentSourceFactory factory = findByName(config.getFactoryName());
    return factory.createSource(config);
  }

  @Nullable
  private ComponentSourceFactory findByName(String name) {
    final ComponentSourceFactory factory = factories.get(name);
    checkState(factory != null, format("No component source factory found for name {}", name));
    return factory;
  }
}
