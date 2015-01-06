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
package org.sonatype.nexus.repository;

import javax.inject.Inject;

import org.sonatype.nexus.repository.util.SimpleStateGuard;
import org.sonatype.nexus.repository.util.SimpleStateGuard.Action;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.FacetSupport.State.DESTROYED;
import static org.sonatype.nexus.repository.FacetSupport.State.FAILED;
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

  public static enum State
  {
    NEW,
    INITIALISED,
    STARTED,
    STOPPED,
    DESTROYED,
    FAILED
  }

  protected final SimpleStateGuard<State> states = new SimpleStateGuard.Builder<State>()
      .initial(NEW)
      .failure(FAILED)
      .create();

  //
  // Lifecycle
  //

  @Override
  public final void init(final Repository repository) throws Exception {
    checkNotNull(repository);
    final FacetSupport self = this;

    states.transition(INITIALISED)
        .from(NEW)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            self.repository = repository;
            doInit();
          }
        });
  }

  protected void doInit() throws Exception {
    // nop
  }

  @Override
  public void update() throws Exception {
    states.guard(STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            doUpdate();
          }
        });
  }

  protected void doUpdate() throws Exception {
    // nop
  }

  @Override
  public final void start() throws Exception {
    final FacetSupport self = this;

    states.transition(STARTED)
        .from(INITIALISED, STOPPED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            doStart();
            eventBus.register(self);
          }
        });
  }

  protected void doStart() throws Exception {
    // nop
  }

  @Override
  public final void stop() throws Exception {
    final FacetSupport self = this;

    states.transition(STOPPED)
        .from(STARTED)
        .run(new Action() {
          @Override
          public void run() throws Exception {
            eventBus.unregister(self);
            doStop();
          }
        });
  }

  protected void doStop() throws Exception {
    // nop
  }

  @Override
  public final void destroy() throws Exception {
    final FacetSupport self = this;

    states.transition(DESTROYED)
        .run(new Action()
        {
          @Override
          public void run() throws Exception {
            if (states.is(STARTED)) {
              stop();
            }

            doDestroy();
            self.repository = null;
          }
        });
  }

  protected void doDestroy() throws Exception {
    // nop
  }
}
