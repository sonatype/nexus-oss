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

import java.util.List;

import javax.inject.Provider;

import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A base class for implementing {@link Recipe}s.
 *
 * @since 3.0
 */
public abstract class RecipeSupport
    extends ComponentSupport
    implements Recipe
{
  private final Format format;

  private final Type type;

  private final Injector injector;

  protected final List<Provider<? extends Facet>> facetPrototypes;

  public RecipeSupport(final Type type, final Format format, final Injector injector) {
    this.type = checkNotNull(type);
    this.format = checkNotNull(format);
    this.injector = checkNotNull(injector);
    facetPrototypes = Lists.newArrayList();
  }

  protected <T extends Facet> Provider<T> lookupProvider(Class<T> type) {
    return injector.getProvider(type);
  }

  protected <T extends Facet> void addFacetProvider(Class<T> type) {
    facetPrototypes.add(lookupProvider(type));
  }

  @Override
  public Repository create(final Configuration config) throws Exception {
    checkNotNull(config);

    Repository repo = new RepositoryImpl(type, format);
    repo.init(config);

    for (Provider<? extends Facet> facetPrototype : facetPrototypes) {
      final Facet facet = facetPrototype.get();
      repo.attach(facet);
    }

    return repo;
  }

  @Override
  public Format getFormat() {
    return format;
  }

  @Override
  public Type getType() {
    return type;
  }
}
