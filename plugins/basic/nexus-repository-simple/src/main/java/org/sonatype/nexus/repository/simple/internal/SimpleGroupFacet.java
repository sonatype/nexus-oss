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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationAttributes;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentEvent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Group {@link SimpleContentFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleGroupFacet
    extends FacetSupport
    implements SimpleContentFacet
{
  private final EventBus eventBus;

  private final RepositoryManager repositoryManager;

  private final SimpleContentIndexHtmlCache indexHtmlCache;

  private final List<String> members = Lists.newArrayList();

  @Inject
  public SimpleGroupFacet(final EventBus eventBus,
                          final RepositoryManager repositoryManager,
                          final SimpleContentIndexHtmlCache indexHtmlCache)
  {
    this.eventBus = checkNotNull(eventBus);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.indexHtmlCache = checkNotNull(indexHtmlCache);
  }

  private void reset() {
    members.clear();
    indexHtmlCache.invalidate();
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    reset();

    // FIXME: refine api here
    Configuration config = repository.getConfiguration();
    ConfigurationAttributes attrs = config.attributes("group");
    members.addAll((Collection<? extends String>) attrs.get("members"));
    log.debug("Members: {}", members);
  }

  @Override
  protected void doStart() throws Exception {
    eventBus.register(this);
  }

  @Override
  protected void doStop() throws Exception {
    eventBus.unregister(this);
  }

  @Override
  protected void doDestroy() throws Exception {
    reset();
  }

  /**
   * Return list of member repositories.
   */
  private List<Repository> members() {
    List<Repository> repos = Lists.newArrayList();
    for (String member : members) {
      Repository repository = repositoryManager.get(member);
      if (repository != null) {
        repos.add(repository);
      }
      else {
        log.warn("Ignoring missing member repository: {}", member);
      }
    }
    return repos;
  }

  /**
   * Find content in members, first non-null content wins.
   */
  @Nullable
  @Override
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();

    for (Repository repository : members()) {
      SimpleContentFacet facet = repository.facet(SimpleContentFacet.class);
      SimpleContent content = facet.get(name);
      if (content != null) {
        return content;
      }
    }

    return null;
  }

  /**
   * Group is read-only.
   */
  @Nullable
  @Override
  public SimpleContent put(final String name, final SimpleContent content) {
    throw new UnsupportedOperationException();
  }

  /**
   * Group is read-only.
   */
  @Nullable
  @Override
  public SimpleContent delete(final String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String indexHtml() {
    ensureStarted();
    return indexHtmlCache.get(getRepository(), this);
  }

  /**
   * Returns a union of all member repositories contents.
   *
   * Order of members is important, first member with content will be included,
   * second with same name is masked by former.
   */
  @Override
  public Set<Entry<String, SimpleContent>> entries() {
    Map<String,SimpleContent> union = Maps.newHashMap();

    for (Repository repository : members()) {
      SimpleContentFacet facet = repository.facet(SimpleContentFacet.class);
      for (Map.Entry<String,SimpleContent> entry : facet.entries()) {
        // only add new content if named entry does not already exist
        if (!union.containsKey(entry.getKey())) {
          union.put(entry.getKey(), entry.getValue());
        }
      }
    }

    return union.entrySet();
  }

  //
  // Events
  //

  /**
   * Invalidate index cache if any member repositories are mutated.
   */
  private void maybeInvalidateIndexHtmlCache(final SimpleContentEvent event) {
    if (members.contains(event.getRepository().getName())) {
      indexHtmlCache.invalidate();
    }
  }

  @Subscribe
  public void on(final SimpleContentCreatedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }

  @Subscribe
  public void on(final SimpleContentUpdatedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }

  @Subscribe
  public void on(final SimpleContentDeletedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }
}
