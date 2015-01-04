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
package org.sonatype.nexus.repository.manager;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.RepositoryDestroyedEvent;
import org.sonatype.nexus.repository.RepositoryStartedEvent;
import org.sonatype.nexus.repository.RepositoryStoppedEvent;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;

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

  private Configuration configuration;

  private String name;

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
    return checkNotNull(name);
  }

  @Override
  public Configuration getConfiguration() {
    return checkNotNull(configuration);
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
  // State
  //

  // TODO: Sort out synchronization needed to ensure state sanity

  private static enum State
  {
    NEW,
    INITIALISED,
    STARTED,
    STOPPED,
    DESTROYED
  }

  private State state = State.NEW;

  private void ensureState(final State... allowed) {
    assert allowed != null && allowed.length > 0;

    for (State allow : allowed) {
      if (state == allow) {
        return;
      }
    }

    throw new IllegalStateException("Invalid state: " + state.name());
  }

  //
  // Lifecycle
  //

  @Override
  public void init(final Configuration configuration) throws Exception {
    ensureState(State.NEW);

    log.trace("Initialising");

    this.configuration = checkNotNull(configuration);
    this.name = configuration.getRepositoryName();

    state = State.INITIALISED;

    log.trace("Initialized");
  }

  @Override
  public void update(final Configuration configuration) throws Exception {
    ensureState(State.STOPPED);

    log.trace("Updating");

    this.configuration = checkNotNull(configuration);

    for (Facet facet : facets) {
      log.debug("Updating facet: {}", facet);
      facet.update();
    }

    log.trace("Updated");
  }

  @Override
  public void start() throws Exception {
    ensureState(State.INITIALISED, State.STOPPED);

    log.trace("Starting");

    for (Facet facet : facets) {
      log.debug("Starting facet: {}", facet);
      facet.start();
    }

    state = State.STARTED;

    eventBus.post(new RepositoryStartedEvent(this));

    log.trace("Started");
  }

  @Override
  public void stop() throws Exception {
    ensureState(State.STARTED);

    log.trace("Stopping");

    for (Facet facet : facets) {
      log.debug("Stopping facet: {}", facet);
      facet.stop();
    }

    state = State.STOPPED;

    eventBus.post(new RepositoryStoppedEvent(this));

    log.trace("Stopped");
  }

  @Override
  public void destroy() throws Exception {
    // allow any state

    log.trace("Destroying");

    if (state == State.STARTED) {
      stop();
    }

    for (Facet facet : facets) {
      log.debug("Destroying facet: {}", facet);
      facet.destroy();
    }

    facets.clear();
    configuration = null;

    state = State.DESTROYED;

    eventBus.post(new RepositoryDestroyedEvent(this));

    log.trace("Destroyed");
  }

  //
  // Facets
  //

  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);
    ensureState(State.INITIALISED);

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
}
