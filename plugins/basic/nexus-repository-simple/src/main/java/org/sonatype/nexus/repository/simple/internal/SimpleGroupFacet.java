/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentEvent;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentsFacet;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.util.TypeTokens.COLLECTION_STRING;

/**
 * Simple group facet.
 *
 * @since 3.0
 */
@Named
public class SimpleGroupFacet
    extends FacetSupport
    implements SimpleContentsFacet
{
  public static final String CONFIG_KEY = "group";

  private final RepositoryManager repositoryManager;

  private final Set<String> memberNames = Sets.newLinkedHashSet();

  @Inject
  public SimpleGroupFacet(final RepositoryManager repositoryManager) {
    this.repositoryManager = checkNotNull(repositoryManager);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    NestedAttributesMap attributes = repository.getConfiguration().attributes(CONFIG_KEY);
    memberNames.addAll(attributes.require("memberNames", COLLECTION_STRING));
    log.debug("Members names: {}", memberNames);

    // TODO: Validate members are compatible on start?
  }

  @Override
  protected void doDestroy() throws Exception {
    memberNames.clear();
  }

  /**
   * Return list of member repositories.
   */
  private List<Repository> members() {
    List<Repository> members = Lists.newArrayListWithCapacity(memberNames.size());
    for (String name : memberNames) {
      Repository repository = repositoryManager.get(name);
      if (repository != null) {
        members.add(repository);
      }
      else {
        log.warn("Ignoring missing member repository: {}", name);
      }
    }
    return members;
  }

  /**
   * Find content in members, first non-null content wins.
   */
  @Nullable
  @Override
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();

    // TODO: Should this really be going through the repository's view?
    // TODO: ... and thus need the Request and likely should move this logic to handler?
    for (Repository repository : members()) {
      SimpleContentsFacet contents = repository.facet(SimpleContentsFacet.class);
      SimpleContent content = contents.get(name);
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

  /**
   * Returns a union of all member repositories contents.
   *
   * Order of members is important, first member with content will be included,
   * second with same name is masked by former.
   */
  @Override
  public Set<Entry<String, SimpleContent>> entries() {
    Map<String, SimpleContent> union = Maps.newHashMap();

    for (Repository repository : members()) {
      SimpleContentsFacet contents = repository.facet(SimpleContentsFacet.class);
      for (Map.Entry<String, SimpleContent> entry : contents.entries()) {
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
   * Invalidate index.html cache if any member repositories are mutated.
   */
  private void maybeInvalidateIndexHtmlCache(final SimpleContentEvent event) {
    if (memberNames.contains(event.getRepository().getName())) {
      SimpleIndexHtmlFacet indexHtml = getRepository().facet(SimpleIndexHtmlFacet.class);
      indexHtml.invalidate();
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
