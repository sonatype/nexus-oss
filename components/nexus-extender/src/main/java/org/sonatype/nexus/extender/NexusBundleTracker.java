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

import static org.sonatype.nexus.extender.NexusBundlePlan.hasComponents;

/**
 * {@link Bundle} tracker that tracks and binds bundles with Nexus components.
 * 
 * @since 3.0
 */
public class NexusBundleTracker
    extends SisuTracker
{
  private static final Logger log = LoggerFactory.getLogger(NexusBundleTracker.class);

  public NexusBundleTracker(final BundleContext context, final MutableBeanLocator locator) {
    super(context, Bundle.STARTING | Bundle.ACTIVE, locator);
  }

  @Override
  protected List<BundlePlan> discoverPlans() {
    return Collections.<BundlePlan> singletonList(new NexusBundlePlan(locator));
  }

  @Override
  public BindingPublisher prepare(final Bundle bundle) {
    if (bundle.getBundleContext() != context && hasComponents(bundle)) {
      prepareDependencies(bundle);
      try {
        log.info("ACTIVATING {}", bundle);
        BindingPublisher publisher = super.prepare(bundle);
        log.info("ACTIVATED {}", bundle);
        return publisher;
      }
      catch (Exception e) {
        log.warn("BROKEN {}", bundle);
        throw e;
      }
    }
    return null;
  }

  private void prepareDependencies(final Bundle bundle) {
    final BundleWiring wiring = bundle.adapt(BundleWiring.class);
    final List<BundleWire> wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
    if (wires != null) {
      for (BundleWire wire : wires) {
        try {
          final Bundle dependency = wire.getCapability().getRevision().getBundle();
          if (notYetStarted(dependency) && hasComponents(dependency)) {
            dependency.start();

            // pseudo-event to trigger bundle activation
            addingBundle(dependency, null /* unused */);
          }
        }
        catch (Exception e) {
          log.warn("MISSING {}", wire, e);
        }
      }
    }
  }

  private static boolean notYetStarted(final Bundle bundle) {
    return (bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) == 0;
  }
}
