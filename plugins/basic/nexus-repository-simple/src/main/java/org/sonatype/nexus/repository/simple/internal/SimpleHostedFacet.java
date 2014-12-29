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
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hosted {@link SimpleContentFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleHostedFacet
    extends FacetSupport
    implements SimpleContentFacet
{
  private final EventBus eventBus;

  private final SimpleContentIndexHtmlGenerator indexHtmlGenerator;

  private final Map<String, SimpleContent> store = Maps.newConcurrentMap();

  private volatile String indexHtmlCache;

  @Inject
  public SimpleHostedFacet(final EventBus eventBus,
                           final SimpleContentIndexHtmlGenerator indexHtmlGenerator)
  {
    this.eventBus = checkNotNull(eventBus);
    this.indexHtmlGenerator = checkNotNull(indexHtmlGenerator);
  }

  private void reset() {
    store.clear();
    indexHtmlCache = null;
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    reset();
  }

  @Override
  protected void doDestroy() throws Exception {
    reset();
  }

  @Override
  @Nullable
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();
    return store.get(name);
  }

  @Override
  @Nullable
  public SimpleContent put(final String name, final SimpleContent content) {
    checkNotNull(name);
    checkNotNull(content);
    ensureStarted();

    SimpleContent prev = store.put(name, content);
    log.debug("{} repository '{}' content: {} -> {}",
        prev == null ? "Created" : "Updated",
        getRepository().getName(),
        name,
        content
    );

    invalidateIndexHtmlCache();

    if (prev == null) {
      eventBus.post(new SimpleContentCreatedEvent(getRepository(), content));
    }
    else {
      eventBus.post(new SimpleContentUpdatedEvent(getRepository(), content));
    }

    return prev;
  }

  @Override
  @Nullable
  public SimpleContent delete(final String name) {
    checkNotNull(name);
    ensureStarted();

    SimpleContent prev = store.remove(name);
    if (prev != null) {
      log.debug("Deleted repository '{}' content: {} -> {}", getRepository().getName(), name, prev);

      invalidateIndexHtmlCache();

      eventBus.post(new SimpleContentDeletedEvent(getRepository(), prev));
    }
    return prev;
  }

  @Override
  public Set<Map.Entry<String, SimpleContent>> entries() {
    return ImmutableSet.copyOf(store.entrySet());
  }

  private void invalidateIndexHtmlCache() {
    indexHtmlCache = null;
    log.debug("Invalidated index.html cache");
  }

  @Override
  public String indexHtml() {
    ensureStarted();

    // resolve volatile to local for thread sanity
    String html = indexHtmlCache;

    // maybe generate index.html
    if (html == null) {
      html = indexHtmlGenerator.generate(getRepository(), store.entrySet());

      // cache index.html
      indexHtmlCache = html;
      log.debug("Cached index.html");
    }

    return html;
  }
}
