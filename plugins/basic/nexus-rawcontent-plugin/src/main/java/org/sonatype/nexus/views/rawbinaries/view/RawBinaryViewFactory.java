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
package org.sonatype.nexus.views.rawbinaries.view;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.componentviews.NotFoundHandler;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
@Singleton
public class RawBinaryViewFactory
    implements ViewFactory
{
  public static final String RAW_BINARY_RECIPE = "rawBinary";

  private final RawBinaryStore binaryStore;

  private final NotFoundHandler notFoundHandler;

  @Inject
  public RawBinaryViewFactory(final RawBinaryStore binaryStore, final NotFoundHandler notFoundHandler) {
    this.binaryStore = binaryStore;
    this.notFoundHandler = notFoundHandler;
  }

  @Override
  public String getFactoryName() {
    return RAW_BINARY_RECIPE;
  }

  @Override
  public View createView(final ViewConfig config) {
    checkNotNull(config);

    final AllRequestMatcher binariesRequestMatcher = new AllRequestMatcher();

    final Router router = new Router();
    router.addRoute(binariesRequestMatcher, new RawBinariesHandler(binaryStore));

    return new View(config, router, notFoundHandler);
  }
}
