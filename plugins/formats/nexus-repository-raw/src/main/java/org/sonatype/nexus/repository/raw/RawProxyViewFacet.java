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
package org.sonatype.nexus.repository.raw;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.view.DefaultRoute;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.NotFoundHandler;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.matchers.AlwaysMatcher;

import com.google.common.collect.Lists;

/**
 * A proxy for the remote content.
 *
 * @since 3.0
 */
public class RawProxyViewFacet
    extends ViewFacet
{
  public static final String CONFIG_KEY = "raw_proxy_view_facet" ;


  public RawProxyViewFacet(final Router router) {
    super(router);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    final Configuration configuration = repository.getConfiguration();

    final Router router = getRouter();

    // Do we have the content cached already? Is the cache up to date?

    // Can we find the content remotely?


    // Build the primary route of the raw proxy view

    final List<Handler> handlers = Lists.newArrayList();

    // Check the cache to see if we
    //handlers.add(new CacheCheckHandler(repository));
    handlers.add(new RemoteFetchHandler(repository));

    final Route route = new Route(new AlwaysMatcher(), handlers);

    router.addRoute(route);

    // By default, return a 404
    router.setDefaultRoute(new DefaultRoute(Arrays.asList((Handler) new NotFoundHandler())));
  }
}
