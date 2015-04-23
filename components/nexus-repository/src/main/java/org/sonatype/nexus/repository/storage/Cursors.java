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
package org.sonatype.nexus.repository.storage;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Collection of common cursors for {@link Asset} and {@link Component}.
 *
 * @since 3.0
 */
public class Cursors
{
  private static final int DEFAULT_PAGE_SIZE = 100;

  private Cursors() {
    // no instance
  }

  /**
   * Returns a cursor backed by {@link StorageTx#findComponents(String, Map, Iterable, String)} method using default
   * page size.
   */
  public static Cursor<Component> findComponents(final @Nullable String whereClause,
                                                 final @Nullable Map<String, Object> parameters,
                                                 final @Nullable Iterable<Repository> repositories,
                                                 final @Nullable String querySuffix)
  {
    return findComponents(whereClause, parameters, repositories, querySuffix, DEFAULT_PAGE_SIZE);
  }

  /**
   * Returns a cursor backed by {@link StorageTx#findComponents(String, Map, Iterable, String)} method using specified
   * page size.
   */
  public static Cursor<Component> findComponents(final @Nullable String whereClause,
                                                 final @Nullable Map<String, Object> parameters,
                                                 final @Nullable Iterable<Repository> repositories,
                                                 final @Nullable String querySuffix,
                                                 final int pageSize)
  {
    checkArgument(pageSize > 0, "must be a positive integer: %s", pageSize);
    return new FCursor(
        whereClause,
        parameters,
        repositories,
        querySuffix,
        pageSize
    );
  }

  private static class FCursor
      implements Cursor<Component>
  {
    @Nullable
    private final String whereClause;

    @Nullable
    private final Map<String, Object> parameters;

    @Nullable
    private final Iterable<Repository> repositories;

    private final String pagingQuerySuffix;

    private final Formatter formatter;

    private final int pageSize;

    private int skip;

    private int limit;

    public FCursor(final @Nullable String whereClause,
                   final @Nullable Map<String, Object> parameters,
                   final @Nullable Iterable<Repository> repositories,
                   final @Nullable String querySuffix,
                   final int pageSize)
    {
      checkArgument(pageSize > 0, "must be a positive integer: %s", pageSize);

      this.whereClause = whereClause;
      this.parameters = parameters;
      this.repositories = repositories;
      final StringBuilder stringBuilder = new StringBuilder();
      if (!Strings.isNullOrEmpty(querySuffix)) {
        stringBuilder.append(querySuffix).append(" ");
      }
      stringBuilder.append("skip %s limit %s");
      this.pagingQuerySuffix = stringBuilder.toString();
      this.formatter = new Formatter(Locale.US);

      this.pageSize = pageSize;
      this.skip = 0;
      this.limit = this.pageSize;
    }

    @Nonnull
    @Override
    public List<Component> next(final StorageTx tx) {
      try {
        final List<Component> result = Lists.newArrayList();
        Iterables.addAll(
            result,
            tx.findComponents(
                whereClause,
                parameters,
                repositories,
                formatter.format(pagingQuerySuffix, skip, limit).toString()
            )
        );
        return result;
      }
      finally {
        skip = limit;
        limit = limit + pageSize;
      }
    }

    @Override
    public void close() {
      // nop
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "whereClause='" + whereClause + '\'' +
          ", parameters=" + parameters +
          ", repositories=" + repositories +
          ", pagingQuerySuffix='" + pagingQuerySuffix + '\'' +
          ", formatter=" + formatter +
          ", pageSize=" + pageSize +
          ", skip=" + skip +
          ", limit=" + limit +
          '}';
    }
  }
}
