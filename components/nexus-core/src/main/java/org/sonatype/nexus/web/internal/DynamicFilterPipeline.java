/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.web.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.inject.Key;
import com.google.inject.servlet.AbstractFilterPipeline;
import com.google.inject.servlet.FilterDefinition;
import com.google.inject.servlet.FilterPipeline;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic {@link FilterPipeline} that can update its sequence of filter definitions on-demand.
 */
@Singleton
// don't use @Named, keep as implicit JIT-binding
final class DynamicFilterPipeline
    extends AbstractFilterPipeline
{
  private static final String NL = System.getProperty("line.separator");

  private static final Logger log = LoggerFactory.getLogger(DynamicFilterPipeline.class);

  private final DynamicServletPipeline servletPipeline;

  private final BeanLocator locator;

  // dynamic list of definitions
  private final List<FilterDefinition> filterDefinitions;

  // stable cache of definitions
  private volatile FilterDefinition[] filterDefinitionCache = {};

  private volatile ServletContext servletContext;

  @Inject
  DynamicFilterPipeline(final DynamicServletPipeline servletPipeline, final BeanLocator locator) {
    super(null, servletPipeline, null);
    this.servletPipeline = servletPipeline;
    this.locator = locator;

    try {
      // disable lazy init as we don't use it
      super.initPipeline(null /* unused */);
    }
    catch (final Exception e) {
      throw new IllegalStateException(e);
    }

    filterDefinitions = new EntryListAdapter<>(locator.locate(Key.get(FilterDefinition.class)));
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * Refreshes stable cache with the latest dynamic definitions.
   */
  public synchronized void refreshCache() {
    filterDefinitionCache = filterDefinitions.toArray(filterDefinitionCache);
    if (log.isDebugEnabled()) {
      final StringBuilder buf = new StringBuilder("Updated filter definitions:");
      for (final FilterDefinition fd : filterDefinitionCache) {
        buf.append(NL).append(fd.toPaddedString(30));
      }
      log.debug(buf.toString());
    }
    servletPipeline.refreshCache();
  }

  @Override
  public synchronized void initPipeline(final ServletContext context) throws ServletException {
    if (servletContext == null && context != null) {
      servletContext = context;

      // register trigger to update definitions as FilterPipeline bindings come and go
      locator.watch(Key.get(FilterPipeline.class), new FilterPipelineMediator(), this);
    }
  }

  @Override
  protected boolean hasFiltersMapped() {
    return filterDefinitionCache.length > 0;
  }

  @Override
  protected FilterDefinition[] filterDefinitions() {
    return filterDefinitionCache;
  }
}
