/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link Repository} implementation.
 *
 * @since 3.0
 */
public class RepositoryImpl
    extends ComponentSupport
    implements Repository
{
  private final EventBus eventBus;

  private final Type type;

  private final Format format;

  private final FacetLookup facets = new FacetLookup();

  private String name;

  private Configuration configuration;

  // TODO: Sort out synchronization needed to ensure state sanity
  private volatile boolean started;

  @Inject
  public RepositoryImpl(final EventBus eventBus,
                        final @Assisted Type type,
                        final @Assisted Format format)
  {
    this.eventBus = checkNotNull(eventBus);
    this.type = checkNotNull(type);
    this.format = checkNotNull(format);
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Format getFormat() {
    return format;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Configuration getConfiguration() {
    checkState(configuration != null);
    return configuration;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type=" + type +
        ", format=" + format +
        ", name='" + name + '\'' +
        '}';
  }

  //
  // Lifecycle
  //

  @Override
  public void init(final Configuration configuration) throws Exception {
    this.configuration = checkNotNull(configuration);
    this.name = configuration.getRepositoryName();

    log.debug("Initialized");
  }

  @Override
  public void start() throws Exception {
    checkState(!started, "Already started");

    log.trace("Starting");

    for (Facet facet : facets) {
      log.debug("Starting facet: {}", facet);
      facet.start();
    }

    eventBus.post(new RepositoryStartedEvent(this));

    started = true;

    log.trace("Started");
  }

  @Override
  public void stop() throws Exception {
    checkState(started, "Not started");

    log.trace("Stopping");

    for (Facet facet : facets) {
      log.debug("Stopping facet: {}", facet);
      facet.stop();
    }

    eventBus.post(new RepositoryStoppedEvent(this));

    started = false;

    log.trace("Stopped");
  }

  @Override
  public void destroy() throws Exception {
    log.trace("Destroying");

    if (started) {
      stop();
    }

    for (Facet facet : facets) {
      log.debug("Destroying facet: {}", facet);
      facet.destroy();
    }

    facets.clear();

    eventBus.post(new RepositoryDestroyedEvent(this));

    log.trace("Destroyed");
  }

  //
  // Facets
  //

  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);

    log.debug("Attaching facet: {}", facet);
    facet.init(this);
    facets.add(facet);
  }

  @Override
  @Nonnull
  public <T extends Facet> T facet(final Class<T> type) throws MissingFacetException {
    T facet = facets.get(type);
    if (facet == null) {
      throw new MissingFacetException(this, type);
    }

    return facet;
  }

  @Override
  public Iterable<Facet> facets() {
    return facets;
  }
}
