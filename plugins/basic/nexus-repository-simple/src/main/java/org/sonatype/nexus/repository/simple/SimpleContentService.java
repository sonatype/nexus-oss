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
package org.sonatype.nexus.repository.simple;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple content service.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SimpleContentService
  extends ComponentSupport
{
  /**
   * Container for simple repository contents.  Sorted in repository attributes.
   */
  private static class RepositoryContents
  {
    private final Map<String, SimpleContent> backing = Maps.newConcurrentMap();
  }

  /**
   * Return collection of repository specific contents.
   *
   * Store simple contents in repository attributes, so the repository framework manages when these go away.
   */
  private Map<String, SimpleContent> contents(final Repository repository) {
    RepositoryContents contents = repository.getAttributes().get(RepositoryContents.class);
    if (contents == null) {
      contents = new RepositoryContents();
      repository.getAttributes().set(RepositoryContents.class, contents);
    }
    return contents.backing;
  }

  public SimpleContent get(final Repository repository, final String name) {
    checkNotNull(repository);
    checkNotNull(name);
    return contents(repository).get(name);
  }

  public Set<Entry<String, SimpleContent>> entries(final Repository repository) {
    checkNotNull(repository);
    return ImmutableSet.copyOf(contents(repository).entrySet());
  }

  public SimpleContent put(final Repository repository, final String name, final SimpleContent content) {
    checkNotNull(repository);
    checkNotNull(name);
    checkNotNull(content);
    SimpleContent prev = contents(repository).put(name, content);
    log.debug("{} repository '{}' content: {} -> {}",
        prev == null ? "Saved" : "Replaced",
        repository.getName(),
        name,
        content
    );
    return prev;
  }

  public SimpleContent delete(final Repository repository, final String name) {
    checkNotNull(repository);
    checkNotNull(name);
    SimpleContent prev = contents(repository).remove(name);
    if (prev != null) {
      log.debug("Deleted repository '{}' content: {} -> {}", repository.getName(), name, prev);
    }
    return prev;
  }
}
