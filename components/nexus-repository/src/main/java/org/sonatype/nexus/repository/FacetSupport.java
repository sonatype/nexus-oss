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

import javax.inject.Inject;

import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.FacetSupport.State.DESTROYED;
import static org.sonatype.nexus.repository.FacetSupport.State.INITIALISED;
import static org.sonatype.nexus.repository.FacetSupport.State.NEW;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
import static org.sonatype.nexus.repository.FacetSupport.State.STOPPED;

/**
 * Support for {@link Facet} implementations.
 *
 * @since 3.0
 */
public abstract class FacetSupport
    extends ComponentSupport
    implements Facet
{
  private EventBus eventBus;

  @Inject
  public void installDependencies(final EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
  }

  protected EventBus getEventBus() {
    checkState(eventBus != null);
    return eventBus;
  }

  private Repository repository;

  protected Repository getRepository() {
    return checkNotNull(repository);
  }

  //
  // State
  //

  // TODO: Sort out synchronization needed to ensure state sanity

  protected static enum State
  {
    NEW,
    INITIALISED,
    STARTED,
    STOPPED,
    DESTROYED
  }

  private State state = NEW;

  protected void ensureState(final State... allowed) {
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
  public final void init(final Repository repository) throws Exception {
    ensureState(NEW);

    this.repository = checkNotNull(repository);

    log.trace("Initialising");

    doInit();

    state = INITIALISED;

    log.trace("Initialized");
  }

  protected void doInit() throws Exception {
    // nop
  }

  @Override
  public void update() throws Exception {
    ensureState(STOPPED);

    log.trace("Updating");

    doUpdate();

    log.trace("Updated");
  }

  protected void doUpdate() throws Exception {
    // nop
  }

  @Override
  public final void start() throws Exception {
    ensureState(INITIALISED, STOPPED);

    log.trace("Starting");

    doStart();

    eventBus.register(this);

    state = STARTED;

    log.trace("Started");
  }

  protected void doStart() throws Exception {
    // nop
  }

  @Override
  public final void stop() throws Exception {
    ensureState(STARTED);

    log.trace("Stopping");

    eventBus.unregister(this);

    doStop();

    state = STOPPED;

    log.trace("Stopped");
  }

  protected void doStop() throws Exception {
    // nop
  }

  @Override
  public final void destroy() throws Exception {
    // allow any state

    log.trace("Destroying");

    if (state == STARTED) {
      stop();
    }

    doDestroy();

    repository = null;

    state = DESTROYED;

    log.trace("Destroyed");
  }

  protected void doDestroy() throws Exception {
    // nop
  }
}
