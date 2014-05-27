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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.nexus.guice.AbstractInterceptorModule;
import org.sonatype.nexus.guice.NexusModules.PluginModule;
import org.sonatype.nexus.guice.NexusTypeBinder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.launch.BundlePlan;
import org.eclipse.sisu.launch.SisuTracker;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
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

  private final AtomicInteger pluginRank = new AtomicInteger(1);

  private final Map<?, ?> variables;

  private final List<AbstractInterceptorModule> interceptorModules;

  public NexusBundleTracker(final BundleContext context, final MutableBeanLocator locator) {
    super(context, Bundle.STARTING | Bundle.ACTIVE, locator);

    variables = lookup(ParameterKeys.PROPERTIES);
    interceptorModules = new EntryListAdapter<>(locator.locate(Key.get(AbstractInterceptorModule.class)));
  }

  @Override
  public BindingPublisher prepare(final Bundle bundle) {
    if (isNexusPlugin(bundle)) {
      prepareRequiredNexusPlugins(bundle);
      return prepareNexusPlugin(bundle);
    }
    return super.prepare(bundle);
  }

  @Override
  protected List<BundlePlan> discoverPlans() {
    return Collections.emptyList();
  }

  private BindingPublisher prepareNexusPlugin(final Bundle bundle) {
    log.info("ACTIVATING {}", bundle);
    try {
      final ClassSpace pluginSpace = new BundleClassSpace(bundle);

      final boolean hasPlexus = bundle.getResource("META-INF/plexus/components.xml") != null;

      // Assemble plugin components and resources
      final List<Module> modules = new ArrayList<Module>();
      modules.add(new PluginModule());
      modules.addAll(interceptorModules);
      if (!hasPlexus) {
        modules.add(new SpaceModule(pluginSpace, BeanScanning.GLOBAL_INDEX).with(NexusTypeBinder.STRATEGY));
      }
      else {
        modules.add(new PlexusSpaceModule(pluginSpace, BeanScanning.GLOBAL_INDEX));
      }
      modules.add(new AbstractModule()
      {
        @Override
        protected void configure() {
          if (!hasPlexus) {
            binder().requireExplicitBindings();
          }
          bind(MutableBeanLocator.class).toInstance(locator);
          bind(RankingFunction.class).toInstance(new DefaultRankingFunction(pluginRank.incrementAndGet()));
          bind(ParameterKeys.PROPERTIES).toInstance(variables);
        }
      });

      final BindingPublisher publisher = new InjectorPublisher(Guice.createInjector(new WireModule(modules)));

      log.info("ACTIVATED {}", bundle);
      return publisher;
    }
    catch (Exception e) {
      log.warn("BROKEN {}", bundle);
      throw e;
    }
  }

  private <T> T lookup(final Key<T> key) {
    return locator.locate(key).iterator().next().getValue();
  }

  private static boolean isNexusPlugin(final Bundle bundle) {
    return bundle.getResource("META-INF/nexus/plugin.xml") != null;
  }

  private void prepareRequiredNexusPlugins(final Bundle bundle) {
    final BundleWiring wiring = bundle.adapt(BundleWiring.class);
    final List<BundleWire> requiredBundles = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
    if (requiredBundles != null) {
      for (BundleWire wire : requiredBundles) {
        try {
          final Bundle requiredBundle = wire.getCapability().getRevision().getBundle();
          if (isNexusPlugin(requiredBundle)) {
            requiredBundle.start();
            // pseudo-event to trigger bundle activation
            addingBundle(requiredBundle, null /* unused */);
          }
        }
        catch (Exception e) {
          log.warn("MISSING {}", wire, e);
        }
      }
    }
  }
}
