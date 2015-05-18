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
package org.sonatype.nexus.rapture.internal.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.rapture.StateContributor;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableMap;
import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contributes {@code activeBundles} state.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ActiveBundlesStateContributor
  extends ComponentSupport
  implements StateContributor
{
  public static final String STATE_ID = "activeBundles";

  private final BundleContext bundleContext;

  private final BundleService bundleService;

  @Inject
  public ActiveBundlesStateContributor(final BundleContext bundleContext,
                                       final BundleService bundleService)
  {
    this.bundleContext = checkNotNull(bundleContext);
    this.bundleService = checkNotNull(bundleService);
  }

  @Override
  public Map<String, Object> getState() {
    return ImmutableMap.of(STATE_ID, calculateActivateBundles());
  }

  @Override
  @Nullable
  public Map<String, Object> getCommands() {
    return null;
  }

  // TODO: sort out how we can cache/rebuild this, to avoid doing this every status poll

  /**
   * Returns sorted list of activate bundle symbolic-names to use for UI feature activation.
   */
  private Object calculateActivateBundles() {
    log.debug("Calculating active bundles");

    List<String> bundles = new ArrayList<>();

    for (Bundle bundle : bundleContext.getBundles()) {
      BundleInfo info = bundleService.getInfo(bundle);
      String name = info.getSymbolicName();

      // ignore wrapped bundles
      if (name.startsWith("wrap_mvn")) {
        continue;
      }

      // TODO: Should we strip out any others?

      // only report active bundles
      if (info.getState() == BundleState.Active) {
        bundles.add(name);
      }
    }

    Collections.sort(bundles);
    return bundles;
  }
}
