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
package org.sonatype.nexus.repository;

import org.sonatype.nexus.repository.config.Configuration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link Repository} implementation.
 *
 * @since 3.0
 */
public class RepositoryImpl
    implements Repository
{
  private final Type type;

  private final Format format;

  private final FacetLookup facets = new FacetLookup();

  private String name;

  private Configuration configuration;

  // TODO: Sort out synchronization needed to ensure state sanity
  private volatile boolean started;

  public RepositoryImpl(final Type type, final Format format) {
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
  public void init(final Configuration configuration) throws Exception {
    this.configuration = checkNotNull(configuration);
    this.name = configuration.getRepositoryName();
  }

  @Override
  public void start() throws Exception {
    checkState(!started, "Already started");

    for (Facet facet : facets) {
      facet.start();
    }

    // TODO: fire started event

    started = true;
  }

  @Override
  public void stop() throws Exception {
    checkState(started, "Not started");

    for (Facet facet : facets) {
      facet.stop();
    }

    // TODO: fire stopped event

    started = false;
  }

  @Override
  public void dispose() throws Exception {
    if (started) {
      stop();
    }

    for (Facet facet : facets) {
      facet.dispose();
    }

    facets.clear();

    // TODO: fire disposed event
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);

    facets.add(facet);
  }

  @Override
  public <T extends Facet> T facet(final Class<T> type) throws MissingFacetException {
    final T facet = facets.get(type);
    if (facet == null) {
      throw new MissingFacetException(
          String.format("No facet of type %s in repository %s", type.getSimpleName(), getName()));
    }
    return facet;
  }
}
