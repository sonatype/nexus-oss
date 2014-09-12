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
package org.sonatype.nexus.componentviews.example;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.NotFoundHandler;
import org.sonatype.nexus.componentviews.RequestMatcher;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewFactory;

import com.google.common.collect.ImmutableMap;

/**
 * A factory for constructing {@link View}s that expose the {@link ExamplePathListHandler} out of {@link ViewConfig}s.
 *
 * @since 3.0
 */
@Named
@Singleton
public class PathListViewFactory
    implements ViewFactory
{
  private final NotFoundHandler notFound;

  @Inject
  public PathListViewFactory(final NotFoundHandler notFound) {
    this.notFound = notFound;
  }

  @Override
  public String getFactoryName() {
    return "pathList";
  }

  @Override
  public View createView(final ViewConfig config) {
    final Router router = new Router(Arrays.asList((RequestMatcher) new AllRequestMatcher("pathList")));

    final View view = new View(config, router,
        ImmutableMap.of("pathList", (Handler) new ExamplePathListHandler()), notFound);

    return view;
  }
}
