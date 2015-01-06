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
import org.sonatype.nexus.repository.stateguard.Action;
import org.sonatype.nexus.repository.stateguard.StateGuard;
import org.sonatype.nexus.repository.stateguard.StateGuardAware;
import org.sonatype.nexus.repository.util.MultipleFailures;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.DESTROYED;
import static org.sonatype.nexus.repository.manager.RepositoryImpl.State.FAILED;
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
    implements Repository, StateGuardAware
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

  static final class State
  {
    public static final String NEW = "NEW";
    public static final String INITIALISED = "INITIALISED";
    public static final String STARTED = "STARTED";
    public static final String STOPPED = "STOPPED";
    public static final String DESTROYED = "DESTROYED";
    public static final String FAILED = "FAILED";
  }

  private final StateGuard states = new StateGuard.Builder()
      .initial(NEW)
      .failure(FAILED)
      .create();

  @Override
  public StateGuard getStateGuard() {
    return states;
  }

  //
  // Lifecycle
  //

  @Override
  public void init(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    final RepositoryImpl self = this;

    states.transition(INITIALISED)
        .from(NEW)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            self.configuration = configuration;
            self.name = configuration.getRepositoryName();
          }
        });
  }

  @Override
  public void update(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    final RepositoryImpl self = this;

    states.guard(STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            self.configuration = configuration;

            MultipleFailures failures = new MultipleFailures();
            for (Facet facet : facets) {
              try {
                log.debug("Updating facet: {}", facet);
                facet.update();
              }
              catch (Throwable t) {
                log.error("Failed to update facet: {}", facet, t);
                failures.add(t);
              }
            }
            failures.propagate("Failed to update facets");
          }
        });
  }

  @Override
  public void start() throws Exception {
    final RepositoryImpl self = this;

    states.transition(STARTED)
        .from(INITIALISED, STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            MultipleFailures failures = new MultipleFailures();
            for (Facet facet : facets) {
              try {
                log.debug("Starting facet: {}", facet);
                facet.start();
              }
              catch (Throwable t) {
                log.error("Failed to start facet: {}", facet, t);
                failures.add(t);
              }
            }
            failures.propagate("Failed to start facets");

            eventBus.post(new RepositoryStartedEvent(self));
          }
        });
  }

  @Override
  public void stop() throws Exception {
    final RepositoryImpl self = this;

    states.transition(STOPPED)
        .from(STARTED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            MultipleFailures failures = new MultipleFailures();
            for (Facet facet : facets) {
              try {
                log.debug("Stopping facet: {}", facet);
                facet.stop();
              }
              catch (Throwable t) {
                log.error("Failed to stop facet: {}", facet, t);
                failures.add(t);
              }
            }
            failures.propagate("Failed to stop facets");

            eventBus.post(new RepositoryStoppedEvent(self));
          }
        });
  }

  @Override
  public void destroy() throws Exception {
    final RepositoryImpl self = this;

    states.transition(DESTROYED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            if (states.is(STARTED)) {
              stop();
            }

            MultipleFailures failures = new MultipleFailures();
            for (Facet facet : facets) {
              try {
                log.debug("Destroying facet: {}", facet);
                facet.destroy();
              }
              catch (Throwable t) {
                log.error("Failed to destroy facet: {}", facet, t);
                failures.add(t);
              }
            }
            failures.propagate("Failed to destroy facets");

            facets.clear();
            configuration = null;

            eventBus.post(new RepositoryDestroyedEvent(self));
          }
        });
  }

  //
  // Facets
  //

  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);
    final RepositoryImpl self = this;

    states.guard(INITIALISED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            log.debug("Attaching facet: {}", facet);
            facet.init(self);
            facets.add(facet);
          }
        });
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
