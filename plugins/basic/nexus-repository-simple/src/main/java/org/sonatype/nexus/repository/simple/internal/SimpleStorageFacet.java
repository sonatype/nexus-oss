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
package org.sonatype.nexus.repository.simple.internal;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentsFacet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * Simple storage facet.
 *
 * @since 3.0
 */
@Named
@Facet.Exposed
public class SimpleStorageFacet
    extends FacetSupport
    implements SimpleContentsFacet, SimpleIndexHtmlContents
{
  private final Map<String, SimpleContent> store = Maps.newConcurrentMap();

  @Override
  protected void doDestroy() throws Exception {
    store.clear();
  }

  @Override
  @Nullable
  @Guarded(by=STARTED)
  public SimpleContent get(final String name) {
    checkNotNull(name);

    return store.get(name);
  }

  @Override
  @Nullable
  @Guarded(by=STARTED)
  public SimpleContent put(final String name, final SimpleContent content) {
    checkNotNull(name);
    checkNotNull(content);

    SimpleContent prev = store.put(name, content);
    log.debug("{} repository '{}' content: {} -> {}",
        prev == null ? "Created" : "Updated",
        getRepository().getName(),
        name,
        content
    );

    if (prev == null) {
      getEventBus().post(new SimpleContentCreatedEvent(getRepository(), content));
    }
    else {
      getEventBus().post(new SimpleContentUpdatedEvent(getRepository(), content));
    }

    return prev;
  }

  @Override
  @Nullable
  @Guarded(by=STARTED)
  public SimpleContent delete(final String name) {
    checkNotNull(name);

    SimpleContent prev = store.remove(name);
    if (prev != null) {
      log.debug("Deleted repository '{}' content: {} -> {}", getRepository().getName(), name, prev);

      getEventBus().post(new SimpleContentDeletedEvent(getRepository(), prev));
    }
    return prev;
  }

  //
  // SimpleIndexHtmlContents
  //

  @Override
  @Guarded(by=STARTED)
  public Set<Map.Entry<String, SimpleContent>> entries() {
    return ImmutableSet.copyOf(store.entrySet());
  }
}
