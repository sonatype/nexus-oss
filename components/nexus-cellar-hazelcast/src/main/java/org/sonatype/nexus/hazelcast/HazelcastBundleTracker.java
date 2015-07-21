/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.hazelcast;

import com.google.common.base.Strings;
import org.apache.karaf.cellar.core.utils.CombinedClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.BundleTracker;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tracks bundles whose packages should be distributed.
 *
 * @since 3.0
 */
class HazelcastBundleTracker
    extends BundleTracker<Bundle>
{
  private final CombinedClassLoader combinedClassLoader;

  public HazelcastBundleTracker(final BundleContext bundleContext, final CombinedClassLoader combinedClassLoader) {
    super(bundleContext, Bundle.STARTING | Bundle.ACTIVE, null);
    this.combinedClassLoader = checkNotNull(combinedClassLoader);

    open();
  }

  @Override
  public Bundle addingBundle(final Bundle bundle, final BundleEvent event) {
    if (hasDistributedImports(bundle)) {
      combinedClassLoader.addBundle(bundle);
      return bundle;
    }
    return null; // no need to track
  }

  @Override
  public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
    if (!context.equals(bundle.getBundleContext())) {
      combinedClassLoader.removeBundle(bundle);
    }
  }

  private static boolean hasDistributedImports(Bundle bundle) {
    final String imports = Strings.nullToEmpty(bundle.getHeaders().get(Constants.IMPORT_PACKAGE));
    return imports.contains("com.hazelcast") || imports.contains("org.apache.karaf.cellar")
        || imports.contains("com.orientechnologies");
  }
}
