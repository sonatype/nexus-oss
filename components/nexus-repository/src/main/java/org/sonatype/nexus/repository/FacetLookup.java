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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Helper to provide {@link Facet} lookup.
 *
 * @since 3.0
 */
public class FacetLookup
  implements Iterable<Facet>
{
  private final Map<Class<? extends Facet>, Facet> backing = Maps.newHashMap();

  // TODO: sort out synchronization requirement for usage

  public void add(final Facet facet) {
    checkNotNull(facet);

    // discover all types to expose facet
    Class<? extends Facet> root = facet.getClass();
    List<Class<? extends Facet>> types = exposedTypes(root);

    // verify we are not clobbering existing facet types
    for (Class<? extends Facet> type : types) {
      checkState(!backing.containsKey(type), "Duplicate exposed facet type: %s, root: %s", type, root);
    }

    // map all exposed types to given facet
    for (Class<? extends Facet> type : types) {
      backing.put(type, facet);
    }
  }

  private List<Class<? extends Facet>> exposedTypes(final Class<? extends Facet> root) {
    List<Class<? extends Facet>> exposed = Lists.newArrayList();
    // TODO: Provide smarter mapping so that ViewFacet and [Specific]ViewFacet would return the same instance
    // TODO: And verify that facets do not provide more of the same type of interfaces?
    // HACK: For now just add the root type to move forward
    exposed.add(root);
    return exposed;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T extends Facet> T get(final Class<T> type) {
    checkNotNull(type);
    return (T) backing.get(type);
  }

  public void clear() {
    backing.clear();
  }

  @Override
  public Iterator<Facet> iterator() {
    return Iterators.unmodifiableIterator(backing.values().iterator());
  }
}
