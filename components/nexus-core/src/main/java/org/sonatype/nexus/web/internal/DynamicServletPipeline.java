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
package org.sonatype.nexus.web.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Key;
import com.google.inject.servlet.AbstractServletPipeline;
import com.google.inject.servlet.ServletDefinition;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic {@code ServletPipeline} that can update its sequence of servlet definitions on-demand.
 */
@Singleton
final class DynamicServletPipeline
    extends AbstractServletPipeline
{
  private static final String NL = System.getProperty("line.separator");

  private static final Logger log = LoggerFactory.getLogger(DynamicServletPipeline.class);

  // dynamic list of definitions
  private final List<ServletDefinition> servletDefinitions;

  // stable cache of definitions
  private volatile ServletDefinition[] servletDefinitionCache = {};

  @Inject
  DynamicServletPipeline(final BeanLocator locator) {
    servletDefinitions = new EntryListAdapter<>(locator.locate(Key.get(ServletDefinition.class)));
  }

  /**
   * Refreshes stable cache with the latest dynamic definitions.
   */
  public synchronized void refreshCache() {
    servletDefinitionCache = servletDefinitions.toArray(servletDefinitionCache);
    if (log.isDebugEnabled()) {
      final StringBuilder buf = new StringBuilder("Updated servlet definitions:");
      for (final ServletDefinition sd : servletDefinitionCache) {
        buf.append(NL).append(sd.toPaddedString(30));
      }
      log.debug(buf.toString());
    }
  }

  @Override
  protected boolean hasServletsMapped() {
    return servletDefinitionCache.length > 0;
  }

  @Override
  protected ServletDefinition[] servletDefinitions() {
    return servletDefinitionCache;
  }
}
