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

import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Proxy {@link SimpleContentFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleProxyFacet
    extends FacetSupport
    implements SimpleContentFacet
{
  public static final String CONFIG_KEY = "proxy";

  private final EventBus eventBus;

  private final SimpleContentIndexHtmlCache indexHtmlCache;

  private String remoteUrl;

  @Inject
  public SimpleProxyFacet(final EventBus eventBus,
                          final SimpleContentIndexHtmlCache indexHtmlCache)
  {
    this.eventBus = checkNotNull(eventBus);
    this.indexHtmlCache = checkNotNull(indexHtmlCache);
  }

  private void reset() {
    remoteUrl = null;
    indexHtmlCache.invalidate();
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    reset();

    NestedAttributesMap attributes = repository.getConfiguration().attributes(CONFIG_KEY);
    remoteUrl = attributes.require("remoteUrl", String.class);
    log.debug("Remote URL: {}", remoteUrl);
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
   * Find content in members, first non-null content wins.
   */
  @Nullable
  @Override
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();

    // TODO:

    return null;
  }

  /**
   * Proxy is read-only.
   */
  @Nullable
  @Override
  public SimpleContent put(final String name, final SimpleContent content) {
    throw new UnsupportedOperationException();
  }

  /**
   * Proxy is read-only.
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
   * ???
   */
  @Override
  public Set<Entry<String, SimpleContent>> entries() {
    // TODO:
    return null;
  }
}
