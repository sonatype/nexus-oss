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

import java.util.concurrent.locks.ReentrantLock;

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
import org.sonatype.nexus.repository.util.SimpleStateGuard;
import org.sonatype.nexus.repository.util.SimpleStateGuard.Action;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.DESTROYED;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.INITIALISED;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.NEW;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.STARTED;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.STOPPED;

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

  // visible for static import usage
  static enum State
  {
    NEW,
    INITIALISED,
    STARTED,
    STOPPED,
    DESTROYED
  }

  private final SimpleStateGuard<State> states = new SimpleStateGuard.Builder<State>()
      .lock(new ReentrantLock())
      .initial(NEW)
      .create();

  //
  // Lifecycle
  //

  @Override
  public void init(final Configuration configuration) throws Exception {
    checkNotNull(configuration);

    states.transition(INITIALISED)
        .from(NEW)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _init(configuration);
          }
        });
  }

  private void _init(final Configuration configuration) throws Exception {
    this.configuration = configuration;
    this.name = configuration.getRepositoryName();
  }

  @Override
  public void update(final Configuration configuration) throws Exception {
    checkNotNull(configuration);

    states.transition(STOPPED)
        .from(STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _update(configuration);
          }
        });
  }

  private void _update(final Configuration configuration) throws Exception {
    this.configuration = configuration;

    // TODO: handle exceptions
    for (Facet facet : facets) {
      log.debug("Updating facet: {}", facet);
      facet.update();
    }
  }

  @Override
  public void start() throws Exception {
    states.transition(STARTED)
        .from(INITIALISED, STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _start();
          }
        });
  }

  private void _start() throws Exception {
    // TODO: handle exceptions
    for (Facet facet : facets) {
      log.debug("Starting facet: {}", facet);
      facet.start();
    }

    eventBus.post(new RepositoryStartedEvent(this));
  }

  @Override
  public void stop() throws Exception {
    states.transition(STOPPED)
        .from(STARTED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _stop();
          }
        });
  }

  private void _stop() throws Exception {
    // TODO: handle exceptions
    for (Facet facet : facets) {
      log.debug("Stopping facet: {}", facet);
      facet.stop();
    }

    eventBus.post(new RepositoryStoppedEvent(this));
  }

  @Override
  public void destroy() throws Exception {
    states.transition(DESTROYED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _destroy();
          }
        });
  }

  private void _destroy() throws Exception {
    if (states.is(STARTED)) {
      _stop();
    }

    // TODO: handle exceptions
    for (Facet facet : facets) {
      log.debug("Destroying facet: {}", facet);
      facet.destroy();
    }

    facets.clear();
    configuration = null;

    eventBus.post(new RepositoryDestroyedEvent(this));
  }

  //
  // Facets
  //

  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);

    states.guard(INITIALISED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            _attach(facet);
          }
        });
  }

  private void _attach(final Facet facet) throws Exception {
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
