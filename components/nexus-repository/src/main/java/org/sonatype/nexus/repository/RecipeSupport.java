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

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.google.inject.Key;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.BeanLocator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Support for {@link Recipe} implementations.
 *
 * @since 3.0
 */
public abstract class RecipeSupport
    extends ComponentSupport
    implements Recipe
{
  private final Format format;

  private final Type type;

  private BeanLocator beanLocator;

  protected final List<Provider<? extends Facet>> facetPrototypes = Lists.newArrayList();

  protected RecipeSupport(final Type type, final Format format) {
    this.type = checkNotNull(type);
    this.format = checkNotNull(format);
  }

  @Override
  public Format getFormat() {
    return format;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Inject
  public void installDependencies(final BeanLocator beanLocator) {
    this.beanLocator = checkNotNull(beanLocator);
  }

  // FIXME: Consider alternatives to initialize support bits that do not rely on JSR-250?

  @PostConstruct
  protected final void init() throws Exception {
    log.debug("Initializing");
    doInit();
  }

  protected abstract void doInit();

  /**
   * Add a facet provider.
   */
  protected void addFacet(final Provider<? extends Facet> provider) {
    checkNotNull(provider);
    log.debug("Adding facet provider: {}", provider);
    facetPrototypes.add(provider);
  }

  /**
   * Add a facet provider for the given type.
   */
  protected <T extends Facet> void addFacet(final Class<T> type) {
    checkNotNull(type);

    checkState(beanLocator != null);
    Iterator<? extends BeanEntry<Annotation, T>> iter = beanLocator.locate(Key.get(type)).iterator();
    if (iter.hasNext()) {
      BeanEntry<Annotation, T> entry = iter.next();
      log.debug("Adding facet provider: {} -> {}", type, entry);
      facetPrototypes.add(entry.getProvider());
    }
    else {
      throw new RuntimeException("Missing facet provider for type: " + type);
    }
  }

  @Override
  public void apply(final Repository repository) throws Exception {
    checkNotNull(repository);
    for (Provider<? extends Facet> facetPrototype : facetPrototypes) {
      repository.attach(facetPrototype.get());
    }
  }
}
