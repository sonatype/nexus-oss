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
import org.codehaus.plexus.context.Context;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.launch.SisuTracker;
import org.eclipse.sisu.plexus.DefaultPlexusBeanLocator;
import org.eclipse.sisu.plexus.PlexusAnnotatedBeanModule;
import org.eclipse.sisu.plexus.PlexusBeanConverter;
import org.eclipse.sisu.plexus.PlexusBeanLocator;
import org.eclipse.sisu.plexus.PlexusBeanModule;
import org.eclipse.sisu.plexus.PlexusBindingModule;
import org.eclipse.sisu.plexus.PlexusXmlBeanConverter;
import org.eclipse.sisu.plexus.PlexusXmlBeanModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
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

  private final List<BindingPublisher> publishers = new ArrayList<BindingPublisher>();

  private final AtomicInteger pluginRank = new AtomicInteger(1);

  private final BeanManager beanManager;

  private final Map<?, ?> variables;

  private final List<AbstractInterceptorModule> interceptorModules;

  public NexusBundleTracker(final BundleContext context, final MutableBeanLocator locator) {
    super(context, Bundle.ACTIVE, locator);

    beanManager = lookup(Key.get(BeanManager.class));
    variables = lookup(Key.get(Context.class)).getContextData(); // FIXME: change to ParameterKeys.PROPERTIES
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

  private BindingPublisher prepareNexusPlugin(final Bundle bundle) {
    log.info("ACTIVATING " + bundle);
    final BindingPublisher publisher;
    try {
      final ClassSpace pluginSpace = new BundleClassSpace(bundle);

      // Scan for annotated JSR330/Plexus components and Plexus XML
      final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();
      beanModules.add(new PlexusXmlBeanModule(pluginSpace, variables));
      beanModules.add(new PlexusAnnotatedBeanModule(pluginSpace, variables, BeanScanning.INDEX)
          .with(NexusTypeBinder.STRATEGY));

      // Assemble plugin components and resources
      final List<Module> modules = new ArrayList<Module>();
      modules.add(new PluginModule());
      modules.addAll(interceptorModules);
      modules.add(new PlexusBindingModule(beanManager, beanModules));
      modules.add(new AbstractModule()
      {
        @Override
        protected void configure() {
          bind(MutableBeanLocator.class).toInstance(locator);
          bind(RankingFunction.class).toInstance(new DefaultRankingFunction(pluginRank.incrementAndGet()));
          bind(PlexusBeanLocator.class).to(DefaultPlexusBeanLocator.class);
          bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);
          bind(ParameterKeys.PROPERTIES).toInstance(variables);
        }
      });

      publisher = new InjectorPublisher(Guice.createInjector(new WireModule(modules)));
      publishers.add(publisher); // FIXME: workaround for weak-cache publisher issue in superclass

      log.info("ACTIVATED " + bundle);
    }
    catch (Exception e) {
      log.warn("BROKEN " + bundle);
      throw e;
    }
    return publisher;
  }

  private <T> T lookup(final Key<T> key) {
    return locator.locate(key).iterator().next().getValue();
  }

  private static boolean isNexusPlugin(final Bundle bundle) {
    // TODO: check imports, for when plugins eventually move away from Require-Bundle?
    final String requiredBundles = bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
    return null != requiredBundles && (requiredBundles.contains("org.sonatype.nexus.plugin-api"));
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
          log.warn("MISSING " + wire, e);
        }
      }
    }
  }
}