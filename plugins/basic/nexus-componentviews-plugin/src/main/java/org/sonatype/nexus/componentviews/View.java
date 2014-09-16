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
package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.componentviews.config.ViewConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A binding between a view name (which is part of the URL), and a list of {@link RequestMatcher request matchers},
 * each of which can delegate to a chain of {@link Handler}s.
 *
 * @since 3.0
 */
public class View
{
  private static final Logger log = LoggerFactory.getLogger(View.class);

  private final Router router;

  private final Handler noRouteHandler;

  private final ViewConfig config;

  public View(final ViewConfig config, final Router router, final Handler noRouteHandler) {
    this.router = router;
    this.config = config;
    this.noRouteHandler = noRouteHandler;
  }

  public String getName() {
    return config.getViewName();
  }

  public ViewResponse dispatch(final ViewRequest request) {
    final Handler handler = router.findHandler(request);

    if (handler == null) {
      return noRouteHandler.handle(request);
    }

    // No exception handling here presumes that these will get caught by a global filter and nicely reported as
    // 500 errors.
    return handler.handle(request);
  }

  public ViewConfig getConfig() {
    return config;
  }
}
