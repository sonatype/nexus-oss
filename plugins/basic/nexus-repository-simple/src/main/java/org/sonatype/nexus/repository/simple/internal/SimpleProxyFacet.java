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

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple proxy facet.
 *
 * @since 3.0
 */
@Named
@Facet.Exposed
public class SimpleProxyFacet
    extends FacetSupport
{
  public static final String CONFIG_KEY = "proxy";

  private String remoteUrl;

  @Override
  protected void doInit(final Repository repository) throws Exception {
    NestedAttributesMap attributes = repository.getConfiguration().attributes(CONFIG_KEY);
    remoteUrl = attributes.require("remoteUrl", String.class);
    log.debug("Remote URL: {}", remoteUrl);
  }

  @Override
  protected void doDestroy() throws Exception {
    remoteUrl = null;
  }

  /**
   * ???
   */
  @Nullable
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();

    // TODO:

    return null;
  }
}
