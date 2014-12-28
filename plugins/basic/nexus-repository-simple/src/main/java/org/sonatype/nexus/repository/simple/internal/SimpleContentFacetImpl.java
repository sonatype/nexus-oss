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
package org.sonatype.nexus.repository.simple.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SimpleContentFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleContentFacetImpl
  extends FacetSupport
  implements SimpleContentFacet
{
  private final Map<String, SimpleContent> backing = Maps.newConcurrentMap();

  @Override
  protected void doInit(final Repository repository) throws Exception {
    backing.clear();
  }

  @Override
  protected void doDestroy() throws Exception {
    backing.clear();
  }

  @Override
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();
    return backing.get(name);
  }

  @Override
  public Set<Entry<String, SimpleContent>> entries() {
    ensureStarted();
    return ImmutableSet.copyOf(backing.entrySet());
  }

  @Override
  public SimpleContent put(final String name, final SimpleContent content) {
    checkNotNull(name);
    checkNotNull(content);
    ensureStarted();
    SimpleContent prev = backing.put(name, content);
    log.debug("{} repository '{}' content: {} -> {}",
        prev == null ? "Saved" : "Replaced",
        getRepository().getName(),
        name,
        content
    );
    return prev;
  }

  @Override
  public SimpleContent delete(final String name) {
    checkNotNull(name);
    ensureStarted();
    SimpleContent prev = backing.remove(name);
    if (prev != null) {
      log.debug("Deleted repository '{}' content: {} -> {}", getRepository().getName(), name, prev);
    }
    return prev;
  }
}
