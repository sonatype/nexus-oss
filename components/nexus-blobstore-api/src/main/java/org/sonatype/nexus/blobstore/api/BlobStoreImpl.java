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
package org.sonatype.nexus.blobstore.api;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuard;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.common.stateguard.Transitions;
import org.sonatype.nexus.common.throwables.MultipleFailures;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.NEW;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.FAILED;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.INITIALISED;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.STARTED;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.STOPPED;
import static org.sonatype.nexus.blobstore.api.BlobStoreImpl.State.DELETED;

/**
 * {@link BlobStore} implementation.
 */
public class BlobStoreImpl
  extends StateGuardLifecycleSupport
  implements BlobStore
{
  private final EventBus eventBus;

  private final Type type;

  private final FacetLookup facets = new FacetLookup();

  private BlobStoreConfiguration configuration;

  @Inject
  public BlobStoreImpl(final EventBus eventBus, final @Assisted Type type) {
    this.eventBus = checkNotNull(eventBus);
    this.type = checkNotNull(type);
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public BlobStoreConfiguration getBlobStoreConfiguration() {
    return checkNotNull(configuration);
  }

  @Override
  public String getName() {
    return checkNotNull(configuration.getName());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +"{" +
        "type=" + type +
        ", name=" + getName() + "}";
  }

  static final class State
  {
    public static final String NEW = "NEW";

    public static final String INITIALISED = "INITIALISED";

    public static final String STARTED = "STARTED";

    public static final String STOPPED = "STOPPED";

    public static final String DELETED = "DELETED";

    public static final String FAILED = "FAILED";
  }

  private final StateGuard states = new StateGuard.Builder()
      .logger(createLogger())
      .initial(NEW)
      .failure(FAILED)
      .create();
  
  @Override
  @Transitions(from = NEW, to = INITIALISED)
  public void init(final BlobStoreConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
  }

  @Override
  @Transitions(from = {INITIALISED, STOPPED}, to = STARTED)
  public void start() throws Exception {
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
    failures.maybePropagate("Failed to start facets");

    // TODO - KR event publishing
  }

  @Override
  @Transitions(from = STARTED, to = STOPPED)
  public void stop() throws Exception {
    MultipleFailures failures = new MultipleFailures();

    for (Facet facet : facets.reverse()) {
      try {
        log.debug("Stopping facet: {}", facet);
        facet.stop();
      }
      catch (Throwable t) {
        log.error("Failed to stop facet: {}", facet, t);
        failures.add(t);
      }
    }
    failures.maybePropagate("Failed to stop facets");

    // TODO - KR event publishing
  }

  @Override
  @Transitions(from = STOPPED, to = DELETED)
  public void delete() throws Exception {
    MultipleFailures failures = new MultipleFailures();

    for (Facet facet : facets.reverse()) {
      try {
        log.debug("Deleting facet: {}", facet);
        facet.delete();
      }
      catch (Throwable t) {
        log.error("Failed to delete facet: {}", facet, t);
        failures.add(t);
      }
    }
    failures.maybePropagate("Failed to delete facets");
  }

  @Override
  @Guarded(by = INITIALISED)
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);
    log.debug("Attaching facet: {}", facet);
    facet.init(this);
    facets.add(facet);
  }

  @Override
  @Nonnull
  public <T extends Facet> T facet(final Class<T> type) throws IllegalStateException {
    T facet = facets.get(type);
    if (facet == null) {
      throw new MissingFacetException(this, type);
    }

    return facet;
  }
}
