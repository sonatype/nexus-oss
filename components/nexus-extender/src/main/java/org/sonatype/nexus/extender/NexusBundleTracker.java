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
package org.sonatype.nexus.extender;

import java.util.Collections;
import java.util.List;

import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.BundlePlan;
import org.eclipse.sisu.launch.SisuTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Bundle} tracker that tracks and binds bundles with Nexus components.
 * 
 * @since 3.0
 */
public class NexusBundleTracker
    extends SisuTracker
{
  private static final Logger log = LoggerFactory.getLogger(NexusBundleTracker.class);

  private final Bundle systemBundle;

  public NexusBundleTracker(final BundleContext context, final MutableBeanLocator locator) {
    super(context, Bundle.STARTING | Bundle.ACTIVE, locator);
    systemBundle = context.getBundle(0);
  }

  @Override
  protected List<BundlePlan> discoverPlans() {
    return Collections.<BundlePlan> singletonList(new NexusBundlePlan(locator));
  }

  @Override
  public BindingPublisher prepare(final Bundle bundle) {
    if (hasComponents(bundle)) {
      prepareDependencies(bundle);
      try {
        BindingPublisher publisher;
        log.debug("ACTIVATING {}", bundle);
        publisher = super.prepare(bundle);
        log.debug("ACTIVATED {}", bundle);
        return publisher;
      }
      catch (Exception e) {
        log.warn("BROKEN {}", bundle);
        throw e;
      }
    }
    // make sure we have everything we need to start
    else if (bundle.getBundleContext() == context) {
      prepareDependencies(bundle);
    }
    return null;
  }

  @Override
  protected boolean evictBundle(Bundle bundle) {
    // when system is shutting down we disable eviction of bundles to keep things stable
    return super.evictBundle(bundle) && (systemBundle.getState() & Bundle.STOPPING) == 0;
  }

  private void prepareDependencies(final Bundle bundle) {
    final BundleWiring wiring = bundle.adapt(BundleWiring.class);
    final List<BundleWire> wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
    if (wires != null) {
      for (BundleWire wire : wires) {
        try {
          final Bundle dependency = wire.getCapability().getRevision().getBundle();
          if (hasComponents(dependency)) {
            if (!live(dependency)) {
              dependency.start();
            }
            if (live(dependency)) {
              // pseudo-event to trigger bundle activation
              addingBundle(dependency, null /* unused */);
            }
          }
        }
        catch (Exception e) {
          log.warn("MISSING {}", wire, e);
        }
      }
    }
  }

  private static boolean hasComponents(Bundle bundle) {
    return bundle.getResource("META-INF/sisu/javax.inject.Named") != null
        || bundle.getResource("META-INF/plexus/components.xml") != null;
  }

  private static boolean live(final Bundle bundle) {
    return (bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0;
  }
}
